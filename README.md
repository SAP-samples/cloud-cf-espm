# ESPM in Cloud Foundry

The ESPM (Enterprise Sales & Procurement Model) application is a reference application which demonstrates how to build applications on SAP Cloud Platform (Cloud Foundry) with the Java runtime. 

# Description

ESPM application has 2 underlying applications
- webshop: this application is a webshopping app, which does have any authentication
- retailer: this application is used by a retailer to manage stocks, approve/reject sales orders. We use authentication here

For more details about the project, please refer to https://github.com/SAP/cloud-espm-v2 

# Requirements

- [Java8](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
- [Maven](https://maven.apache.org/download.cgi)
- [CF CLI](https://help.sap.com/viewer/65de2977205c403bbc107264b8eccf4b/Cloud/en-US/4ef907afb1254e8286882a2bdef0edf4.html)
- If you do not yet have a Cloud Foundry environment trial or enterprise account, signup for a Cloud Foundry environment trial account by following the [documentation](https://help.sap.com/viewer/65de2977205c403bbc107264b8eccf4b/Cloud/en-US/76e79d62fa0149d5aa7b0698c9a33687.html)
- To deploy the MTAR we need the MTA CF CLI plugin, download the MTA CF CLI Plugin from [here](https://tools.hana.ondemand.com/#cloud)
- To build the multi target application, we need the [Cloud MTA Build tool](https://sap.github.io/cloud-mta-build-tool/), download the tool from [here](https://sap.github.io/cloud-mta-build-tool/download/)
- For Windows system, install 'MAKE' from [here](https://sap.github.io/cloud-mta-build-tool/makefile/)

# Download and Installation

### Running the application

**Note:** If you wish to deploy the application using MTA Build tool, then directly skip to the section [Building MTAR using MBT](#building-mtar using mbt)

#### 1. Login to Cloud Foundry

```
cf api <api>
cf login -o <org> -s <space>
```

#### 2. Create Service

If you are using a cf trial account then create the following services for HANA and XSUAA using the below commands respectively:

```
cf create-service hanatrial schema espm-hana
``` 

```
cf cs xsuaa application espm-uaa -c xs-security.json
```

If you are using a productive account then create the required services as mentioned below:

Create SAP HANA Service instance with plan schema using the below command:

```
cf create-service hana schema espm-hana
```
Create SAP HANA Service instance with plan 64standard as described [here](https://help.sap.com/viewer/cc53ad464a57404b8d453bbadbc81ceb/Cloud/en-US/21418824b23a401aa116d9ad42dd5ba6.html)

> If there are multiple instances of SAP HANA Service in the space where you plan to deploy this application, please modify the  mta.yaml as shown below. Replace <database_guid> with the [id of the databse](https://help.sap.com/viewer/cc53ad464a57404b8d453bbadbc81ceb/Cloud/en-US/93cdbb1bd50d49fe872e7b648a4d9677.html?q=guid) you would like to bind the application with :
 ```
 # Hana HDI Container
  - name: cloud-cap-xf-sf-db-hdi-container
    parameters:
      service: hana
      service-plan: hdi-shared
      config:
        database_id: <database_guid>
    properties:
      hdi-container-name: '${service-name}'
    type: com.sap.xs.hdi-container
```

Create SAP XSUAA Service instance with plan application using the below command:

```
cf cs xsuaa application espm-uaa -c xs-security.json
```

#### 3. Edit Manifest

Open the manifest.yml file and edit the following:
Replace <i-number> placeholders with your ```I/D/C numbers``` so that the application name and host name is unique in the CF landscape.

```DATABASE_TYPE: <DB name>```

Replace the ```<DB name>``` with ```hana``` for which you have created the service instance.

Replace the <DB instance name> with the hana service instance that you have created above.

#### 4. Build the application

Maven build the java application to package into a war file

From the root folder execute ```mvn clean install```


```
Note: When running the application in internet, follow the below steps to download the ngdbc driver to your local ~.m2 repository, as its not available in central maven

1. You need to download the SAP Hana Cloud Platform SDK from here: https://tools.hana.ondemand.com/#cloud
2. Take the latest "Java Web Tomcat 8" from the download section (a package starting with neo-).
3. Unzip the archive to an arbitrary location on your devbox.
4. Extract the JDBC driver (ngdbc.jar) from the archive (you will find the driver in the archive under: repository/.archive/lib/ngdbc.jar). The driver is closed source, so it is NOT available from public Maven repositories!
5. Put the driver either to your local maven repository with:
**mvn install:install-file -Dfile=<path-to-file> -DgroupId=com.sap.db.jdbc \**
    **-DartifactId=ngdbc -Dversion=2.0.13 -Dpackaging=jar**

``` 

#### 5. Install Dependencies

Go to web/retailer and execute ```npm install```

Go to web/webshop and execute ```npm install```

Make sure you have npm registry = "https://npm.sap.com/"

To setup the registry locally for each command, use the following

```npm install --registry=https://npm.sap.com/ -proxy=null```

#### 6. Push the application

```cf push -f manifest.yml```


#### 7. Setup Role collections

We need to setup the Role collections for the UAA, since the Retailer application needs the template role called "Retailer" assigned to the user, for the application to work.

 - In your trial account, in the left pane select Role collections under the Security tab.

 - Add a new Role collection named, "Retailer"

 - Click on the newly created "Retailer "role collection and add new Role.

 - Select the application identifier name similar to the one you have given in the xs-security.json file.

 - Select the role template and assign the Role you have created in the previous step.

#### 8. Assign Role to the user

We need to assign the role which we have created in the previous step to the user.

 - In your trial account, in the left pane select "Trust Configuration" under the Security tab.

 - Click on the default IDP service.

 - Enter username/email and click on the add Assignment button.

 - Select the Role as "Retailer" to assign it to the user.

# Security Implementation

![Security Diagram](/docs/images/ui-merge.jpg?raw=true)

Approuter has been binded to the XSUAA service.

Authentication Method specified in the xs-app.json file with

- Webshop - AuthenticationType: None

- Retailer - AuthenticationType: XSUAA

# Securing the Backend Application

To Secure the backend application, we need to bind the XSUAA service to the backend.

![Backend Security Diagram](/docs/images/ui-merge-backend-uaa.jpg?raw=true)


# Building MTAR using MBT

Install MBT: `npm install -g mbt`

Build the application: `mbt build -p=cf`
	
# Deploy MTAR

To Deploy the application navigate to mta_archives folder under your project root folder and run the below command from CLI

	`cf deploy ESPM_MTA_1.0.0.mtar`

In case if MTA Plugin is missing, you can download from https://tools.hana.ondemand.com/#cloud

# How to Obtain Support

In case you find a bug/need support please create github issues

# License

Copyright (c) 2018 SAP SE or an SAP affiliate company. All rights reserved.
This file is licensed under the Apache Software License, v. 2 except as noted otherwise in the [LICENSE](/LICENSE) file.


