#!/bin/bash
#***********************************************************
# Script to help automate debugging tomcat projects on 
# Eclipse using an "outside" tomcat server. Set the env vars
# to values for your project (this file is set up for
# SyslogSender as an example.
# When script is run:
#   1. Tomcat stops if it is running. If it was not running,
#      you will see a Connection Refused error, which you
#      can ignore.
#   2. Existing project war and directory are deleted from
#      the Tomcat webapps directory.
#   3. The war file for the appropriate profile is copied to
#      the Tomcat webapps directory, removing its -profile
#      extension (if any).
#   4. Tomcat is started in debug mode, with all default
#      parameters values except that JPDA_SUSPEND is set,
#      so tomcat will suspend waiting for a connection on
#      the standard debug port (8000).
# After the script is run, connect to the Tomcat using the
# Eclipse debugger. See "Debug tomcat project on Eclipse -
# best way.odt for details. (This script basically automates
# the bash in that SOP.)
#***********************************************************
TOMCAT=/opt/apache-tomcat-8.0.3
NAME=RFD
TARGET=/home/rmoult01/eclipseProjects/rfd/git/rfd/RFD/target

if [ "${PROFILE}" ]
then
   SOURCE=${NAME}-${PROFILE}.war
else
   SOURCE=${NAME}.war
fi
DEST=${NAME}.war
echo Stopping Tomcat
${TOMCAT}/bin/shutdown.sh
sleep 3
echo Clearing and resetting ${NAME}
rm -Rf "${TOMCAT}/webapps/${NAME}*"
cp ${TARGET}/${SOURCE} ${TOMCAT}/webapps/${DEST}
echo Starting Tomcat
export JPDA_SUSPEND=y
${TOMCAT}/bin/catalina.sh jpda start
echo Ready to debug
