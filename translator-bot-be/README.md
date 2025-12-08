# README

## Prerequisites

- JDK 17
- Maven

```
$env:M2_HOME = 'C:\a\software\apache-maven-3.9.4'
$env:JAVA_HOME = 'C:\a\software\jdk-17'
$env:PATH = $env:M2_HOME + '\bin;' + $env:JAVA_HOME + '\bin;' + $env:PATH
```

## Build

```bash
mvn clean package
```

## Run

Setup the environment configuring:

```bash
# mandatory values

## AWS S3
$env:AWS_ACCESS_KEY_ID=*s3 key, see dedicated section below*
$env:AWS_SECRET_ACCESS_KEY=*s3 secrets, see dedicated section below*
$env:AWS_REGION=*region*
$env:AWS_BUCKET=*bucket*

# OIDC
#$env:OIDC_ISSUER=*issuer uri*
#$env:OIDC_JWKS_URI=*jwks uri*
#$env:OIDC_AUTHORIZATION_URL=*authorization url*
#$env:OIDC_CLIENT_ID=*client id*

# Discord
$env:DISCORD_TOKEN=*bot token*

# optional values
$env:SERVER_PORT=8080
$env:TOMCAT_BASEDIR=./tomcat
```

Then run the application

### From maven, for local development

```bash
./mvnw compile exec:java
```

### In production

```bash
java -jar ./target/translator-bot-be-${project.version}.jar
```

### AWS S3 values

- `AWS_ACCESS_KEY_ID`: IAM > Users > *technical user name* > access key
- `AWS_SECRET_ACCESS_KEY`: IAM > Users > *technical user name* > access key, available only when keys are created
