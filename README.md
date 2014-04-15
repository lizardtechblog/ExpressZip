README
=======

This is an example of changing styles for the ExpressZip application. For information on building and deploying ExpressZip, see the main branch. 

You can view the result of the style changes in the following figure:

![Custom Style Example](https://raw.githubusercontent.com/lizardtechblog/ExpressZip/styles/ExpressZip_CustomStyles.png)

The styling information for ExpressZip is contained in the following directory:
```
WebContent/VAADIN/themes/ExpressZip
```
To change the logo, you can either replace the ExpZip_Logo161x33px.png file with your own logo, or you can edit the embedded logo line in following file:
```
/src/com/lizardtech/expresszip/vaadin/MapToolbarViewComponent.java
```
For example, you might change the line to point to the following custom logo file:
```
Embedded logo = new Embedded(null, new ThemeResource("img/Custom_Logo.png"));
