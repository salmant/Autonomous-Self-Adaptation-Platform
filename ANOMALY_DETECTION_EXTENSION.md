# Anomaly Detection Extension for the Autonomous Self-Adaptation Platform

## Overview

This document proposes an anomaly detection extension for the Autonomous Self-Adaptation Platform (ASAP). The aim is to strengthen the existing monitoring and self-adaptation workflow by adding an intelligent layer that can identify abnormal infrastructure, network, and application behaviour before it causes service degradation.

The current ASAP architecture already includes monitoring agents, a monitoring server, an alarm-trigger component, and a self-adapter. This extension builds on those components by introducing anomaly detection between the monitoring layer and the adaptation layer.

## Motivation

Traditional monitoring systems often rely on static thresholds, such as CPU usage above a fixed percentage or response time above a predefined value. While useful, static thresholds may not detect gradual degradation, unusual workload patterns, or cascading failures across distributed components.

An anomaly detection layer can help identify abnormal behaviour based on metric patterns and relationships between system components. This is particularly useful for scalable containerised systems where workload, resource usage, and network conditions change dynamically at runtime.

## Relevant Metrics

The anomaly detection layer can use metrics already collected by the ASAP monitoring components, including:

- CPU utilisation
- Memory usage
- Disk usage
- Network bandwidth
- Network delay
- Jitter
- Throughput
- Packet loss
- Application response time
- Workload intensity
- Number of active containers
- Service-level indicators related to QoS degradation

## Proposed Architecture

This extension delivers an anomaly detection layer between the monitoring server and the self-adapter.

The workflow would be:

Monitoring Agent
      ↓
Monitoring Server
      ↓
Anomaly Detection Layer
      ↓
Alarm Trigger
      ↓
Self-Adapter
      ↓
Container scaling and load-balancer reconfiguration


In this workflow, the monitoring server collects infrastructure, network, and application metrics. The anomaly detection layer analyses these metrics and identifies unusual behaviour. If an anomaly is detected, the alarm-trigger component can generate an alert, and the self-adapter can decide whether an adaptation action is required.
Example Anomaly Scenarios
The following scenarios could be detected by the proposed extension:
1.	Sudden CPU or memory spikes across multiple containers. 
2.	Gradual increase in application response time. 
3.	Abnormal network delay or packet loss affecting service quality. 
4.	Unexpected workload growth that is not yet reflected in static threshold rules. 
5.	Cascading degradation where one overloaded component affects dependent components. 
6.	Repeated scaling events that indicate unstable adaptation behaviour. 

Detection Approaches
Several anomaly detection approaches could be considered:
1. Threshold-Based Detection
This is the simplest method. It detects anomalies when a metric exceeds a predefined value. It is easy to implement but may not be flexible enough for dynamic environments.
2. Statistical Deviation Detection
This method compares current metric values with historical behaviour. For example, a sudden increase in response time or memory usage can be flagged if it deviates significantly from normal patterns.
3. Graph-Based Dependency Analysis
In distributed systems, components are often connected through service dependencies, communication paths, or shared infrastructure. A graph-based approach can model components as nodes and dependencies as edges. This can help detect cascading anomalies and identify which component is likely to be the source of degradation.
4. Hybrid Detection
A practical implementation could combine threshold-based rules with statistical and graph-based analysis. This would allow the platform to detect both simple resource issues and more complex distributed-system anomalies.
Integration with the Self-Adapter
The anomaly detection layer should not replace the existing self-adaptation mechanism. Instead, it should provide additional intelligence to support better adaptation decisions.
For example:
•	If workload increases normally, the self-adapter can scale containers as usual. 
•	If workload increases together with abnormal latency, the platform can trigger a higher-priority adaptation action. 
•	If repeated scaling actions do not improve response time, the platform can flag a possible configuration or dependency issue. 
•	If several connected components degrade at the same time, graph-based analysis can help identify the likely source component. 

Benefits
This extension could improve the ASAP architecture by:
•	Detecting abnormal behaviour earlier. 
•	Reducing reliance on static thresholds. 
•	Supporting more intelligent scaling decisions. 
•	Improving resilience in distributed and containerised systems. 
•	Helping identify cascading failures across dependent components. 
•	Strengthening the link between monitoring, alerting, and self-adaptation. 
Future Work
Future development could include:
•	Implementing a lightweight anomaly scoring function. 
•	Adding graph-based dependency modelling between services. 
•	Integrating anomaly scores with the alarm-trigger component. 
•	Evaluating detection accuracy using synthetic workload changes. 
•	Comparing threshold-based and anomaly-based adaptation decisions. 
•	Extending the monitoring API to expose anomaly scores. 
Summary
The proposed anomaly detection extension strengthens the Autonomous Self-Adaptation Platform by adding an intelligent analysis layer between monitoring and adaptation. This can help the platform move beyond static threshold-based reactions and support more proactive, context-aware self-adaptation in distributed containerised systems.

