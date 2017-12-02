Authors: Salman Taherizadeh
***

A Subscription Cluster represents a group of containers that are providing together the same service. This concept comes when the SWITCH solution performs the horizontal scalability of running container instances. Therefore, for example, if two running container instances are providing the same service, these two containers make one Subscription Cluster. In other words, these two containers belong to a same Subscription Cluster.

There is a table (subscription_agents_table) in the TSDB to store the information about Subscription Clusters. By this table, we can find out that a Subscription Cluster includes which containers. Each Subscription Cluster should have a unique id which is called "subid". As shown in the following example (subscription_agents_table.png), there are two container instances which belong to one Subscription Cluster. Because, these two containers have the same "subid". 
https://github.com/salmant/ASAP/blob/master/SWITCH-Alarm-Trigger/subscription_agents_table.png

In this example (subscription_agents_table.png): 
  - The Subscription Cluster has the subid that is equal to "1ccba0cc92174ce788695cfc0a027b57"
  - The first container instance has the agentid that is equal to "2f56a3593d2f459089685978e795a540"
  - The first container instance has the agentip that is equal to "194.249.0.243"
  - The second container instance has the agentid that is equal to "49476cf05aa24c8c9e7cde38154ca2a4"
  - The second container instance has the agentip that is equal to "195.249.1.247"

Having such information is necessary. Because, you can consider a threshold (e.g. CPU threshold) for a Subscription Cluster. For example, it is possible to specify a CPU-based auto-scaling policy that more containers will be launched if the average CPU utilization of the Subscription Cluster is over a threshold such as 80%. Therefore, to make the average, you should know which containers are in a Subscription Cluster. In the real-world use cases, the auto-scaling policies could be based on the response time of a given service. Therefore, we can also consider other thresholds for application-level metrics, if needed. 

"agentid" is used for the unique identification of container instances. "subid" is the unique identification of Subscription Clusters. Moreover, "agentip" represents "container's IP address" explained here: 
https://github.com/salmant/ASAP/tree/master/SWITCH-Monitoring-System


Instructions for the utilisation of the Alarm-Trigger component in the SWITCH project:

Step 1- Initiating the Monitoring Server on a machine such as “194.249.1.175”.

docker run -e MONITORING_SERVER="194.249.1.175" -p 8080:8080 -p 4242:4242 -p 4245:4245 -p 7199:7199 -p 7000:7000 -p 7001:7001 -p 9160:9160 -p 9042:9042 -p 8012:8012 -p 61621:61621 salmant/ul_monitoring_server_container_image:latest

It takes almost one minute to run the Monitoring Server. The Monitoring Server should be running on a machine with enough memory, disk and CPU resources. This machine should address the Cassandra hardware requirements explained in the following page:

https://wiki.apache.org/cassandra/CassandraHardware

Note 1: The environment variable named "MONITORING_SERVER" should be the IP address of the machine where the Monitoring server is running.

Note 2: The Dockerfile to make the Monitoring Server container image is as follows:

https://github.com/salmant/ASAP/blob/master/SWITCH-Monitoring-System/Dockerfile.centos

Note 3: The Monitoring Server container image is publically released on Docker Hub: 

https://hub.docker.com/r/salmant/ul_monitoring_server_container_image/

Step 2- Using APIs to populate subscription_agents_table.


###################################################################
API1: To add a container instance to a Subscription Cluster.
%%%%%%%%%%%%%%%%%
Method: GET
%%%%%%%%%%%%%%%%%
Description: Insert a row into the table which represents Subscription Clusters.
%%%%%%%%%%%%%%%%%
Input parameters: subid, agentid and agentip.
  - subid is the id of Subscription Cluster
  - agentid represents the id of container instance
  - agentip represents the IP of container instance
