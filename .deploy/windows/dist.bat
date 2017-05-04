@echo off


SET folder=winesaps_win_x86
echo Building %folder%
java -jar packr.jar --platform windows32 --jdk jdk1.6.0_45_x86.zip --executable winesaps --classpath winesaps.jar --mainclass ru.mitrakov.self.rush.desktop.DesktopLauncher --minimizejre hard --output %folder%
7z a -tzip -mx7 %folder%.zip %folder%/
rmdir /s/q %folder%


SET folder=winesaps_win_x64
echo Building %folder%
java -jar packr.jar --platform windows64 --jdk jdk1.6.0_45_x64.zip --executable winesaps --classpath winesaps.jar --mainclass ru.mitrakov.self.rush.desktop.DesktopLauncher --minimizejre hard --output %folder%
7z a -tzip -mx7 %folder%.zip %folder%/
rmdir /s/q %folder%


SET folder=winesaps_win_nojre
echo Building %folder%
7z a -tzip -mx7 %folder%.zip winesaps.jar winesaps.vbs


del winesaps.jar
pause
