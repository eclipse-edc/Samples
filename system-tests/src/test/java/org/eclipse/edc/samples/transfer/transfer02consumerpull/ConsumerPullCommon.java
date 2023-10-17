package org.eclipse.edc.samples.transfer.transfer02consumerpull;


import static org.apache.commons.lang3.RegExUtils.replaceAll;
import static org.eclipse.edc.samples.transfer.FileTransferCommon.getFileContentFromRelativePath;
import static org.eclipse.edc.samples.transfer.TransferUtil.post;
import static org.eclipse.edc.samples.transfer.transfer00prerequisites.PrerequisitesCommon.CONSUMER_MANAGEMENT_HOST;

public class ConsumerPullCommon {

    private static final String START_TRANSFER_FILE_PATH = "transfer/transfer-02-consumer-pull/resources/start-transfer.json";
    private static final String MANAGEMENT_V2_TRANSFER_PROCESS_PATH = "/management/v2/transferprocesses";
    private static final String TRANSFER_PROCESS_ID = "@id";
    private static final String CONTRACT_AGREEMENT_ID_KEY = "<contract agreement id>";

    public static String startConsumerPullTransfer(String contractAgreementId) {
        var requestBody = getFileContentFromRelativePath(START_TRANSFER_FILE_PATH);
        requestBody = requestBody.replaceAll(CONTRACT_AGREEMENT_ID_KEY, contractAgreementId);
        return post(CONSUMER_MANAGEMENT_HOST + MANAGEMENT_V2_TRANSFER_PROCESS_PATH, requestBody, TRANSFER_PROCESS_ID);
    }
}
