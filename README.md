# Data Acquisition Service - DAS

DAS initiates and manages the operations of downloading and parsing data sets. It is a Spring Boot application build by maven

## Required libraries

Following libraries are necessary to successfully build DAS:
- metadata-parser

## Required services

DAS requires following services to function properly:
- Downloader
- Metadata parser
- User management

## Running DAS locally - demo

To run and test DAS locally you need to install and run User management, Downloader and Metadata parser services.  Moreover, you need to publish Metadata parser in version defined in DAS pom.xml to your local maven repository.
 
#### User Management
- pull *user-management* from git repository
- run service from command line with the following parameters:
	
```mvn spring-boot:run -Dspring.cloud.propertiesFile=spring-cloud.properties```
	
#### Downloader
- pull *downloader* from a git repository
- run service from command line with following parameters:
	
```DOWNLOADS_DIR=/tmp SERVER_PORT=8090 mvn clean spring-boot:run```
	
#### Metadata parser
- pull *metadata-parser* from a git repository
- publish artifact to local repository

```mvn install```
- run service from command line with the following parameters:
	
```DOWNLOADS_DIR=/tmp mvn spring-boot:run```
	
#### DAS
- pull *data-acquisition* from a git repository
- run service from command line with the following parameters:
	
```DOWNLOADER_URL="http://localhost:8090" DOWNLOADS_DIR=/tmp mvn clean test spring-boot:run```

where:

  * **DOWNLOADER_URL** - we need to turn Downloader into valid CF service and it should make it easy to connect
  * **DOWNLOADS_DIR** - this is a folder where object store will put downloaded content. It needs to be shared between DAS and Downloader

To run a simple demo use script ./tools/curl.sh : ```./curl.sh <das_app_url> <data_set_uri> <oauth_token>```
(for data_set_uri - only http/s is implemented)

#### EXAMPLE:
```./curl.sh localhost:8080 https://www.quandl.com/api/v1/datasets/BCHARTS/BITSTAMPUSD.csv "`cf oauth-token | grep bearer`" <organisation's UUID>```

This will download requested csv file and save it to memory into /tmp directory

* You might see in Metadata parser's logs following exception:
```ResourceAccessException: I/O error on PUT request for "http://localhost:5000/rest/datasets/(id)":Connection refused;```
To get rid of that exception you need to run Data catalog service along with Elasticsearch 

## Deployment


There are two manifest files.

  * **manifest.yml** uses queues in memory
  * **manifest-kafka.yml** ueses kafka queues

