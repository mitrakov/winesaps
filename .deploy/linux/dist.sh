#!/bin/bash

folder=winesaps_linux_x86
echo Building $folder
java -jar packr.jar --platform linux32 --jdk jdk1.6.0_45_x86.zip --executable winesaps --classpath winesaps.jar --mainclass ru.mitrakov.self.rush.desktop.DesktopLauncher --minimizejre hard --output $folder
chmod +x $folder/jre/bin/java
zip -r $folder.zip $folder/
rm -rf $folder


folder=winesaps_linux_x64
echo Building $folder
java -jar packr.jar --platform linux64 --jdk jdk1.6.0_45_x64.zip --executable winesaps --classpath winesaps.jar --mainclass ru.mitrakov.self.rush.desktop.DesktopLauncher --minimizejre hard --output $folder
chmod +x $folder/jre/bin/java
zip -r $folder.zip $folder/
rm -rf $folder


folder=winesaps_linux_nojre
echo Building $folder
chmod +x winesaps.sh
zip $folder.zip winesaps.jar winesaps.sh


rm winesaps.jar
echo Done...
