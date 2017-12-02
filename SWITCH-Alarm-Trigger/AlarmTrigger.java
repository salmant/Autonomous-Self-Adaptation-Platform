import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.policies.ConstantReconnectionPolicy;
import com.datastax.driver.core.policies.DowngradingConsistencyRetryPolicy;

import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.net.*;
import java.io.*;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import com.google.gson.Gson;

import java.util.ArrayList;

class Alarm {
	public String date;
	public String time; 
	public String metric_name;
	public String subid;
	public String value;
	public String message_type;
	public Alarm (String date, String time, String metric_name, String subid, String value, String message_type){
		this.date = date;
		this.time = time; 
		this.metric_name = metric_name;
		this.subid = subid;
		this.value = value;
		this.message_type = message_type;
	}
}

class Metric {
	public String type;
	public String metric_group_name;
	public String subid;
	public String metric_name;
	public String data_type;
	public String action;
	public String unit;
	public int period;
	public double minimum;
	public double maximum;
	public double warning_value;
	public String warning_operator;
	public double critical_value;
	public String critical_operator;
	public Metric(String type, String metric_group_name, String subid, String metric_name, String data_type, String action, String unit, int period, double minimum, double maximum, double warning_value, String warning_operator, double critical_value, String critical_operator) {
		this.type = type;
		this.metric_group_name = metric_group_name;
		this.subid = subid;
		this.metric_name = metric_name;
		this.data_type = data_type;
		this.action = action;
		this.unit = unit;
		this.period = period;
		this.minimum = minimum;
		this.maximum = maximum;
		this.warning_value = warning_value;
		this.warning_operator = warning_operator;
		this.critical_value = critical_value;
		this.critical_operator = critical_operator;
	}
	public String get_type() { return type; }
	public String get_metric_group_name() { return metric_group_name; }
	public String get_subid() { return subid; }
	public String get_metric_name() { return metric_name; }
	public String get_data_type() { return data_type; }
	public String get_action() { return action; }
	public String get_unit() { return unit; }
	public int get_period() { return period; }
	public double get_minimum() { return minimum; }
	public double get_maximum() { return maximum; }
	public double get_warning_value() { return warning_value; }
	public String get_warning_operator() { return warning_operator; }
	public double get_critical_value() { return critical_value; }
	public String get_critical_operator() { return critical_operator; }
}
/////////////////////////////////////////////
class CheckingMetric extends Thread implements Runnable {
	public String TSDB_Server_IP; 
	public String TSDB_Server_Username; 
	public String TSDB_Server_Password; 
	public String alerts_txt_file; 
	public String json_sending_url;
	public String json_sending_url_GUI;
	
	public String subid;
	public String metric_name;
	public String action;
	public int period;
	public double minimum;
	public double maximum;
	public double warning_value;
	public String warning_operator;
	public double critical_value;
	public String critical_operator;
	
