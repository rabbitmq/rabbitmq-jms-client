# ---------------------------------------------------------------------------
# Sample environment script for JMS CTS
#
# This is invoked by jmscts.sh to configure:
# . the CLASSPATH, for JMS provider jars
# . JVM options
#
# The following configures the JMS CTS for OpenJMS 0.7.6
# ---------------------------------------------------------------------------

# Configure the CLASSPATH
#
CLASSPATH=$OPENJMS_HOME/lib/openjms-0.7.6.jar

# Configure JVM options
#
JAVA_OPTS=-Xmx256m
JAVA_OPTS="$JAVA_OPTS \
           -Dopenjms.home=$OPENJMS_HOME \
           -Djavax.net.ssl.trustStore=$OPENJMS_HOME/config/client.keystore \
           -Djavax.net.ssl.keyStore=$OPENJMS_HOME/config/client.keystore \
           -Djavax.net.ssl.keyStorePassword=openjms"
