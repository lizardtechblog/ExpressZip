README
=======

###Overview

ExpressZip is a web application that you can use to view and export LizardTech Express Server
imagery. The following list describes some of the features of ExpressZip:
- Easily view Express Server imagery.
- Use filters and location search to find imagery.
- Select multiple layers that you want to export.
- Select an area of interest that you want to export.
- Use shapefiles to select a region that you want to export.
- Create image tiles.
- Select multiple output formats including JPEG, GeoTIFF, PNG, GIF, and BMP.

###Demo

http://demo.lizardtech.com:8080/ExpressZip

###Documentation

http://demo.lizardtech.com:8080/ExpressZip/doc/

###Building ExpressZip

The ExpressZip application is a Java web application for Tomcat. Before you can build the ExpressZip
application from the source code, you must install the Java SDK and Maven.

To compile ExpressZip, run the following command:
```
mvn package
```
Maven builds the project, prints a build success or failure message, and creates a WAR file in the
target directory.

###Deploying ExpressZip

The WAR file created by maven is a JAR file packaged for use as a web application. To deploy the web
application in Tomcat, complete the following steps:

1. Move a copy of the WAR file created by Maven to the Tomcat webapps directory. 
2. In the Tomcat server.xml file, ensure that the unpackWARs and autoDeploy properties are set to true.
3. Restart Tomcat.
When Tomcat starts, the ExpressZip application is extracted from the WAR file and deployed.
4. Configure an export directory and base layer in the ExpressZip.properties file.

###Accessing ExpressZip

By default, you can access ExpressZip via HTTPS and HTTP. To access ExpressZip, open a web browser and navigate to one of the following URLs:

- For HTTP:
```
http://<Tomcat Server name>:8080/ExpressZip
```
- For HTTPS
```
https://<Express Server name>:8443/ExpressZip
```
