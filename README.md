NetBeans Plugin for PlatformIO
==============================
__(NOTE: This project is no longer being developed)__

NBPIO is a plugin suite for Netbeans 8.1 that provides easy integration of PlatformIO within the IDE. It consists of three plugins:

+ PlatformIO Project
+ SerialMonitor
+ PureJavaComm
 
The first provides a special project wizard for PlatformIO projects, the second provides a Serial Monitor while the third is just a wrapper around a PureJavaComm library (an RXTX replacement)

Quickstart
----------
First of all, you need to have [PlatformIO CLI](http://platformio.org/get-started/cli) installed on your system. For a complete guide on how to do that click [here](http://docs.platformio.org/en/latest/installation.html). 

Once you have PlatformIO installed it is time to get going with NBPIO plugin. To take advantage of this plugin set, install PlatformIO Project and SerialMonitor (PureJavaComm will be installed automatially as a dependency) and then:

1. In the main menu go to "File" ⇨ "New Project".
2. Select "PlatformIO" in the "Categories" section.
3. Fill in the project form.
4. The project will be created by PlatformIO according to the specified parameters and imported into NetBeans.
5. The empty project contains a single source file called "main.cpp" which should be automatically opened after creating the project.
6. To build the project use the standard Build Project command or press F11.
7. The upload command from PlatformIO has been mapped to the IDE's "Run" command, so to upload the project select "run" ⇨ "Run Project" or press F6.

The SerialMonitor component can be opened by navigating to "Window" ⇨ "IDE Tools" and selecting "Serial Monitor" (somewhere at the end of the list).

Take a look at [nbpio-demo.mp4](https://raw.githubusercontent.com/gholdys/nbpio/master/nbpio-demo.mp4) file for a short demonstration.

_Currently, the plugins have been tested only on Linux_

