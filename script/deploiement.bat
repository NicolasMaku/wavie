@echo off
setlocal enabledelayedexpansion

set nom_projet=wavie

set temp=C:\Users\nicol\Documents\0-ITU\S4\webDynamique\deploiement\temp
set src=C:\Users\nicol\Documents\0-ITU\S4\webDynamique\framework\projTest\src
set web=C:\Users\nicol\Documents\0-ITU\S4\webDynamique\framework\projTest\web
set xml=C:\Users\nicol\Documents\0-ITU\S4\webDynamique\framework\projTest\web.xml
@REM set dispatcher=C:\Users\nicol\Documents\0-ITU\S4\webDynamique\spring02\WEB-INF\dispatcher-servlet.xml
set lib=C:\Users\nicol\Documents\0-ITU\S4\webDynamique\framework\projTest\lib

set project_dir=C:\Program Files\Apache Software Foundation\Tomcat 10.1\webapps

IF EXIST "%temp%" (
    rd /s /q "%temp%"
)

mkdir "%temp%"
mkdir "%temp%\WEB-INF"
mkdir "%temp%\WEB-INF\classes"
mkdir "%temp%\WEB-INF\lib"

xcopy "%web%" "%temp%\views" /s /e /i
xcopy "%xml%" "%temp%\WEB-INF\" /s /e /i
@REM xcopy "%dispatcher%" "%temp%\WEB-INF\" /s /e /i
xcopy "%lib%\*" "%temp%\WEB-INF\lib\" /s /e /i

cd "%src%"
cd ..
dir /s /B "%src%\*.java" > sources.txt
dir /s /B "%lib%\*.jar" > libs.txt

set "classpath="
for /F "delims=" %%i in (libs.txt) do set "classpath=!classpath!%%i;"

javac -d "%temp%\WEB-INF\classes" -cp "%classpath%" @sources.txt

@REM del sources.txt
@REM del libs.txt

cd "%temp%"
cd ..
jar -cvf "%nom_projet%.war" -C "%temp%" .

copy "%nom_projet%.war" "%project_dir%"