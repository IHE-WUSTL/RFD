#!/bin/bash
#*****************************************************
# FormManager run                        R Moulton
# This directory must be current to run.
#*****************************************************
CLASS=edu.wustl.mir.erl.ihe.wss.WSServer
CP=../../../lib/*:../
java -cp $CP $CLASS -a RFDServers -p dev
