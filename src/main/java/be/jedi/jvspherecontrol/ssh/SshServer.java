package be.jedi.jvspherecontrol.ssh;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;


public class SshServer {

	
	String sshUser="kapitein";
	String sshHost="192.168.2.150";
	String sshPassword="12345678";
	String sshKeyFile="";
	String sshKeyPassPhrase;
	
	public SshServer(String sshUser, String sshHost, String sshPassword,
			String sshKeyFile, String sshKeyPassPhrase) {
		super();
		this.sshUser = sshUser;
		this.sshHost = sshHost;
		this.sshPassword = sshPassword;
		this.sshKeyFile = sshKeyFile;
		this.sshKeyPassPhrase = sshKeyPassPhrase;
	}

	
	void  checkSsh() {

		JSch jcsh=new JSch();

		int sshTimeout=30000;
		int port=22;
		try {
			Session session=jcsh.getSession(sshUser, sshHost, port);
			session.setPassword(sshPassword);
			session.setConfig("StrictHostKeyChecking", "no");
			jcsh.addIdentity(sshKeyFile);
			//			jcsh.addIdentity(sshKeyFile,sshKeyPassPhrase);
			session.connect(sshTimeout);

			//			Channel channel=session.openChannel("exec");
			//		    ((ChannelExec)channel).setCommand("who am i");
			//		    channel.setInputStream(null);
			//			channel.setOutputStream(System.out);
			//			channel.connect();
			//			channel.disconnect();
			session.disconnect();

			//com.jcraft.jsch.JSchException:
		} catch (JSchException e) {
			// TODO Auto-generated catch block
			System.out.println("##"+e.getMessage());
			//Auth fail
			e.printStackTrace();
		};

	}
}
