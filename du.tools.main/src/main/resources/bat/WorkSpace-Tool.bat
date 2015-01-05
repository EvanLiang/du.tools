@echo off
@echo off&setlocal enabledelayedexpansion

set JAVA_HOME=C:\BEA92\env\Java\jdk1.7.0_10
set CLASSPATH=.;%JAVA_HOME%\jre\lib
set APP_HOME=%cd%
echo %APP_HOME%

for %%i in (dir /b/s %APP_HOME%\*.jar) do set CL_PA=!CL_PA!;%%i
for %%i in (dir /b/s %APP_HOME%\lib\*.jar) do set CL_PA=!CL_PA!;%%i
echo %CL_PA%

set JVM_ARGS="-Xms512M"
@REM set JVM_ARGS="%JVM_ARGS% -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"

START "Launching..." %JAVA_HOME%/bin/javaw.exe %JVM_ARGS% -classpath "%CL_PA%" du.tools.main.windows.WinMain