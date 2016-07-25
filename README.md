# lstmprovisor-java
Java version of lstm-based Jazz machine-learning project

This project contains java code for running pre-trained compressing auto-encoders on music data (leadsheet (.ls) files) and generating output samples.

Installation:
The project should be cloned and opened in NetBeans.
Once the project is in NetBeans, right click the project and select "Install Ivy".

To run the program, configure the run arguments in build.xml so that:

Args 0 should be input lead sheet path
Argos 1 should be output folder path
Args 2 should be the auto encoder params path
Args3 should be the name generator params path
Args 4 is the queue output folder path
Args 5 is the path to a reference queue path

Right now the Driver is in a state of constant modification for running tests. A configurable driver program is in the works.
