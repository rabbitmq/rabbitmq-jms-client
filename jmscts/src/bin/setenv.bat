rem ---------------------------------------------------------------------------
rem Sample environment script for JMS CTS
rem
rem This is invoked by jmscts.bat to configure:
rem . the CLASSPATH, for JMS provider jars
rem . JVM options
rem
rem The following configures the JMS CTS for OpenJMS 0.7.6
rem ---------------------------------------------------------------------------

rem Configure the CLASSPATH
rem
set CLASSPATH=%OPENJMS_HOME%/lib/openjms-0.7.6.jar

rem Configure JVM options
rem
set JAVA_OPTS=-Xmx256m
set JAVA_OPTS=%JAVA_OPTS% -Dopenjms.home=%OPENJMS_HOME% 
set JAVA_OPTS=%JAVA_OPTS% -Djavax.net.ssl.trustStore=%OPENJMS_HOME%/config/client.keystore 
set JAVA_OPTS=%JAVA_OPTS% -Djavax.net.ssl.keyStore=%OPENJMS_HOME%/config/client.keystore
set JAVA_OPTS=%JAVA_OPTS% -Djavax.net.ssl.keyStorePassword=openjms
