package be.jedi.jvspherecontrol.vsphere;



import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;

import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthState;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.ExecutionContext;

public class VsphereDataStoreClient 
{

	//Username, 
	String user,password;
	//Uri to connect to
	URI serverUri;
	//Default is not to bypass SSL Security, set to true to ignore self-signed certificates
	boolean insecure=false;
	//Overwrite existing files or not
	boolean overwrite=false;
	//Create Subdirectories
	boolean subdirectories=false;

	//Defaults for datastore and datacenter
	String datastore="datastore1";
	String datacenter="ha-datacenter";

	private DefaultHttpClient httpclient;

	public VsphereDataStoreClient(String user, String password,URI serverUri) {
		this.user=user;
		this.password=password;
		this.serverUri=serverUri;

		httpclient = new DefaultHttpClient();
		if (insecure) {
			httpclient=(DefaultHttpClient) VsphereDataStoreClient.wrapClient(httpclient); 
		}
		Credentials defaultcreds = new UsernamePasswordCredentials(user, password);
		HttpProtocolParams.setUserAgent(httpclient.getParams(), "Vmware Datastore Client/0.0.1");

		httpclient.getCredentialsProvider().setCredentials(new AuthScope(serverUri.getHost(),serverUri.getPort(),AuthScope.ANY_REALM), defaultcreds);

	}

	public boolean exists(String path) throws ClientProtocolException, IOException {

		String headurl=serverUri+"/folder/"+path+"?"+serverUri.getQuery();
		System.out.println(headurl);

		HttpHead headmethod=new HttpHead(headurl);

		HttpResponse response = httpclient.execute(headmethod);
		HttpEntity resEntity = response.getEntity();
		int statusCode=response.getStatusLine().getStatusCode();

		return (statusCode==200);
	}

	public void uploadFile(String sourcefile,String destination) throws ClientProtocolException, IOException {

		File fObject = new File(sourcefile);

		String puturl=serverUri+"/folder"+sourcefile+"?dcPath="+datacenter+"&"+"dsName="+datastore;

		HttpPut putmethod=new HttpPut(puturl);
		String path=putmethod.getURI().getRawPath();	 
		System.out.println(path);

		System.out.println(fObject.isDirectory());
		System.out.println(fObject.getParent());

		URI uri =putmethod.getURI();

		InputStreamEntity reqEntity = new InputStreamEntity(new FileInputStream(fObject), fObject.length());

		putmethod.setEntity(reqEntity);

		HttpResponse response2 = httpclient.execute(putmethod);

		HttpEntity resEntity2 = response2.getEntity();

		System.out.println(response2.getStatusLine());

	}


	public void setInsecure(boolean insecure) {
		this.insecure = insecure;
	}


	public void setOverwrite(boolean overwrite) {
		this.overwrite = overwrite;
	}

	private static HttpClient wrapClient(HttpClient base) {
		try {

			//http://theskeleton.wordpress.com/2010/07/24/avoiding-the-javax-net-ssl-sslpeerunverifiedexception-peer-not-authenticated-with-httpclient/
			SSLContext ctx = SSLContext.getInstance("TLS");
			X509TrustManager tm = new X509TrustManager() {

				public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException {
				}

				public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException {
				}

				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}
			};
			ctx.init(null, new TrustManager[]{tm}, null);
			SSLSocketFactory ssf = new SSLSocketFactory(ctx);
			ssf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
			ClientConnectionManager ccm = base.getConnectionManager();
			SchemeRegistry sr = ccm.getSchemeRegistry();
			sr.register(new Scheme("https", ssf, 443));
			return new DefaultHttpClient(ccm, base.getParams());
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
}
