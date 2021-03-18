# Spotlight Java Client

## Pre-requisites

This example assumes you are using at least Java 8. It only uses classes from Java SE and does not require any external dependencies.

## Requests to HTTP

If you are performing requests to a url using `http` protocol, you can just use the code in `SpotlightClient.java` adjusting the necessary information such as API URL, application token, etc.

## Requests to HTTPS

You can also use the code from `SpotlightClient.java` and it should works fine for requests to `https` if the SSL certificate was issued by a reconigzed authority listed in `$JAVA_HOME/lib/security/cacerts`. Spotlight inside the GE Network uses a self-signed certificate, thus using `SpotlightClient.java` might throw the exception:

    Resolving javax.net.ssl.SSLHandshakeException: sun.security.validator.ValidatorException: PKIX path building failed

If you get that error you have two options, and for both options you will need to download the certificate public key. You can follow the steps in https://www.globalsign.com/en/blog/how-to-view-ssl-certificate-details/ for your preferred browser. Suppose this file is named `spotlight-certificate.crt` once downloaded.

### Adding Spotlight certificate to valid certificates list

If you have admin privileges in the machine where your app is running, run the following command as administrator:

    keytool -import -file spotlight-certificate.crt -alias spotlight-certificate -keystore $JAVA_HOME/lib/security/cacerts

it will ask for a password, and by default it is `changeit`. Contact your system administrator in case this password is different. Once your app is restarted, the code in `SpotlightClient.java` should run without problems.

### Adding Spotlight certificate to requests

If you don't have admin privileges in the machine where your app is running, you will need to add the certificate in each requests using a SSLSocketFactory. First you need to create a custom keystore and include the certificate in it:

    keytool -import -file spotlight-certificate.crt -alias spotlight-certificate -keystore spotlight.jks

The previous command will create a new keystore file named `spotlight.jks`, and then you will be able to use the code in file `SpotlightClientSSL.java` and it should run without problems. You will need to update the required information in `SpotlightClientSSL.java`.

## Compiling

Run `javac SpotlightClient.java` or `javac SpotlightClientSSL.java` and it will generate a `.class` file.

## Running

You can run the compiled class with `java SpotlightClient` or `java SpotlightClientSSL`.