	public CheckingMetric(String TSDB_Server_IP, String TSDB_Server_Username, String TSDB_Server_Password, String alerts_txt_file, String json_sending_url, String json_sending_url_GUI, String subid, String metric_name, String action, int period, double minimum, double maximum, double warning_value, String warning_operator, double critical_value, String critical_operator) {
        this.TSDB_Server_IP=TSDB_Server_IP; 
		this.TSDB_Server_Username=TSDB_Server_Username; 
		this.TSDB_Server_Password=TSDB_Server_Password; 
		this.alerts_txt_file=alerts_txt_file; 
		this.json_sending_url=json_sending_url;
		this.json_sending_url_GUI=json_sending_url_GUI;
		
		this.subid = subid;
		this.metric_name = metric_name;
		this.action = action;
		this.period = period;
		this.minimum = minimum;
		this.maximum = maximum;
		this.warning_value = warning_value;
		this.warning_operator = warning_operator;
		this.critical_value = critical_value;
		this.critical_operator = critical_operator;
    }
	//////////
	public void run() {
		try {
			while(true){
				long lasttime = System.currentTimeMillis(); 
				DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				Date date = new Date(); 
				String event_date = dateFormat.format(date);
				event_date = event_date.substring(0, 10);
				/////////////////////////////////////
				double RESULT=0.0;
				if (action.equals("average")) RESULT = Calculate_AVG(metric_name, subid, event_date);
				else if (action.equals("maximum")) RESULT = Calculate_MAX(metric_name, subid, event_date);
				else if (action.equals("minimum")) RESULT = Calculate_MIN(metric_name, subid, event_date);
				switch(warning_operator) {
					case "<":
						// Statements
						if (RESULT < warning_value) notify_alert(dateFormat.format(date), metric_name, subid, RESULT, "warning");
						break; 
					case "<=":
						// Statements
						if (RESULT <= warning_value) notify_alert(dateFormat.format(date), metric_name, subid, RESULT, "warning");
						break; 
					case ">":
						// Statements
						if (RESULT > warning_value) notify_alert(dateFormat.format(date), metric_name, subid, RESULT, "warning");
						break; 
					case ">=":
						// Statements
						if (RESULT >= warning_value) notify_alert(dateFormat.format(date), metric_name, subid, RESULT, "warning");
						break; 
					case "==":
						// Statements
						if (RESULT == warning_value) notify_alert(dateFormat.format(date), metric_name, subid, RESULT, "warning");
						break; 
					case "!=":
						// Statements
						if (RESULT != warning_value) notify_alert(dateFormat.format(date), metric_name, subid, RESULT, "warning");
						break; 
				}
				switch(critical_operator) {
					case "<":
						// Statements
						if (RESULT < critical_value) notify_alert(dateFormat.format(date), metric_name, subid, RESULT, "critical");
						break; 
					case "<=":
						// Statements
						if (RESULT <= critical_value) notify_alert(dateFormat.format(date), metric_name, subid, RESULT, "critical");
						break; 
					case ">":
						// Statements
						if (RESULT > critical_value) notify_alert(dateFormat.format(date), metric_name, subid, RESULT, "critical");
						break; 
					case ">=":
						// Statements
						if (RESULT >= critical_value) notify_alert(dateFormat.format(date), metric_name, subid, RESULT, "critical");
						break; 
					case "==":
						// Statements
						if (RESULT == critical_value) notify_alert(dateFormat.format(date), metric_name, subid, RESULT, "critical");
						break; 
					case "!=":
						// Statements
						if (RESULT != critical_value) notify_alert(dateFormat.format(date), metric_name, subid, RESULT, "critical");
						break; 
				}
				///////////////
				long curtime = System.currentTimeMillis(); 
				Thread.sleep((period * 1000) - (curtime-lasttime));
			}
		} catch(Exception e) { e.printStackTrace(); }
	}
	//////////
	public double Calculate_AVG(String metric_name, String subid, String event_date) { 
		Cluster cluster;
		Session session;
		cluster = Cluster.builder().addContactPoints(TSDB_Server_IP).withCredentials
					(TSDB_Server_Username, TSDB_Server_Password).withPort(9042).withRetryPolicy
					(DowngradingConsistencyRetryPolicy.INSTANCE).withReconnectionPolicy(new
					ConstantReconnectionPolicy(1000L)).build();
		session = cluster.connect("jcatascopiadb");
		// find the agents in the cluster
		ResultSet results = session.execute("SELECT agentid FROM jcatascopiadb.subscription_agents_table WHERE subid=\'"+ subid +"\'");
		double AVG = 0.0;
		double count = 0.0;
		for (Row row : results) {
			String AgentIDs = new  String();
			AgentIDs = row.getString("agentid");
			AgentIDs = AgentIDs + ":" + metric_name;
			ResultSet results2 = session.execute("select value, toTimestamp(event_timestamp) AS ET from jcatascopiadb.metric_value_table where metricid=\'"+AgentIDs+"\' and event_date=\'"+ event_date +"\' ORDER BY event_timestamp DESC LIMIT 1");
			for (Row row2 : results2) {
				AVG = AVG + Double.parseDouble(row2.getString("value"));
				count = count + 1;
			}
		}
		AVG = AVG / count;
		return AVG;
	}
	//////////
	public double Calculate_MAX(String metric_name, String subid, String event_date) { 
		Cluster cluster;
		Session session;
		cluster = Cluster.builder().addContactPoints(TSDB_Server_IP).withCredentials
					(TSDB_Server_Username, TSDB_Server_Password).withPort(9042).withRetryPolicy
					(DowngradingConsistencyRetryPolicy.INSTANCE).withReconnectionPolicy(new
					ConstantReconnectionPolicy(1000L)).build();
		session = cluster.connect("jcatascopiadb");
		// find the agents in the cluster
		ResultSet results = session.execute("SELECT agentid FROM jcatascopiadb.subscription_agents_table WHERE subid=\'"+ subid +"\'");
		double MAX = -Double.MAX_VALUE;
		for (Row row : results) {
			String AgentIDs = new  String();
			AgentIDs = row.getString("agentid");
			AgentIDs = AgentIDs + ":" + metric_name;
			ResultSet results2 = session.execute("select value, toTimestamp(event_timestamp) AS ET from jcatascopiadb.metric_value_table where metricid=\'"+AgentIDs+"\' and event_date=\'"+ event_date +"\' ORDER BY event_timestamp DESC LIMIT 1");
			for (Row row2 : results2) {
				if (Double.parseDouble(row2.getString("value")) > MAX) MAX = Double.parseDouble(row2.getString("value"));
			}
		}
		return MAX;
	}
	//////////
	public double Calculate_MIN(String metric_name, String subid, String event_date) { 
		Cluster cluster;
		Session session;
		cluster = Cluster.builder().addContactPoints(TSDB_Server_IP).withCredentials
					(TSDB_Server_Username, TSDB_Server_Password).withPort(9042).withRetryPolicy
					(DowngradingConsistencyRetryPolicy.INSTANCE).withReconnectionPolicy(new
					ConstantReconnectionPolicy(1000L)).build();
		session = cluster.connect("jcatascopiadb");
		// find the agents in the cluster
		ResultSet results = session.execute("SELECT agentid FROM jcatascopiadb.subscription_agents_table WHERE subid=\'"+ subid +"\'");
		double MIN = Double.MAX_VALUE;
		for (Row row : results) {
			String AgentIDs = new  String();
			AgentIDs = row.getString("agentid");
			AgentIDs = AgentIDs + ":" + metric_name;
			ResultSet results2 = session.execute("select value, toTimestamp(event_timestamp) AS ET from jcatascopiadb.metric_value_table where metricid=\'"+AgentIDs+"\' and event_date=\'"+ event_date +"\' ORDER BY event_timestamp DESC LIMIT 1");
			for (Row row2 : results2) {
				if (Double.parseDouble(row2.getString("value")) < MIN) MIN = Double.parseDouble(row2.getString("value"));
			}
		}
		return MIN;
	}
	//////////
	public void notify_alert(String n_Time, String n_metric_name, String n_subid, double n_AVG, String n_warning_or_critical) throws Exception {
		try{
			/////////////////////////////////////////////////Insert into file locally
			String mainCommand = "echo " + "date: " + n_Time.substring(0, 10) + ", time: " + n_Time.substring(10) + ", metric_name: " + n_metric_name + ", subid: " + n_subid + ", value: " + Double.toString(n_AVG) + ", " + n_warning_or_critical + " >> " + alerts_txt_file;
			////////////
			String[] command={"/bin/bash", "-c", mainCommand};
			Process P = Runtime.getRuntime().exec(command);
			P.waitFor();
			BufferedReader StdInput = new BufferedReader(new InputStreamReader(P.getInputStream()));
			String TopS ="";
			while((TopS= StdInput.readLine())!=null){}
			//////////////////////////////////////////////////call API
			Alarm alarm1 = new Alarm(n_Time.substring(0, 10), n_Time.substring(10), n_metric_name, n_subid, Double.toString(n_AVG), n_warning_or_critical);
			String postUrl = json_sending_url;// put in your url
			Gson gson = new Gson();
			HttpClient httpClient = HttpClientBuilder.create().build();
			HttpPost post = new HttpPost(postUrl);
			StringEntity postingString = new StringEntity(gson.toJson(alarm1));
			post.setEntity(postingString);
			post.setHeader("Content-type", "application/json");
			HttpResponse  response = httpClient.execute(post);
			//////////////////////////////////////////////////
			Alarm alarm2 = new Alarm(n_Time.substring(0, 10), n_Time.substring(10), n_metric_name, n_subid, Double.toString(n_AVG), n_warning_or_critical);
			String postUrl2 = json_sending_url_GUI;// put in your url
			Gson gson2 = new Gson();
			HttpClient httpClient2 = HttpClientBuilder.create().build();
			HttpPost post2 = new HttpPost(postUrl2);
			StringEntity postingString2 = new StringEntity(gson2.toJson(alarm2));
			post2.setEntity(postingString2);
			post2.setHeader("Content-type", "application/json");
			HttpResponse  response2 = httpClient2.execute(post2);
			//////////////////////////////////////////////////
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	//////////
}
/////////////////////////////////////////////
public class AlarmTrigger {
	public static ArrayList<Metric> ListOfMetrics = new ArrayList<Metric>();
	public static void main(String[] args) throws Exception {
		try {
			ReadMetricsFromYAMLFile(args[0]); 
			for (int i=0; i<ListOfMetrics.size(); i++) {
				Metric temp = ListOfMetrics.get(i);
				CheckingMetric aCheckingMetric = new CheckingMetric(args[1], args[2], args[3], args[4], args[5], args[6], temp.get_subid(), temp.get_metric_name(), temp.get_action(), temp.get_period(), temp.get_minimum(), temp.get_maximum(), temp.get_warning_value(), temp.get_warning_operator(), temp.get_critical_value(), temp.get_critical_operator());
				aCheckingMetric.start();
			}
		} catch (Exception e) {throw e;}
	}
	//////////
	public static void ReadMetricsFromYAMLFile(String aURL) throws Exception {
		try {
			///////////////////
			BufferedReader in;
			if (aURL.toLowerCase().contains("http")) { 
				URL file = new URL(aURL);
				in = new BufferedReader(new InputStreamReader(file.openStream()));
			} else { 
				File file = new File(aURL);
				FileReader fileReader = new FileReader(file);
				in = new BufferedReader(fileReader);
				StringBuffer stringBuffer = new StringBuffer();
			}
			///////////////////
			String line;
			while ((line = in.readLine()) != null){
				line = line.replaceAll("\\s+","");
				line = line.replaceAll("\"","");
				if(line.startsWith("metric")) {
					   String type = "";
					   String metric_group_name = "";
					   String subid = "";
					   String metric_name = "";
					   String data_type = "";
					   String action = "";
					   String unit = "";
					   int period = 0;
					   double minimum = 0.0;
					   double maximum = 0.0;
					   double warning_value = 0.0;
					   String warning_operator = "";
					   double critical_value = 0.0;
					   String critical_operator;
					while ((line = in.readLine()) != null){
						line = line.replaceAll("\\s+","");
						line = line.replaceAll("\"","");
						if(line.startsWith("type:")) {line=line.replace("type:", ""); type=line;}
						else if(line.startsWith("metric_group_name:")) {line=line.replace("metric_group_name:", ""); metric_group_name=line;}
						else if(line.startsWith("subid:")) {line=line.replace("subid:", ""); subid=line;}
						else if(line.startsWith("metric_name:")) {line=line.replace("metric_name:", ""); metric_name=line;}
						else if(line.startsWith("data_type:")) {line=line.replace("data_type:", ""); data_type=line;}
						else if(line.startsWith("action:")) {line=line.replace("action:", ""); action=line;}
						else if(line.startsWith("unit:")) {line=line.replace("unit:", ""); unit=line;}
						else if(line.startsWith("period:")) {line=line.replace("period:", ""); period=Integer.parseInt(line);}//
						else if(line.startsWith("minimum:")) {line=line.replace("minimum:", ""); minimum=Double.parseDouble(line);}//
						else if(line.startsWith("maximum:")) {line=line.replace("maximum:", ""); maximum=Double.parseDouble(line);}//
						else if(line.startsWith("warning_value:")) {line=line.replace("warning_value:", ""); warning_value=Double.parseDouble(line);}//
						else if(line.startsWith("warning_operator:")) {line=line.replace("warning_operator:", ""); warning_operator=line;}
						else if(line.startsWith("critical_value:")) {line=line.replace("critical_value:", ""); critical_value=Double.parseDouble(line);}//
						else if(line.startsWith("critical_operator:")) {
								line=line.replace("critical_operator:", ""); critical_operator=line; 
								Metric temp = new Metric(type, metric_group_name, subid, metric_name, data_type, action, unit, period, minimum, maximum, warning_value, warning_operator, critical_value, critical_operator);
								ListOfMetrics.add(temp);
								break;
							 }
					}
				}
			}
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	//////////
}

