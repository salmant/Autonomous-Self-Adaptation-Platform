// ------------------------------------------------------------------------
// Author: Salman Taherizadeh
// ------------------------------------------------------------------------
package com.timgroup.statsd;
import com.timgroup.statsd.StatsDClient;
import com.timgroup.statsd.NonBlockingStatsDClient;
import java.util.UUID;
public class MonitoringAgent {
	public static void main(String[] args) {
	try {
		// Defining (1) MONITORING_PREFIX, (2) the Monitoring Adapter's IP address
		StatsDClient statsd = new NonBlockingStatsDClient("eu.switch.beia", "194.249.1.46", 8125);
		// Defining an ID for the container (or for a VM)
		String ID = UUID.randomUUID().toString().replace("-", "");
		// Defining the IP address of the container (or of a VM)
		String IP = "194-249-1-110"; // Defining the Monitoring Agent's IP address
		while(true){
			// metric 1 -> <metric_group_name>:CPU, <metric_name>:cpuTotal, <units>:percent
			long value1 = (int)(Math.random()*100);
			statsd.recordGaugeValue(ID + "." + IP + ".CPU.cpuTotal.percent", value1);
			// metric 2 -> <metric-group-name>:Memory.<metric-name>:memTotal.<units>:percent
			long value2 = (int)(Math.random()*100);
			statsd.recordGaugeValue(ID + "." + IP + ".Memory.memTotal.percent", value2);
			Thread.sleep(10000); // The monitoring interval
		}
	} catch(InterruptedException ex) { Thread.currentThread().interrupt(); }
	}
}
