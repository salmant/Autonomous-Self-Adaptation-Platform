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

public class Run_a_Container extends Thread implements Runnable {
	public String new_IP;
	public String subid;
	public String i_str;
	public int j;

	String[][] server_IP = new String[6][3]; 
	public Run_a_Container(String[][] server_IP_here, int j_here, String str_IP, String sub_id, String str_i) {
        server_IP = server_IP_here; 
		j = j_here;
		new_IP = str_IP;
		subid = sub_id;
		i_str = str_i;
    }
	public void run() {
		try {
			String Id = create_a_container();
			server_IP[j][2]=Id;
			start_a_container(Id);
			add_to_haproxy();
			add_to_cluster_in_cassandra();
		} catch(Exception e) { 
			System.out.println(e.getMessage()); System.out.println("error in Run_a_Container -> run_start()"); 
			}
	}
	/////////////////////////////////////////////////////////////
	public String create_a_container(){
        try{
			String mainCommand =
			"	curl -X POST -H \'Content-Type: application/json\' -d \'{	" +
			"	\"Image\":\"salmant/salmancontainerimage:latest\",	" +
			"	\"Env\": [	" +
			"				   \"DOCKER_HOST=" + new_IP + "\"	" +
			"		   ],	" +
			"	\"Tty\": true,	" +
			"	\"PortBindings\": { \"8080/tcp\": [{ \"HostPort\": \"5000\" }] }	" +
			"	}\' http://" + new_IP + ":4243/containers/create	"
			;
			String[] command={"/bin/bash", "-c", mainCommand};
			Process P = Runtime.getRuntime().exec(command);
            P.waitFor();
            BufferedReader StdInput = new BufferedReader(new InputStreamReader(P.getInputStream()));
            String TopS ="";
			TopS= StdInput.readLine();
			TopS = TopS.substring(6);
			TopS = TopS.substring(TopS.indexOf("\"") + 1);
			TopS = TopS.substring(0, TopS.indexOf("\""));
			return TopS;
        }catch(Exception ex){
            System.out.println("error in Run_a_Container -> create_a_container()");
			ex.printStackTrace();
			return "error";
        }
    }
	/////////////////////////////////////////////////////////////
	public void start_a_container(String new_id){
        try{
			String mainCommand = "curl -X POST http://" + new_IP + ":4243/containers/" + new_id + "/start";
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
            System.out.println("error in Run_a_Container -> start_a_container()");
			ex.printStackTrace();
        }
    }
	/////////////////////////////////////////////////////////////
	public void add_to_cluster_in_cassandra(){
		Cluster cluster;
		Session session;
		cluster = Cluster.builder().addContactPoints("194.249.1.175").withCredentials
                    ("catascopia_user", "catascopia_pass").withPort(9042).withRetryPolicy
                    (DowngradingConsistencyRetryPolicy.INSTANCE).withReconnectionPolicy(new
                    ConstantReconnectionPolicy(1000L)).build();
		session = cluster.connect("jcatascopiadb");
		ResultSet results = session.execute("select * from jcatascopiadb.agent_table;");
		for (Row row : results) {
			if ((new_IP.equals(row.getString("agentip")))&&(row.getString("status").equals("UP"))){
				String new_agentid = row.getString("agentid"); 
				ResultSet results2 = session.execute("INSERT INTO jcatascopiadb.subscription_agents_table (subid, agentid, agentip) VALUES (\'" + subid +"\', \'" + new_agentid +"\', \'"+ new_IP +"\');");
			}
		}
	}
	/////////////////////////////////////////////////////////////
	public void add_to_haproxy(){
		String haproy_ip = "194.249.1.110";
        String haproy_host_user = "root";
        String haproy_host_password = "*****************";
		//////////////////////////////// add new server
		try{
			String command = "echo " + "\"" + "    server " + i_str + "-www " + new_IP + ":5000 check" + "\"" + ">> /etc/haproxy/haproxy.cfg";
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
        }
	}
	/////////////////////////////////////////////////////////////
}