%%%%%%%%%%%%%%%%%
Template: http://<MONITORING_SERVER >:8080/JCatascopia-Web/restAPI/agents/subscriptionAgentTable/<subid>/<agentid>/<agentip>
%%%%%%%%%%%%%%%%%
Example: http://194.249.1.175:8080/JCatascopia-Web/restAPI/agents/subscriptionAgentTable/1ccba0cc92174ce788695cfc0a027b57/49476cf05aa24c8c9e7cde38154ca2a4/195.249.1.247
%%%%%%%%%%%%%%%%%
Result: 49476cf05aa24c8c9e7cde38154ca2a4 was inserted in the Virtual Cluster 1ccba0cc92174ce788695cfc0a027b57.
###################################################################


###################################################################
API2: To add a container instance to a Subscription Cluster.
%%%%%%%%%%%%%%%%%
Method: POST
%%%%%%%%%%%%%%%%%
Description: Insert a row into the table which represents Subscription Clusters.
%%%%%%%%%%%%%%%%%
Input parameters: subid, agentid and agentip.
  - subid is the id of Subscription Cluster
  - agentid represents the id of container instance
  - agentip represents the IP of container instance
%%%%%%%%%%%%%%%%%
Template: curl -H "Content-Type: application/json" -X POST http://<MONITORING_SERVER>:8080/JCatascopia-Web/restAPI/agents/subscriptionAgentTable/<subid>/<agentid>/<agentip>
%%%%%%%%%%%%%%%%%
Example: curl -H "Content-Type: application/json" -X POST http://194.249.1.175:8080/JCatascopia-Web/restAPI/agents/subscriptionAgentTable/1ccba0cc92174ce788695cfc0a027b57/49476cf05aa24c8c9e7cde38154ca2a4/195.249.1.247
%%%%%%%%%%%%%%%%%
Result: 49476cf05aa24c8c9e7cde38154ca2a4 was inserted in the Virtual Cluster 1ccba0cc92174ce788695cfc0a027b57.
###################################################################


###################################################################
API3: To delete a container instance from a Subscription Cluster.
%%%%%%%%%%%%%%%%%
Method: GET
%%%%%%%%%%%%%%%%%
Description: Eliminate a row from the table which represents Subscription Clusters.
%%%%%%%%%%%%%%%%%
Input parameters: subid and agentid.
  - subid is the id of Subscription Cluster
  - agentid represents the id of container instance
%%%%%%%%%%%%%%%%%
Template: http://<MONITORING_SERVER>:8080/JCatascopia-Web/restAPI/agents/deleteSubscriptionAgentTable/<subid>/<agentid>
%%%%%%%%%%%%%%%%%
Example: http://194.249.1.175:8080/JCatascopia-Web/restAPI/agents/deleteSubscriptionAgentTable/1ccba0cc92174ce788695cfc0a027b57/49476cf05aa24c8c9e7cde38154ca2a4
%%%%%%%%%%%%%%%%%
Result: 49476cf05aa24c8c9e7cde38154ca2a4 was deleted from the Virtual Cluster 1ccba0cc92174ce788695cfc0a027b57.
###################################################################


###################################################################
API4: To delete a container instance from a Subscription Cluster.
%%%%%%%%%%%%%%%%%
Method: POST
%%%%%%%%%%%%%%%%%
Description: Eliminate a row from the table which represents Subscription Clusters.
%%%%%%%%%%%%%%%%%
Input parameters: subid and agentid.
  - subid is the id of Subscription Cluster
  - agentid represents the id of container instance
%%%%%%%%%%%%%%%%%
Template: curl -H "Content-Type: application/json" -X POST http://<MONITORING_SERVER>:8080/JCatascopia-Web/restAPI/agents/deleteSubscriptionAgentTable/<subid>/<agentid>
%%%%%%%%%%%%%%%%%
Example: curl -H "Content-Type: application/json" -X POST http://194.249.1.175:8080/JCatascopia-Web/restAPI/agents/deleteSubscriptionAgentTable/1ccba0cc92174ce788695cfc0a027b57/49476cf05aa24c8c9e7cde38154ca2a4
%%%%%%%%%%%%%%%%%
Result: 49476cf05aa24c8c9e7cde38154ca2a4 was deleted from the Virtual Cluster 1ccba0cc92174ce788695cfc0a027b57.
###################################################################

