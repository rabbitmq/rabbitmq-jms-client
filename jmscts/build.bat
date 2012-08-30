@echo off
rem $Id: build.bat,v 1.14 2003/09/29 09:58:06 tanderson Exp $

if not defined JAVA_HOME (
  echo JAVA_HOME not set
  exit 1
)

set JAVAC=%JAVA_HOME%\bin\java

rem
rem Concatenate all jars required by ANT append the classpath environment 
rem variable
rem
set CP=%JAVA_HOME%\lib\tools.jar

set CP=lib\ant-1.5.3-1.jar;%CP%
set CP=lib\ant-optional-1.5.3-1.jar;%CP%
set CP=lib\xerces-2.3.0.jar;%CP%
set CP=lib\xml-apis-1.0.b2.jar;%CP%
set CP=src\etc;%CP%

rem Note that src\etc is required for the castorbuilder.properties file


rem Execute the build tool passing the build.xml file

echo %CP%
%JAVAC% -classpath %CP% org.apache.tools.ant.Main %*
