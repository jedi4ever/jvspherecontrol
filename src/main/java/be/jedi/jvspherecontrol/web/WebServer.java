package be.jedi.jvspherecontrol.web;

import org.eclipse.jetty.server.Server;

public class WebServer {
	
	void startWeb() {
		Server server = new Server(8080);
		try {
			server.start();
			server.join();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


}
