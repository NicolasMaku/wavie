@echo off
setlocal

set src=C:\Users\nicol\Documents\0-ITU\S4\webDynamique\framework\framework\src
set lib=C:\Users\nicol\Documents\0-ITU\S4\webDynamique\framework\framework\lib
set output=C:\Users\nicol\Documents\0-ITU\S4\webDynamique\framework\output
set lib_test=C:\Users\nicol\Documents\0-ITU\S4\webDynamique\framework\projTest\lib

set jar=wavie.jar

dir /s /B "%src%\*.java" > sources.txt

javac -d "%output%" @sources.txt

del sources.txt

jar cf "%lib_test%\%jar%" -C %output% .