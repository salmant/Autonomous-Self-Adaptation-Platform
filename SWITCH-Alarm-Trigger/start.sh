#!/bin/bash
# Synopsis: ./start.sh --AlarmTriggerYMLURL=salam --MONITORING_SERVER=hello --JSONAlertURL=test --JSONAlertURLSIDEGUI=test2


java AlarmTrigger $AlarmTriggerYMLURL $MONITORING_SERVER catascopia_user catascopia_pass logfile.txt $JSONAlertURL $JSONAlertURLSIDEGUI &

if [ -f "/opt/tomcat/ssl/tomcat-users.xml" ]; then
    echo "Passwords found - running with basic auth"
    cp /opt/tomcat/conf/web.xml.password /opt/tomcat/conf/web.xml
    exec catalina.sh run
else
    echo "SSL Keys not found - running in none secure mode"
    cp /opt/tomcat/conf/web.xml.nonsecure /opt/tomcat/conf/web.xml
    exec catalina.sh run
fi

