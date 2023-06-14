# ESPM in Cloud Foundry

[![REUSE status](https://api.reuse.software/badge/github.com/SAP-samples/cloud-cf-espm)](https://api.reuse.software/info/github.com/SAP-samples/cloud-cf-espm)

The ESPM (Enterprise Sales & Procurement Model) application is a reference application which demonstrates how to build applications on SAP Business Technology Platform (Cloud Foundry) with the Java runtime. 

# Description

ESPM application has 2 underlying applications
- webshop: this application is a webshopping app, which does not have any authentication.
- retailer: this application is used by a retailer to manage stocks, approve/reject sales orders. We use authentication here.

For more details about the project, please refer to https://github.com/SAP/cloud-espm-v2 

# Requirements

- SAPMachine-Jdk11
- [Maven-3.6.3](https://maven.apache.org/download.cgi)
- Kindly set the npm registry for @sap libraries using the command :
  ```
  npm set @sap:registry=https://registry.npmjs.org/ 
  
  ```
- [CF CLI](https://help.sap.com/viewer/65de2977205c403bbc107264b8eccf4b/Cloud/en-US/4ef907afb1254e8286882a2bdef0edf4.html)
- If you do not yet have a Cloud Foundry environment trial or enterprise account, signup for a Cloud Foundry environment trial account by following the [documentation](https://developers.sap.com/tutorials/hcp-create-trial-account.html)
- To deploy the MTAR we need the MTA CF CLI plugin, download the MTA CF CLI Plugin from [here](https://tools.hana.ondemand.com/#cloud)
- To build the multi target application, we need the [Cloud MTA Build tool](https://sap.github.io/cloud-mta-build-tool/), download the tool from [here](https://sap.github.io/cloud-mta-build-tool/download/).
	- For Windows system, install 'MAKE' from [here](https://sap.github.io/cloud-mta-build-tool/makefile/)
	- Simplest way to install would be by running the command
		```
		npm install -g mbt
		```
		
### Entitlements
	
| Service                                  | Plan       | Number of Instances |
|------------------------------------------|------------|:-------------------:|
| SAP HANA Schemas & HDI Containers        | schema     |          1          |
| SAP HANA cloud                           | hana       |          1          |
| Cloud Foundry Runtime                    |            |          2          |	

# Download and Installation

### Running the application

**Note:** If you wish to deploy the application using MTA Build tool, then directly skip to the section [Building MTAR using MBT](https://github.com/SAP-samples/cloud-cf-espm#building-mtar-using-mbt)

#### 1. Login to Cloud Foundry

```
cf api <api>
cf login -o <org> -s <space>
```

#### 2. Create Service

ESPM application requires 2 backing services XSUAA  and HANA.
1. Create XSUAA service using the below command: 
```
cf cs xsuaa application espm-uaa -c xs-security.json
```
> *Note:*  To avoid redirect uri issues after deployement ,please update xs-security.json file with required changes
  ex :
```
"oauth2-configuration": {
        "redirect-uris": [
        "https://*.eu10-004.hana.ondemand.com/**",
        "https://*.eu10.hana.ondemand.com/**"
        ]
        }
	
```


> *Note:* Make sure the application name used in the xs-security.json is unique.

2. If you are using a SAP Business Technology Platform (BTP) Cloudfoundry trial account then create the  HANA service following the below command
```
cf create-service hanatrial schema espm-hana
``` 

If you are using a productive SAP BTP Cloudfoundry account then create the required HANA services as mentioned below:

2.1 Create SAP HANA Cloud service instance with plan hana as described here : https://help.sap.com/viewer/9ae9104a46f74a6583ce5182e7fb20cb/hanacloud/en-US/784a1dbb421a4da29fb1e3bdf5f198ec.html

2.2 Create schema in SAP HANA Cloud Service instance(created in previous step) by creating an instance of the SAP HANA service broker by running the below command:

```
cf create-service hana schema espm-hana
```
  

#### 4. Build the application

Maven build the java application to package into a war file

From the root folder execute ```mvn clean install```


```
Note: Its no longer required to manually setup ngdbc since the following artifact is available in maven central and also the sap_java_buildpack provides the hana jdbc driver out-of-the-box.
``` 

#### 6. Push the application

 In the root folder ,Replace <your_domain> with your domain name. e.g cfapps.eu10.hana.ondemand.com of  manifest.yml file

```cf push -f manifest.yml```


### Assign Role to the user

 - In your Subaccount, navigate to Security > Users.

 - Enter the e-mail address of the user in search bar, select and expand.

 - Choose Assign Role Collection

 - Select  "Retailer-RoleCollection" role and assign it to the user.

	
#### 9. Accessing the application
	
To access the webshop page launch the given URL : `https://<your-application-ui-route>/webshop/index.html`

To access the retailer page launch the given URL : `https://<your-application-ui-route>/retailer/index.html`

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
The ESPM application can be packaged as a [Multi Target Application](https://www.sap.com/documents/2016/06/e2f618e4-757c-0010-82c7-eda71af511fa.html) which makes it easier to deploy the application. MTA application needs a [MTA Development descriptor](https://help.sap.com/viewer/4505d0bdaf4948449b7f7379d24d0f0d/2.0.03/en-US/4486ada1af824aadaf56baebc93d0256.html)(mta.yml) which is used to define the elements and dependencies of multi-target application. 

> If there are multiple instances of SAP HANA Cloud Service in the space where you plan to deploy this application, please modify the  mta.yaml as shown below. Replace <database_guid> with the [id of the databse](https://help.sap.com/viewer/cc53ad464a57404b8d453bbadbc81ceb/Cloud/en-US/93cdbb1bd50d49fe872e7b648a4d9677.html?q=guid) you would like to bind the application with :

 ```
 # Hana Schema
  - name: espm-hana
    parameters:
      service: hana
      service-plan: schema
      config:
        database_id: <database_guid>
    type: com.sap.xs.hana-schema
```  

Build the application by running following command from root folder of the project: `mbt build -p=cf`
	
# Deploy MTAR
If CF MTA Plugin is not installed, intall if  from [here](https://tools.hana.ondemand.com/#cloud)

Run the below command from CLI

	cf deploy mta_archives/ESPM_MTA_1.0.0.mtar

# Accessing the application
	
To access the webshop page launch the given URL : `https://<your-application-ui-route>/webshop/index.html`

To access the retailer page launch the given URL : `https://<your-application-ui-route>/retailer/index.html`

### Demo script for [ESPM Webshop](/docs/demoscript/WebshopREADME.md) 
### Demo script for [ESPM Retailer-SalesorderApproval](/docs/demoscript/Retailer_SalesOrderApprovalREADME.md)
### Demo script for [ESPM Retailer-StockUpdate](/docs/demoscript/Retailer_StockUpdateREADME.md)


# How to Obtain Support

In case you find a bug/need support please create github issues


## License

Copyright (c) 2020 SAP SE or an SAP affiliate company. All rights reserved. This project is licensed under the Apache Software License, version 2.0 except as noted otherwise in the [LICENSE](LICENSES/Apache-2.0.txt) file.


