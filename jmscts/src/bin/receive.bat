@echo off
rem ---------------------------------------------------------------------------
rem Script to receive messages
rem
rem Required Environment Variables
rem
rem   JAVA_HOME       Points to the Java Development Kit installation.
rem
rem Optional Environment Variables
rem 
rem   JMSCTS_HOME     Points to the JMS CTS installation directory.
rem
rem   JAVA_OPTS       Java runtime options used when the command is executed.
rem
rem
rem $Id: receive.bat,v 1.1 2003/12/28 14:03:13 tanderson Exp $
rem ---------------------------------------------------------------------------

if "%OS%" == "Windows_NT" setlocal

if not "%JAVA_HOME%" == "" goto gotJavaHome
echo The JAVA_HOME environment variable is not set.
echo This is required to run jmscts.
exit /B 1

:gotJavaHome

if exist "%JAVA_HOME%\bin\java.exe" goto okJavaHome
echo The JAVA_HOME environment variable is not set correctly.
echo This is required to run jmscts
exit /B 1

:okJavaHome

set _RUNJAVA="%JAVA_HOME%\bin\java"

rem Guess JMSCTS_HOME if it is not set
if not "%JMSCTS_HOME%" == "" goto gotJMSCTSHome
set JMSCTS_HOME=.
if exist "%JMSCTS_HOME%\bin\jmscts.bat" goto okJMSCTSHome
set JMSCTS_HOME=..
if exist "%JMSCTS_HOME%\bin\jmscts.bat" goto okJMSCTSHome
echo The JMSCTS_HOME variable is not set.
echo This is required to run jmscts.
exit /B 1

:gotJMSCTSHome

if exist "%JMSCTS_HOME%\bin\jmscts.bat" goto okJMSCTSHome
echo The JMSCTS_HOME variable is not set correctly.
echo This is required to run jmscts.
exit /B 1

:okJMSCTSHome

rem Set CLASSPATH to empty by default. User jars can be added via the 
rem setenv.bat script
set CLASSPATH=
if exist "%JMSCTS_HOME%\bin\setenv.bat" call "%JMSCTS_HOME%\bin\setenv.bat"

set CLASSPATH=%CLASSPATH%;%JMSCTS_HOME%\lib\@JMSCTS@
set POLICY_FILE=%JMSCTS_HOME%/config/jmscts.policy

rem Configure TrAX
set JAVAX_OPTS=-Djavax.xml.transform.TransformerFactory=org.apache.xalan.processor.TransformerFactoryImpl

rem Execute the requested command

echo Using JMSCTS_HOME: %JMSCTS_HOME%
echo Using JAVA_HOME:   %JAVA_HOME%
echo Using CLASSPATH:   %CLASSPATH%

set MAINCLASS=org.exolab.jmscts.tools.receive.Main
set _EXECJAVA=%_RUNJAVA%

rem Get remaining unshifted command line arguments and save them 
set CMD_LINE_ARGS=
:setArgs
if ""%1""=="""" goto doneSetArgs
set CMD_LINE_ARGS=%CMD_LINE_ARGS% %1
shift
goto setArgs
:doneSetArgs

rem Execute Java with the applicable properties

%_EXECJAVA% %JAVA_OPTS% %JAVAX_OPTS% -classpath "%CLASSPATH%" -Djava.security.manager -Djava.security.policy="%POLICY_FILE%" -Djmscts.home="%JMSCTS_HOME%" %MAINCLASS% %CMD_LINE_ARGS% 
