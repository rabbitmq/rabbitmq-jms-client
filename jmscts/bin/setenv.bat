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
set TESTCLASSPATH=C:\development\rabbit-jms\jmscts\jmscts\build\classes;c:\development\rabbit-jms\rabbit-jms-client\com.rabbitmq.jms\target\classes;c:\development\rabbit-jms\rabbit-jms-client\com.rabbitmq.jms\target\test-classes;C:\development\rabbit-jms\rabbit-jms-client\com.rabbitmq.jms\target\rabbitmq-jms-1.0-SNAPSHOT-tests.jar;c:\Users\filip\.m2\repository\com\rabbitmq\amqp-client\2.8.4\amqp-client-2.8.4.jar

rem Configure JVM options
rem
set JAVA_OPTS=-Xmx256m  -Drabbit.jms.terminationTimeout=5000
set JAVA_OPTS=%JAVA_OPTS% -Dopenjms.home=%OPENJMS_HOME% 
set JAVA_OPTS=%JAVA_OPTS% -Djavax.net.ssl.trustStore=%OPENJMS_HOME%/config/client.keystore 
set JAVA_OPTS=%JAVA_OPTS% -Djavax.net.ssl.keyStore=%OPENJMS_HOME%/config/client.keystore
set JAVA_OPTS=%JAVA_OPTS% -Djavax.net.ssl.keyStorePassword=openjms
