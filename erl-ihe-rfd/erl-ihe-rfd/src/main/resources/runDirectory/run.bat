@ECHO OFF
rem ************************************************
rem FormManager run                    R Moulton
rem This directory must be current to run
rem ************************************************
SET ROOT=..\..\
SET CLASS=edu.wustl.mir.erl.ihe.wss.WSServer
SET CP=.:%ROOT%dist\WSServer.jar:%ROOT%lib\*
java -cp %CP% %CLASS% -a RFDServers
