#!/bin/sh
# $Id: build.sh,v 1.27 2003/09/29 09:58:07 tanderson Exp $

#
# Set up the environment
#
if [ -z "$JAVA_HOME" ] ; then
  JAVAC=`which java`
  if [ -z "$JAVAC" ] ; then
    echo "Cannot find JAVA. Please set your PATH."
    exit 1
  fi
  JAVA_BIN=`dirname $JAVAC`
  JAVA_HOME=$JAVA_BIN/..
fi

JAVAC=$JAVA_HOME/bin/java

#
# Concatenate all jars required by ANT append the classpath environment 
# variable
#
CP=$JAVA_HOME/lib/tools.jar

CP=lib/ant-1.5.3-1.jar:$CP
CP=lib/ant-optional-1.5.3-1.jar:$CP
CP=lib/xerces-2.3.0.jar:$CP
CP=lib/xml-apis-1.0.b2.jar:$CP
CP=src/etc:$CP

# Note that src/etc is required for the castorbuilder.properties file

#
# Execute the build tool passing the build.xml file
#

echo $CP
$JAVAC -classpath $CP org.apache.tools.ant.Main $*
