# lstmprovisor-java
Java version of lstm-based Jazz machine-learning project

This project contains java code for running pre-trained compressing auto-encoders on music data (leadsheet (.ls) files) and generating output samples.

Installation:
The project should be cloned and opened in NetBeans.
Once the project is in NetBeans, right click the project and select "Install Ivy".

The driver program reads in a properties file to retrieve parameters for running. Please supply the path to your properties file as args[0] when running. Documentation for which parameters to specify in your properties file is included in the javadoc for the main method in src/main/Driver.java

This program uses directories of csv files or .ctome files generated by the lstm-provisor python portion of this project.

This project was developed by the Impro-Visor Intelligent Music Software Team at Harvey Mudd College led by Professor Robert Keller.
