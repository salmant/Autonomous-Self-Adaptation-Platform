import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Host;
import com.datastax.driver.core.Metadata;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.policies.ConstantReconnectionPolicy;
import com.datastax.driver.core.policies.DowngradingConsistencyRetryPolicy;
import static java.lang.System.out;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;

import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.io.File;
import java.io.FileReader;

public class HPA {
	public static void main(String[] args)  throws Exception {
		///////////////////
		String subid="1ccba0cc92174ce788695cfc0a027b57"; //id of the cluster
		String haproxy_agentid="f7a1ed4f3bd94ce3b942dbb75477a806";
		String event_date=new SimpleDateFormat("yyyy-MM-dd").format(new Date());
		double CPU_threshold = 80; // %80
		int num_Servers = 12;
		String[][] server_IP = new String[num_Servers][3];
		for (int j=0; j<num_Servers; j++) server_IP[j][1] = "FREE";
		server_IP[0][0] = "194.249.1.72";
		server_IP[1][0] = "194.249.1.93";
		server_IP[2][0] = "194.249.1.102";
		server_IP[3][0] = "194.249.1.128";
		server_IP[4][0] = "194.249.1.143";
		server_IP[5][0] = "194.249.1.46";
		server_IP[6][0] = "194.249.0.44";
		server_IP[7][0] = "194.249.1.28";
		server_IP[8][0] = "194.249.0.243";
		server_IP[9][0] = "194.249.1.43";
		server_IP[10][0] = "194.249.1.42";
		server_IP[11][0] = "194.249.1.76";
		///////////////////
		while(true){
		try {
			///////////////////
			long lasttime = System.currentTimeMillis(); 
			///////////////////
			int inc = 0;
			int dec = 0;
			///////////////////
			System.out.println("---------------------------------"); 
			DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSS");
			Date date = new Date(); 
			System.out.println("Time: " + dateFormat.format(date));
			double AVG_CPU = Calculate_AVG("cpuTotal", subid, event_date);
			double AVG_Mem = Calculate_AVG("memUsedPercent", subid, event_date);
			System.out.println("AVG_CPU_Percentage: " + AVG_CPU); 
			System.out.println("AVG_Mem_Percentage: " + AVG_Mem); 
			double AVG_response_time = Fetch_AVG_response_time_metric ("avgResponseTime", haproxy_agentid, event_date); 
			System.out.println("AVG_response_time: " + AVG_response_time); 
			int containers = How_many_existing_servers(); 
			System.out.println("#containers: " + containers); 
			double GoodPut_current = Fetch_request_rate_metric ("requestRate", haproxy_agentid, event_date); 
			GoodPut_current = GoodPut_current / containers; 
			System.out.println("GoodPut_current: " + GoodPut_current); 
			add_to_csv_file(dateFormat.format(date), AVG_CPU, AVG_Mem, AVG_response_time, containers, GoodPut_current);
			///////////////////
			int NoP = 0; // number of necessary pods
			NoP = (int) Math.ceil((AVG_CPU * containers) / CPU_threshold);
			if (containers < NoP){
				inc = NoP - containers;
				System.out.println("#inc: " + inc); 
				///////////////////
				for (int i=0; i<inc; i++){
					int j=0;
					for (j=0; j<num_Servers; j++){
						if (server_IP[j][1].equals("FREE")){
							server_IP[j][1] = String.valueOf(containers+(i+1)); // "occupied" 
							Run_a_Container server = new Run_a_Container(server_IP, j, server_IP[j][0], subid, String.valueOf(containers+(i+1)));
							server.start();
							System.out.println("--->>>"+server_IP[j][2]);
							break;
						}
					}
					if (j==num_Servers) break; // all servers are occupied.
				}
			}
			///////////////////////////////////
			if (containers > NoP){
				dec =  containers - NoP;
				if ((dec >= 1) && (containers != 1)) {
					System.out.println("#dec: " + dec); 
					for (int i=0; i<dec; i++){
						int max_num_in_ip_array = 1;
						for (int k = 0; k < num_Servers; k++) if ((!(server_IP[k][1].equals("FREE"))) && (Integer.parseInt(server_IP[k][1])>max_num_in_ip_array)) max_num_in_ip_array=Integer.parseInt(server_IP[k][1]);
						if (max_num_in_ip_array==1) break; // there is just one running container
						for (int j=0; j<num_Servers; j++){
							if (server_IP[j][1].equals(String.valueOf(max_num_in_ip_array))){
								server_IP[j][1] = "FREE"; 
								Terminate_a_Container server = new Terminate_a_Container(server_IP[j][0], subid, String.valueOf(max_num_in_ip_array),server_IP[j][2]);
								server.run_start();
								break;
							}
						}
					}
				}
			}
			System.out.println("---------------------------------"); 
			long curtime = System.currentTimeMillis(); 
			Thread.sleep(Math.abs(30000 - (curtime-lasttime)));	
		} catch (Exception e) {throw e;}
		} // end of while(true)
	}
	/////////////////////////////////////////////
	public static double Fetch_AVG_response_time_metric(String metric_name, String haproxy_agentid, String event_date) {
		Cluster cluster;
		Session session;
		cluster = Cluster.builder().addContactPoints("194.249.1.175").withCredentials
                    ("catascopia_user", "catascopia_pass").withPort(9042).withRetryPolicy
                    (DowngradingConsistencyRetryPolicy.INSTANCE).withReconnectionPolicy(new
                    ConstantReconnectionPolicy(1000L)).build();
		session = cluster.connect("jcatascopiadb");
		haproxy_agentid = haproxy_agentid + ":" + metric_name;
		ResultSet results = session.execute("select value from jcatascopiadb.metric_value_table where metricid=\'"+haproxy_agentid+"\' and event_date=\'"+ event_date +"\' ORDER BY event_timestamp DESC LIMIT 1");
		double AVG_response_time_metric = 0.0;
		for (Row row : results) {
			AVG_response_time_metric = Double.parseDouble(row.getString("value"));
		}
		return Math.abs(AVG_response_time_metric);
	}
	/////////////////////////////////////////////
	public static double Fetch_request_rate_metric(String metric_name, String haproxy_agentid, String event_date) {
		Cluster cluster;
		Session session;
		cluster = Cluster.builder().addContactPoints("194.249.1.175").withCredentials
                    ("catascopia_user", "catascopia_pass").withPort(9042).withRetryPolicy
                    (DowngradingConsistencyRetryPolicy.INSTANCE).withReconnectionPolicy(new
                    ConstantReconnectionPolicy(1000L)).build();
		session = cluster.connect("jcatascopiadb");
		haproxy_agentid = haproxy_agentid + ":" + metric_name;
		ResultSet results = session.execute("select value from jcatascopiadb.metric_value_table where metricid=\'"+haproxy_agentid+"\' and event_date=\'"+ event_date +"\' ORDER BY event_timestamp DESC LIMIT 1");
		double GoodPut = 0.0;
		for (Row row : results) {
			GoodPut = Double.parseDouble(row.getString("value"));
		}
		return Math.abs(GoodPut);
	}
	/////////////////////////////////////////////
	public static double Calculate_AVG(String metric_name, String subid, String event_date) {
		Cluster cluster;
		Session session;
		cluster = Cluster.builder().addContactPoints("194.249.1.175").withCredentials
                    ("catascopia_user", "catascopia_pass").withPort(9042).withRetryPolicy
                    (DowngradingConsistencyRetryPolicy.INSTANCE).withReconnectionPolicy(new
                    ConstantReconnectionPolicy(1000L)).build();
		session = cluster.connect("jcatascopiadb");
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
	/////////////////////////////////////////////
	public static int How_many_existing_servers() {
		int i=0; 
		String haproy_ip = "194.249.1.110";
        String haproy_host_user = "root";
        String haproy_host_password = "********************";
		try{
			String command = "cat /etc/haproxy/haproxy.cfg";
            JSch jsch = new JSch();
            com.jcraft.jsch.Session session = jsch.getSession(haproy_host_user, haproy_ip, 22);
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);;
            session.setPassword(haproy_host_password);
            session.connect();
            Channel channel = session.openChannel("exec");
            ((ChannelExec)channel).setCommand(command);
            channel.setInputStream(null);
            ((ChannelExec)channel).setErrStream(System.err);
            InputStream input = channel.getInputStream();
            channel.connect();
            try{
                InputStreamReader inputReader = new InputStreamReader(input);
                BufferedReader bufferedReader = new BufferedReader(inputReader);
                String line = null;
                while((line = bufferedReader.readLine()) != null){
                    if (line.contains("-www")) i++;
					//System.out.println(line);
                }
                bufferedReader.close();
                inputReader.close();
            }catch(IOException ex){
                ex.printStackTrace();
				return i;
            }
            channel.disconnect();
            session.disconnect();
			return i;
        }catch(Exception ex){
            ex.printStackTrace();
			return i;
        }
	}
	/////////////////////////////////////////////
	public static void add_to_csv_file(String n_Time, double n_AVG_CPU, double n_AVG_Mem, double n_AVG_response_time, int n_containers, double n_GoodPut_current){
		try{
			String mainCommand = "echo " + n_Time.substring(0, 10) + ", " + n_Time.substring(10) + ", " + Double.toString(n_AVG_CPU) + ", " + Double.toString(n_AVG_Mem) + ", " + Double.toString(n_AVG_response_time) + ", " + String.valueOf(n_containers) + ", " + Double.toString(n_GoodPut_current) + " >> MonitoredData.csv";
			////////////
			String[] command={"/bin/bash", "-c", mainCommand};
			Process P = Runtime.getRuntime().exec(command);
			P.waitFor();
			BufferedReader StdInput = new BufferedReader(new InputStreamReader(P.getInputStream()));
			String TopS ="";
			while((TopS= StdInput.readLine())!=null){
				//System.out.println(TopS); 
			}
		}catch(Exception ex){
			ex.printStackTrace();
			System.out.println("error"); 
		}
	}
	/////////////////////////////////////////////
}