Step 3- Providing the YAML file via a URL as input for the Alarm-Trigger. 
To this end, the YAML file should be accessible via a URL such as "http://194.249.1.72:5000/AlarmTrigger.yml". The Alarm-Trigger component will fetch this input and then start working. This URL can be everything, and hence the Alarm-Trigger is able to fetch the YAML file through this URL. 

A template for the YAML file can be seen here: 

https://github.com/salmant/ASAP/blob/master/SWITCH-Alarm-Trigger/AlarmTrigger.yml

Step 4- Preparing an API to receive notifications sent by the Alarm-Trigger component if any metric (defined in the YAML file) would be violated.  
If a metric reaches its associated threshold, the Alarm-Trigger will notify another two entities: the first one which is responsible for an adaptation action and the second one which is the SWITCH Web-based IDE (SIDE). In this situation, the Alarm-Trigger calls an API provided by the first entity and sends a POST notification as a JSON to this API. Similarly, at the same time, the Alarm-Trigger calls an API provided by the second entity and sends a POST notification as a JSON to this API as well.
An example for the JSON notification is shown below: 

{
	"date":"2017-08-25",
	"time":"11:30:00",
	"metric_name":"cpuTotal",
	"subid":"1ccba0cc92174ce788695cfc0a027b57",
	"value":"91.0500021021063106",
	"message_type":"warning"
}

Accordingly, before the Alarm-Trigger starts working, the API to receive notifications should be ready and available. However, if this API is not prepared when the Alarm-Trigger is working, this fact cannot stop the Alarm-Trigger to proceed ahead. When we would like to launch the Alarm-Trigger later, this API should be defined for this component as an input. 

Step 5- Launching the Alarm-Trigger on a machine such as "211.7.78.63". 

docker run -e AlarmTriggerYMLURL="http://194.249.1.72:5000/AlarmTrigger.yml" -e MONITORING_SERVER="194.249.1.175" -e JSONAlertURL="https://gurujsonrpc.appspot.com/guru" -e JSONAlertURLSIDEGUI="https://gurujsonrpc.appspot.com/guru" -p 4444:8080 salmant/ul_alarm_trigger_container_image:latest

As shown in the above command, if we would like to run the Alarm-Trigger component, different parameters should be set for the following environmental variables: 
	- MONITORING_SERVER: This is the IP address of the Monitoring Server explained in Step 1.
	- AlarmTriggerYMLURL: This is the URL to fetch the YAML file explained in Step 3.
	- JSONAlertURL: This is the URL prepared by an entity which is responsible for an adaptation action. This API is used to receive POST notifications (sent by the Alarm-Trigger) if metrics reach the thresholds explained in Step 4.
	- JSONAlertURLSIDEGUI: This is the URL prepared by the SWITCH Web-based IDE (SIDE). This API is used to receive POST notifications (sent by the Alarm-Trigger) if metrics reach the thresholds explained in Step 4.

Note 1: If the Alarm-Trigger would be running, an API has been prepared to fetch all notifications raised so far. This API is not completely necessary, since the Alarm-Trigger has sent these notifications earlier to other entities explained in previous step. However, it could have its own functionalities. 

http://<AlarmTrigger_IP>:<Port>/logfile.jsp

Note 2: Moreover, another API has been implemented to empty the log file which consists of alarm notifications. 

http://<AlarmTrigger_IP>:<Port>/emptylogfile.jsp

Note 3: The Dockerfile to make the Alarm-trigger container image is as follows:

https://github.com/salmant/ASAP/blob/master/SWITCH-Alarm-Trigger/Dockerfile.centos

Note 4: The Alarm-trigger component container image is publically released on Docker Hub: 

https://hub.docker.com/r/salmant/ul_alarm_trigger_container_image/

