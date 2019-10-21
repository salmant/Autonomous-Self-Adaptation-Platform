# Autonomous Self-Adaptation Platform (ASAP)

The Autonomous Self-Adaptation Platform (ASAP) is designed and developed to build scalable complex systems. It includes different components:
* Monitoring Agent, which is implemented by StatsD available for C/C++, Python and Java. It is a container-based component able to measure infrastructure (e.g. CPU, memory, disk, bandwidth, etc.), network (e.g. delay, jitter, throughput and packet loss) and application (e.g. response time, etc.).
https://github.com/salmant/Autonomous-Self-Adaptation-Platform/tree/master/SWITCH-Monitoring-System
* Monitoring Server containerised includes a data collection and aggregation system by both Push or Pull mode implemented in Java, a Cassandra TSDB and a Web-based GUI. It also provides a set of RESTful APIs to fetch and show all stored information.
https://github.com/salmant/Autonomous-Self-Adaptation-Platform/tree/master/SWITCH-Monitoring-System-APIs
* Alarm-Trigger implemented in Java reads a YAML file as input and sends JSON-based POST notifications as QoS degradation alerts.
https://github.com/salmant/Autonomous-Self-Adaptation-Platform/tree/master/SWITCH-Alarm-Trigger
* Self-Adapter increases or decreases the number of running containers depending on workload variations at runtime. Furthermore, it dynamically configures the HAProxy Load-Balancer.
https://github.com/salmant/Autonomous-Self-Adaptation-Platform/tree/master/SWITCH-Self-Adapter

![Image](https://media-exp1.licdn.com/media-proxy/ext?w=800&h=800&f=n&hash=WlzlpYOo6Zp%2BSw4ktaKIY3RB05c%3D&ora=1%2CaFBCTXdkRmpGL2lvQUFBPQ%2CxAVta5g-0R6jnhodx1Ey9KGTqAGj6E5DQJHUA3L0CHH05IbfPWi6KMTZeOOh9kASfSoHjQBgeO-1STS1R47tKti7KN1w3cS2IZn5agYUbhl4j3lK6w)


## Recommended Document
<br />`“Dynamic Multi-level Auto-scaling Rules for Containerized Applications”, The Computer Journal, Oxford University Press. doi: 10.1093/comjnl/bxy043`

https://doi.org/10.1093/comjnl/bxy043

## Contact
I would like to hear from people using the Autonomous Self-Adaptation Platform (ASAP). If you have any question or you would like to discuss any part of the system, please send an email to Salman.Taherizadeh@ijs.si 

## Licence
The SWITCH Monitoring System is published under the ![Apache 2 license](https://github.com/salmant/ASAP/blob/master/LICENSE).
