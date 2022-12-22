# Basic samples

The samples in this scope teach the very basics about using the EDC framework. When new to the project, you
should do these samples first before moving on to any of the other scopes, as all other samples build up on what you
learn here.

## Samples

### [Basic sample 01](./basic-01-basic-connector/README.md): Run a simple connector

In this sample you will learn what you need to run a connector. You will create a build file and run a very simple
connector, which will serve as the basis for the following samples in this scope.

### [Basic sample 02](./basic-02-health-endpoint/README.md): Write your first extension

In the EDC, every feature or functionality is provided as an extension. This sample will teach you how to create your
own extensions and use them in your connector. The extension created in this sample provides a simple, static health
endpoint.

### [Basic sample 03](./basic-03-configuration/README.md): Use the filesystem-based configuration

The EDC uses many configurable values, so that we e.g. do not have to recompile the entire code just to start a 
component on a different port. In this sample you will learn how to provide configuration values at start up by using
a properties file as well as how to use configuration values in your own extensions.
