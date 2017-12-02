#!/bin/bash

TOMCAT_DIR="/usr/share"

CASSANDRA_WAIT_SEC=25
MONITOR_WAIT_SEC=15

ROUTABLE_IP=$MONITORING_SERVER
PRIVATE_IP=`hostname -i`
ZERO_IP="0.0.0.0"

sed -i 's/^\s*\#\s*\(listen_interface_prefer_ipv6:\s*false\)/\1/' /etc/cassandra/conf/cassandra.yaml
sed -i 's/^\s*\(start_rpc:\)\s*false/\1 true/' /etc/cassandra/conf/cassandra.yaml
sed -i 's/^\#\s*\(rpc_interface_prefer_ipv6:\s*false\)/\1/' /etc/cassandra/conf/cassandra.yaml

sed -i 's/127.0.0.1/'${ROUTABLE_IP}'/g' /etc/cassandra/conf/cassandra.yaml
sed -i 's/127.0.0.1/'${ROUTABLE_IP}'/g' /etc/cassandra/default.conf/cassandra.yaml

sed -i 's/listen_address: localhost/listen_address: '${PRIVATE_IP}'/g' /etc/cassandra/conf/cassandra.yaml
sed -i 's/listen_address: localhost/listen_address: '${PRIVATE_IP}'/g' /etc/cassandra/default.conf/cassandra.yaml

sed -i 's/rpc_address: localhost/rpc_address: '${ZERO_IP}'/g' /etc/cassandra/conf/cassandra.yaml
sed -i 's/rpc_address: localhost/rpc_address: '${ZERO_IP}'/g' /etc/cassandra/default.conf/cassandra.yaml

sed -i 's/\# broadcast_rpc_address: 1.2.3.4/broadcast_rpc_address: '${ROUTABLE_IP}'/g' /etc/cassandra/conf/cassandra.yaml
sed -i 's/\# broadcast_rpc_address: 1.2.3.4/broadcast_rpc_address: '${ROUTABLE_IP}'/g' /etc/cassandra/default.conf/cassandra.yaml

sed -i 's/\# broadcast_address: 1.2.3.4/broadcast_address: '${ROUTABLE_IP}'/g' /etc/cassandra/conf/cassandra.yaml
sed -i 's/\# broadcast_address: 1.2.3.4/broadcast_address: '${ROUTABLE_IP}'/g' /etc/cassandra/default.conf/cassandra.yaml

cassandra start &
sleep $CASSANDRA_WAIT_SEC

while true; do
mode="$(nodetool netstats | grep 'Mode' | awk '{ print $2}')"
case "${mode}" in
        "STARTING")
		echo "Waiting for Cassandra to start."
		sleep 5
		;;
        "NORMAL")
		echo "Cassandra is running."
		break
		;;
		*) echo "There is an issue during the execution of Cassandra";;
    esac
done

sed -i "s/^\s*db_host\s*=\s*\(.*\)/db_host=${ROUTABLE_IP}/" /JCatascopia-Server-0.0.2-SNAPSHOT/JCatascopiaServerDir/resources/server.properties
sed -i "s/localhost/${ROUTABLE_IP}/" /usr/share/tomcat/webapps/JCatascopia-Web/WEB-INF/web.xml
java -jar /JCatascopia-Server-0.0.2-SNAPSHOT/JCatascopiaServerDir/JCatascopia-Server-0.0.2-SNAPSHOT.jar /JCatascopia-Server-0.0.2-SNAPSHOT/JCatascopiaServerDir /var/lock/JCatascopia-Server-lock &
sleep $MONITOR_WAIT_SEC
exec $TOMCAT_DIR/tomcat/bin/catalina.sh run
