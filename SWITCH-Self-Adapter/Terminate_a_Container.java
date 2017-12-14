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

public class Terminate_a_Container{
	public String new_IP;
	public String subid;
	public String i_str;
	public String containerid;
	public Terminate_a_Container(String str_IP, String sub_id, String str_i, String container_id) {
        new_IP = str_IP;
		subid = sub_id;
		i_str = str_i;
		containerid = container_id;
    }
	public void run_start() {
		try {
			remove_from_haproxy();
			String Id = fetch_agentid_of_container(); 
			remove_from_cluster_in_cassandra(Id); 
			remove_from_AgentTable_in_cassandra(Id); 
			Thread.sleep(500);
			stop_a_container(containerid);
			
		} catch(Exception e) { System.out.println(e.getMessage()); }
	}
	/////////////////////////////////////////////////////////////
	public String fetch_agentid_of_container(){
        try{
			Cluster cluster;
			Session session;
			cluster = Cluster.builder().addContactPoints("194.249.1.175").withCredentials
                    ("catascopia_user", "catascopia_pass").withPort(9042).withRetryPolicy
                    (DowngradingConsistencyRetryPolicy.INSTANCE).withReconnectionPolicy(new
                    ConstantReconnectionPolicy(1000L)).build();
			session = cluster.connect("jcatascopiadb");
			ResultSet results = session.execute("SELECT agentid FROM jcatascopiadb.subscription_agents_table WHERE subid=\'"+ subid +"\' and agentip=\'"+ new_IP +"\' LIMIT 1;");
			String AgentID = new  String();
			for (Row row : results) {
				AgentID = row.getString("agentid");
			}
			return AgentID;
        }catch(Exception ex){
            ex.printStackTrace();
			return "error in fetch_agentid_of_container()";
        }
    }
	/////////////////////////////////////////////////////////////
	public void stop_a_container(String new_container_id){
        try{
			String mainCommand = "curl -X POST http://" + new_IP + ":4243/containers/" + new_container_id + "/stop";
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
			System.out.println("error in stop_a_container()");
        }
    }
	/////////////////////////////////////////////////////////////
	public void remove_from_cluster_in_cassandra(String new_id){
		try{
		Cluster cluster;
		Session session;
		cluster = Cluster.builder().addContactPoints("194.249.1.175").withCredentials
                    ("catascopia_user", "catascopia_pass").withPort(9042).withRetryPolicy
                    (DowngradingConsistencyRetryPolicy.INSTANCE).withReconnectionPolicy(new
                    ConstantReconnectionPolicy(1000L)).build();
		session = cluster.connect("jcatascopiadb");
				ResultSet results = session.execute("DELETE FROM jcatascopiadb.subscription_agents_table WHERE subid=\'" + subid +"\' and agentid=\'" + new_id +"\';");
		}catch(Exception ex){
            ex.printStackTrace();
			System.out.println("error in remove_from_cluster_in_cassandra()");
        }
	}
	/////////////////////////////////////////////////////////////
	public void remove_from_AgentTable_in_cassandra(String new_id){
		try{
		Cluster cluster;
		Session session;
		cluster = Cluster.builder().addContactPoints("194.249.1.175").withCredentials
                    ("catascopia_user", "catascopia_pass").withPort(9042).withRetryPolicy
                    (DowngradingConsistencyRetryPolicy.INSTANCE).withReconnectionPolicy(new
                    ConstantReconnectionPolicy(1000L)).build();
		session = cluster.connect("jcatascopiadb");
				ResultSet results = session.execute("DELETE FROM jcatascopiadb.agent_table WHERE  agentid=\'" + new_id +"\';");
		}catch(Exception ex){
            ex.printStackTrace();
			System.out.println("error in remove_from_AgentTable_in_cassandra()");
        }
	}
	/////////////////////////////////////////////////////////////
	public void remove_from_haproxy(){
		String haproy_ip = "194.249.1.110";
        String haproy_host_user = "root";
        String haproy_host_password = "***************";
		//////////////////////////////// add new server
		try{
			String command = "sed " + "\'/" + "    server " + i_str + "-www " + new_IP + ":5000 check/d" + "\' /etc/haproxy/haproxy.cfg" + " > /etc/haproxy/test.cfg ; mv /etc/haproxy/test.cfg /etc/haproxy/haproxy.cfg";
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
					//System.out.println(line);
                }
                bufferedReader.close();
                inputReader.close();
            }catch(IOException ex){
                ex.printStackTrace();
            }
            channel.disconnect();
            session.disconnect();
        }catch(Exception ex){
            ex.printStackTrace();
			System.out.println("error in remove_from_haproxy() first section"); 
        }
		//////////////////////////////// reload HAProxy
		try{
			String command = "sh /haproxy.sh";
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
					//System.out.println(line);
                }
                bufferedReader.close();
                inputReader.close();
            }catch(IOException ex){
                ex.printStackTrace();
            }
            channel.disconnect();
            session.disconnect();
        }catch(Exception ex){
            ex.printStackTrace();
			System.out.println("error in remove_from_haproxy() second section"); // to be deleted !!!!
        }
	}
	/////////////////////////////////////////////////////////////
}