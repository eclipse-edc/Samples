#!/bin/bash

set -euxo pipefail

# This script creates and configures the following resources:
# - Azure Key Vault (AKV) instance for managing secrets
# - Azure Data Factory (ADF) instance connected to AKV
# It then runs a DPF service configured with credentials to write secrets to AKV and run pipelines in ADF

# Create a service principal to be used by the DPF server to connect to ADF and AKV
test -s secrets || az ad sp create-for-rbac --skip-assignment -o json > secrets
appId=$(jq -r .appId secrets)
tenant=$(jq -r .tenant secrets)
password=$(jq -r .password secrets)

# Azure Subscription and resource names to create
subscriptionId=<subscriptionId>
consumerRg=edc-sample-consumer
providerRg=edc-sample-provider
adf=edcsampleadf
akv=edcsampleadfvault
provstore=edcproviderstore
consstore=edcconsumerstore
location=EastUS
linkedServiceName=AzureKeyVault

# Set Subscription and create resource group
az account set -s $subscriptionId
az group create --name $providerRg -l "$location"
az group create --name $consumerRg -l "$location"

# Create storage accounts for provider-side data (source) and consumer-side data (sink)
az storage account create --name $provstore --resource-group $providerRg
az storage account create --name $consstore --resource-group $consumerRg

# Create ADF
az datafactory create --location "$location" --name "$adf" --resource-group "$providerRg"
adfManagedIdentity=$(az datafactory show --name $adf --resource-group $providerRg --query identity.principalId -o tsv)
adfResourceId=$(az datafactory show --name $adf --resource-group $providerRg --query id -o tsv)

# Create ADF linked service to retrieve keys from AKV
az datafactory linked-service create --factory-name "$adf" --properties "{\"type\":\"AzureKeyVault\",\"typeProperties\":{\"baseUrl\":\"https://$akv.vault.azure.net/\"}}" --name "$linkedServiceName" --resource-group "$providerRg"

# Create AKV
az keyvault show --name $akv --resource-group $providerRg || az keyvault create --name $akv --resource-group $providerRg --location "$location" --enable-rbac-authorization true
akvResourceId=$(az keyvault show --name $akv --resource-group $providerRg --query id -o tsv)

# Grant ADF read access to AKV secrets
az role assignment create --assignee "$adfManagedIdentity" \
  --role "Key Vault Secrets User" \
  --scope "$akvResourceId"

# Grant DPF write access to AKV secrets
az role assignment create --assignee "$appId" \
  --role "Key Vault Secrets Officer" \
  --scope "$akvResourceId"

# Grant DPF access to ADF
az role assignment create --assignee "$appId" \
  --role "Data Factory Contributor" \
  --scope "$adfResourceId"

# Start DPF server
export AZURE_CLIENT_ID=$appId
export AZURE_TENANT_ID=$tenant
export AZURE_SUBSCRIPTION_ID=$subscriptionId
export AZURE_CLIENT_SECRET=$password