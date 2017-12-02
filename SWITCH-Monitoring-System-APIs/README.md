The Monitoring System allows all external entities to access the monitoring information stored in the Cassandra TSDB in a unified way via prepared APIs. 
***
**API1: Fetching the list of Containers.**
<br />Method: GET
<br />Description: Each container being monitored can be recognized by a unique ID called agentID. This API gives the list of all monitored containers and their information including IP, status and name. The status can be different as follows: (I) UP: The container is running and sending measured values to the Monitoring Server. (II) DOWN: If a container stops working, after a while (a certain period) the Monitoring Server will put "DOWN" for the container's stat automatically. Because, the container has not sent any value for that certain period of time. (III) TERMINATED: After DOWN status, again if the container would not send any value for a certain period of time, the Monitoring Server will put "TERMINATED" for the container's stat automatically.
<br />Input parameters: None
<br />Template: <br />`http://<MONITORING_SERVER>:8080/JCatascopia-Web/restAPI/agents/`
<br />Example:	<br />`http://194.249.1.175:8080/JCatascopia-Web/restAPI/agents/`
<br />Result:
{"agents":[{"agentID":"fac87442ef9b4b7c884cb112614ed2bd","agentIP":"200.201.202.203","status":"TERMINATED","agentName":"200.201.202.203"},{"agentID":"2dc0712372f74c2a91cd94efa3945023","agentIP":"194.249.1.72","status":"UP","agentName":"194.249.1.72"},{"agentID":"1937e52f34e941a0977a455830f4f94a","agentIP":"111.201.202.345","status":"TERMINATED","agentName":"111.201.202.345"},{"agentID":"869706bffc8242ffbcb44f3d3dc048f2","agentIP":"200.201.202.203","status":"TERMINATED","agentName":"200.201.202.203"}]}
***
**API2: Fetching the list of metrics being monitored for a specified container.**
<br />Method: GET
<br />Description: This API gives the list of measured metrics being monitored for a container specified via an ID. For each metric, it shows the information e.g. ID of metrics, name of metrics (metric_name), scale of metrics (units), type of metrics (data_type), group name of metrics (metric_group_name) and so on.
<br />Input parameters: agentID which is an ID for a given container
<br />Template: <br />`http://<MONITORING_SERVER>:8080/JCatascopia-Web/restAPI/agents/<agentID>/availableMetrics`
<br />Example: <br />`http://194.249.1.175:8080/JCatascopia-Web/restAPI/agents/2dc0712372f74c2a91cd94efa3945023/availableMetrics`
<br />Result: 
{"metrics":[{"metricID":"2dc0712372f74c2a91cd94efa3945023:arch","name":"arch","units":"","type":"STRING","group":"StaticInfo"},{"metricID":"2dc0712372f74c2a91cd94efa3945023:btime","name":"btime","units":"","type":"STRING","group":"StaticInfo"},{"metricID":"2dc0712372f74c2a91cd94efa3945023:cpuNum","name":"cpuNum","units":"","type":"STRING","group":"StaticInfo"},{"metricID":"2dc0712372f74c2a91cd94efa3945023:cpuTotal","name":"cpuTotal","units":"percent","type":"DOUBLE","group":"CPU"},{"metricID":"2dc0712372f74c2a91cd94efa3945023:memTotal","name":"memTotal","units":"KB","type":"DOUBLE","group":"Memory"},{"metricID":"2dc0712372f74c2a91cd94efa3945023:os","name":"os","units":"","type":"STRING","group":"StaticInfo"}]}
***
**API3: Fetching the last value of a metric.**
<br />Method: GET
<br />Description: This API gives the last value of a metric specified via metricID.
<br />Input parameters: MetricID (which represents  “agentid”+“metric_name”)
<br />Template: <br />`http://<MONITORING_SERVER>:8080/JCatascopia-Web/restAPI/metrics/<metricID>`
<br />Example: <br />`http://194.249.1.175:8080/JCatascopia-Web/restAPI/metrics/2dc0712372f74c2a91cd94efa3945023:cpuTotal`
<br />Result:
{"metricID":"2dc0712372f74c2a91cd94efa3945023:cpuTotal", "values":[{"metricID":"2dc0712372f74c2a91cd94efa3945023:cpuTotal","name":"cpuTotal","units":"percent","type":"DOUBLE","group":"CPU","value":"7950","timestamp":"14:19:56"}]}
***
**API4: Fetching the list of containers in a Subscription Cluster.**
<br />Method: GET
<br />Description: Each Subscription Cluster has a unique id which is called "subid". This API gives the list of containers (their ID (agentID) and their IP (agentIP)) which are the members of a Subscription Cluster specified via a given ID (subid).
<br />Input parameters: subid
<br />Template: <br />`http://<MONITORING_SERVER>:8080/JCatascopia-Web/restAPI/subscriptions/<subid>/agents`
<br />Example: <br />`http://194.249.1.175:8080/JCatascopia-Web/restAPI/subscriptions/1ccba0cc92174ce788695cfc0a027b57/agents`
<br />Result: 
{"agents":[{"agentID":"2dc0712372f74c2a91cd94efa3945023","agentIP":"194.249.1.72"}]}
***
**API5: Fetching the values for a specific metric taken from the first time to the second time.**
<br />Method: GET
<br />Description: This API gives all values of a metric specified via metricID for a certain period. In other words, values measured during a certain period of time (between "firstTime" and "secondTime").
<br />Input parameters: MetricID (which represents  “agentid”+“metric_name”), firstTime and secondTime
<br />Template: <br />`http://<MONITORING_SERVER>:8080/JCatascopia-Web/restAPI/metrics/<metricID>?firstTime=<firstTim>&secondTime=<secondTime>`
<br />Example: <br />`http://35.187.127.38:8080/JCatascopia-Web/restAPI/metrics/5dd5e346e5b243a0a783b2d225a6b0f8:cpuTotal?firstTime=2017-11-23%2013:10:00&secondTime=2017-11-23%2013:11:04`
<br />Result: 
{"metricID":"5dd5e346e5b243a0a783b2d225a6b0f8:cpuTotal", "values":[{"metricID":"5dd5e346e5b243a0a783b2d225a6b0f8:cpuTotal","name":"cpuTotal","units":"%","type":"DOUBLE","group":"CPU","value":"5.443548387096774","timestamp":"13:10:49"},{"metricID":"5dd5e346e5b243a0a783b2d225a6b0f8:cpuTotal","name":"cpuTotal","units":"%","type":"DOUBLE","group":"CPU","value":"5.37525354969574","timestamp":"13:10:19"}]}`
***
**API6: Fetching the last "n" values for a specific metric that is being monitored.**
<br />Method: GET
<br />Description: This API gives the last "n" values of a metric specified via metricID.
<br />Input parameters: MetricID (which represents  “agentid”+“metric_name”), date (eventDate), n (how many of the last measurements)
<br />Template: <br />`http://<MONITORING_SERVER>:8080/JCatascopia-Web/restAPI/metrics/<metricID>?eventDate=<eventDate>&lastRows=<n>`
<br />Example: <br />`http://35.187.127.38:8080/JCatascopia-Web/restAPI/metrics/5dd5e346e5b243a0a783b2d225a6b0f8:cpuTotal?eventDate=2017-12-01&lastRows=10`
<br />Result: 
{"metricID":"5dd5e346e5b243a0a783b2d225a6b0f8:cpuTotal", "values":[{"metricID":"5dd5e346e5b243a0a783b2d225a6b0f8:cpuTotal","name":"cpuTotal","units":"%","type":"DOUBLE","group":"CPU","value":"5.549949545913218","timestamp":"23:59:51"},{"metricID":"5dd5e346e5b243a0a783b2d225a6b0f8:cpuTotal","name":"cpuTotal","units":"%","type":"DOUBLE","group":"CPU","value":"4.919678714859438","timestamp":"23:59:31"},{"metricID":"5dd5e346e5b243a0a783b2d225a6b0f8:cpuTotal","name":"cpuTotal","units":"%","type":"DOUBLE","group":"CPU","value":"6.633165829145728","timestamp":"23:59:01"},{"metricID":"5dd5e346e5b243a0a783b2d225a6b0f8:cpuTotal","name":"cpuTotal","units":"%","type":"DOUBLE","group":"CPU","value":"5.421686746987952","timestamp":"23:58:31"},{"metricID":"5dd5e346e5b243a0a783b2d225a6b0f8:cpuTotal","name":"cpuTotal","units":"%","type":"DOUBLE","group":"CPU","value":"5.639476334340383","timestamp":"23:58:11"},{"metricID":"5dd5e346e5b243a0a783b2d225a6b0f8:cpuTotal","name":"cpuTotal","units":"%","type":"DOUBLE","group":"CPU","value":"5.94758064516129","timestamp":"23:57:51"},{"metricID":"5dd5e346e5b243a0a783b2d225a6b0f8:cpuTotal","name":"cpuTotal","units":"%","type":"DOUBLE","group":"CPU","value":"4.824120603015075","timestamp":"23:57:21"},{"metricID":"5dd5e346e5b243a0a783b2d225a6b0f8:cpuTotal","name":"cpuTotal","units":"%","type":"DOUBLE","group":"CPU","value":"6.55241935483871","timestamp":"23:57:01"},{"metricID":"5dd5e346e5b243a0a783b2d225a6b0f8:cpuTotal","name":"cpuTotal","units":"%","type":"DOUBLE","group":"CPU","value":"5.840886203423969","timestamp":"23:56:31"},{"metricID":"5dd5e346e5b243a0a783b2d225a6b0f8:cpuTotal","name":"cpuTotal","units":"%","type":"DOUBLE","group":"CPU","value":"7.388663967611336","timestamp":"23:56:01"}]}`
