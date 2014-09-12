package org.vaadin.mideaas.frontend;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.ws.rs.core.MediaType;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.vaadin.mideaas.model.SharedProject;
import org.vaadin.mideaas.model.User;
import org.vaadin.mideaas.model.UserSettings;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Link;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Notification.Type;

import fi.jasoft.qrcode.QRCode;

@SuppressWarnings("serial")
public class Deployer extends CoapsCaller  {

	public interface DeployListener {
		public void networkingStarted(String msg);
		public void networkingFinished(boolean success, String msg, String uriToService);
		public void networkingCancelled(String msg);
	}
	
	//private static boolean deploying = false;
	private static Map <String, Map<String,User>> deployingProjectsInUris;
	static {

		deployingProjectsInUris = new LinkedHashMap<String, Map<String,User>>();
		Map<String, User> tut = new LinkedHashMap<String, User>();
		Map<String, User> uh = new LinkedHashMap<String, User>();
		deployingProjectsInUris.put("http://130.230.142.89:8080/CF-api/rest", tut);
		deployingProjectsInUris.put("http://easi-clouds.cs.helsinki.fi:8081/CF-api/rest", uh);
	}
	
	private ExecutorService executor = Executors.newSingleThreadExecutor();
	private static DefaultHttpClient httpclient = new DefaultHttpClient();
	private String date;
	private final String paasApiUri;
	private String memory;
	private String deployLocation;
	private String warLocation;
	private String appName;
	private String warName;
	private CopyOnWriteArrayList<DeployListener> listeners = new CopyOnWriteArrayList<>();
	private String pathToWar;
	
	// refers to link component from DeployComponent Class
	private Link linkToApp;
	// refers to link Button component from DeployComponent Class
	private Button buttonLinkToApp;

	private User user = null;

	private Button deployButton = null;
	
    Deployer(String pathToWar, String paasApiUri, Link linkToApp, Button buttonLinkToApp
    		, String apiLocation, User user, Button deployButton){
    	super(apiLocation);
    	//System.out.println("apiLocation in Deployer: " + apiLocation);
    	//System.out.println("apiLocation in CoapsCaller: " + getApiLocation());
    	this.deployButton = deployButton;
    	
    	this.user = user;
    	
    	this.linkToApp = linkToApp;
    	this.buttonLinkToApp = buttonLinkToApp;
    	
    	this.pathToWar = pathToWar;
    	this.paasApiUri = paasApiUri;
    	File file = new File(pathToWar);
    	warName = file.getName();
    	warLocation = file.getParentFile().getAbsolutePath();
		appName = warName.replace(".war", "");

        deployLocation = "/home/ubuntu/delpoyedprojects";
        memory = "512";
        Date today = new Date();
        date = new SimpleDateFormat("yyyy-MM-dd").format(today);
    }
   
    /*
	public static ClientResponse findApplications() {
		return Deployer.findApplications(createClient());
	}
	
	public static ClientResponse createApplication(String manifest) {
		return Deployer.createApplication(createClient(), manifest);
	}

	public static ClientResponse deployApplication(String envId, String appId, String fileName) throws URISyntaxException, FileNotFoundException{
		try{
			return Deployer.deployApplication(createClient(), envId, appId, fileName);
		}catch(FileNotFoundException e){
			throw e;
		}
	}
		
	public static ClientResponse startApplication(String appId) {
		return CoapsCaller.startApplication(createClient(), appId);
	}
		
	public static ClientResponse restartApplication(String appId) {
		ClientResponse cr = createClient().path("app/" + appId + "/restart")
				.type(MediaType.APPLICATION_XML).post(ClientResponse.class);
		return cr;
	}

	public static ClientResponse stopApplication(String appId) {
		return CoapsCaller.stopApplication(createClient(), appId);
	}
	
	public static ClientResponse createEnvironment(String manifest) {
		return CoapsCaller.createEnvironment(createClient(), manifest);
	}
	*/
    
	public ClientResponse findApplications() {
		return findApplications(createClient());
	}
	
	public  ClientResponse createApplication(String manifest) {
		return createApplication(createClient(), manifest);
	}

	public  ClientResponse deployApplication(String envId, String appId, String fileName) throws URISyntaxException, FileNotFoundException{
		try{
			return deployApplication(createClient(), envId, appId, fileName);
		}catch(FileNotFoundException e){
			throw e;
		}
	}
		
	public  ClientResponse startApplication(String appId) {
		return startApplication(createClient(), appId);
	}
		
	public  ClientResponse restartApplication(String appId) {
		ClientResponse cr = createClient().path("app/" + appId + "/restart")
				.type(MediaType.APPLICATION_XML).post(ClientResponse.class);
		return cr;
	}

	public  ClientResponse stopApplication(String appId) {
		return stopApplication(createClient(), appId);
	}
	
	public  ClientResponse createEnvironment(String manifest) {
		return createEnvironment(createClient(), manifest);
	}
    
    public String deleteApplications(){
    	//String url = paasApiUri+"app/delete";
    	ClientResponse cr = deleteApplications(createClient());
    	return cr.getEntity(new GenericType<String>(){});
    }
	
	/*
    public String deleteApplications(){
    	//String url = paasApiUri+"app/delete";
    	String url = getApiLocation() + "/app/delete";
    	HttpDelete delete = new HttpDelete(url);
		try {
			CloseableHttpResponse response = httpclient.execute(delete);
        	return response.getStatusLine().getStatusCode()+"\n"+getXML(response);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return e.toString();
		}
    }
    */
	
    public String deleteApplication(String appId){    	
    	//String deleteurl = paasApiUri + "app/" + appId + "/delete";
    	ClientResponse cr = deleteApplication(createClient(), appId);
    	return cr.getEntity(new GenericType<String>(){});
    }

    
    /*public String deleteApplication(String appId){    	
    	//String deleteurl = paasApiUri + "app/" + appId + "/delete";
    	String deleteurl = getApiLocation() + "/app/" + appId + "/delete";
    	
    	System.out.println(deleteurl);
    	
    	HttpDelete delete = new HttpDelete(deleteurl);
		try {
			CloseableHttpResponse response = httpclient.execute(delete);
        	return response.getStatusLine().getStatusCode()+"\n"+getXML(response);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return e.toString();
		}        	
    }
    */
    
    //removes environments
    public boolean deleteEnvironments(LogView logView){
    	//String url = paasApiUri+"environment";
    	String url = getApiLocation() + "/environment";
    	HttpGet get = new HttpGet(url);        	
		try {
			logView.newLine("gets environments");
			CloseableHttpResponse response = httpclient.execute(get);
			String string = getXML(response);
			logView.newLine(string);
			ArrayList<Integer> environmentIDs = getIndexes(string);
			logView.newLine("parsed:");
			logView.newLine(environmentIDs.toString());
			for (int i : environmentIDs){
				removeEnvironment(i,logView);
			}
        	return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
    }
    
    private void removeEnvironment(int i, LogView logView) {
		logView.newLine("removes: " + i);
    	//String url = paasApiUri+"environment";
    	String url = getApiLocation() + "environment";
    	HttpDelete delete = new HttpDelete(url+"/" + i);
    	CloseableHttpResponse response;
		try {
			response = httpclient.execute(delete);
			String string = response.getStatusLine().getStatusCode()+"\n"+getXML(response);
			logView.newLine(string);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logView.newLine(e.getMessage());
		}					
	}

	//finds indexes from string
    private static ArrayList<Integer> getIndexes(String string) {
    	ArrayList<Integer> indexes = new ArrayList<Integer>();
    	while(string.contains("<id>")&&string.contains("</id>")){
    		int from = string.indexOf("<id>")+4;
    		int to = string.indexOf("</id>");
    		String id = string.substring(from, to);
    		indexes.add(Integer.parseInt(id));
    		string = string.substring(to+5);
    	}
    	return indexes;
	}        
    
    private static String createManifest(String appName, String warName,
                    String warLocation, String deployLocation, String date, String memory) {
            String xml = ""+
    		"<?xml version=\"1.0\" encoding=\"UTF8\"?>\n"+
            "<paas_application_manifest name=\"" + appName +"Manifest\">\n"+
            "<description>This manifest describes a " + appName + ".</description>\n"+
            "	<paas_application name=\"" + appName + "\"  environement=\"JavaWebEnv\">\n"+
            "		<description>"+appName+" description.</description>\n"+
            "		<paas_application_version name=\"version1.0\" label=\"1.0\">\n"+
//                "			<paas_application_deployable name=\""+warName+"\" content_type=\"artifact\" location=\""+deployLocation+"\" multitenancy_level=\"SharedInstance\"/>\n"+
			"			<paas_application_deployable name=\""+warName+"\" content_type=\"artifact\" location=\""+ warLocation +"\" multitenancy_level=\"SharedInstance\"/>"+
            "			<paas_application_version_instance name=\"Instance1\" initial_state=\"1\" default_instance=\"true\"/>\n"+
            "		</paas_application_version>\n"+
            "	</paas_application>\n"+
            "	<paas_environment name=\"JavaWebEnv\" template=\"TomcatEnvTemp\">\n"+			
            "		<paas_environment_template name=\"TomcatEnvTemp\" memory=\"" + memory + "\">\n"+
            "  		  <description>TomcatServerEnvironmentTemplate</description>\n"+
            "		  <paas_environment_node content_type=\"container\" name=\"tomcat\" version=\"\" provider=\"CF\"/>\n"+
//            "		  <paas_environment_node content_type=\"database\" name=\"mysql\" version=\"\" provider=\"CF\"/>\n"+
            "		</paas_environment_template>\n"+
            "	</paas_environment>\n"+
            "</paas_application_manifest>\n";                		
            return xml;
    }

    private static String getXML(HttpResponse response) {
            ByteArrayOutputStream outstream = new ByteArrayOutputStream();
            try {
                    response.getEntity().writeTo(outstream);
            } catch (IOException e) {
                    return "IOException";
            }
            byte [] responseBody = outstream.toByteArray();
            String responseBodyString = new String(responseBody);
            return responseBodyString;
    }
    
    static String parseUrl(String response) {
            String seekString = "<uri>";
            int startIndex = response.indexOf(seekString)+seekString.length();
            if (startIndex==-1){return "";}
            response = response.substring(startIndex);
            seekString = "</uri>";
            int endIndex = response.indexOf(seekString);
            if (endIndex==-1){return "";}
            String uriToService = response.substring(0, endIndex);
            if (!uriToService.startsWith("http")){
        		uriToService = "http://" + uriToService;
        	}
            return uriToService;
    }

    //should parse appID number from XML... could also be done with somekind of xmlparser :)
    static String parseAppID(String response) {
            String seekString = "appId=\"";
            int startIndex = response.indexOf(seekString)+seekString.length();
            if (startIndex==-1){return "-1";}
            response = response.substring(startIndex);
            seekString = "\"";
            int endIndex = response.indexOf(seekString)+seekString.length();
            if (endIndex==-1){return "-1";}
            String appId = response.substring(0, endIndex-1);
            return appId;
    }
    


    //should parse envID number from XML... could also be done with somekind of xmlparser :)
    static String parseEnvID(String response) {
            String seekString = "envId=\"";
            int startIndex = response.indexOf(seekString)+seekString.length();
            if (startIndex==-1){return "-1";}
            response = response.substring(startIndex);
            seekString = "\"";
            int endIndex = response.indexOf(seekString)+seekString.length();
            if (endIndex==-1){return "-1";}
            String envId = response.substring(0, endIndex-1);
            return envId;
    }

    private HttpResponse makePostRequest(String urlString, String requestXML,File file) throws IOException{
            
            HttpPost post = new HttpPost(urlString);

            //create new post with xml data
            if (requestXML!=null){
                    StringEntity data = new StringEntity(requestXML);
                    data.setContentType("application/xml");
                    post.setEntity(data);
            }
            //make post
            return httpclient.execute(post);
    }

    public String formPost(String deployLocation, File file) {
            try {
                    HttpResponse response = makePostRequest(deployLocation,null,file);
                    HttpEntity entity = response.getEntity();
                    String value = EntityUtils.toString(entity);
                    return value;
            } catch (IOException e) {
                    String output = e.toString(); 
                    return output;
            }
            
    }

	public String getManifest(String pathToWar) {
		// TODO Auto-generated method stub
		return createManifest(appName,warName, warLocation, deployLocation, date,memory);
	}

	public ArrayList<Object[]> createRows(String responseString,final CFAppsView cfAppsView, final LogView logView) {
		String[] splittedResponse = responseString.split("<application>") ;
		ArrayList<Object[]> apps = new ArrayList<Object[]>();
		for (String split:splittedResponse){
			final String idstring = parse(split,"id");
			String name = parse(split,"name");
			String description = parse(split,"description");
			final String uri = parse(split,"uri");
			if (idstring!=null){
				Button button = new Button("Show more");
				final Deployer deployer = this;
				button.addClickListener(new Button.ClickListener() {				
					@Override
					public void buttonClick(ClickEvent event) {
						//WebResource client = Deployer.createClient();
						//ClientResponse response = Deployer.describeApplication(client, idstring);
						WebResource client = createClient();
						ClientResponse response = describeApplication(client, idstring);
						String responseString = response.getEntity(new GenericType<String>(){});
				        CFAppView view = new CFAppView(idstring, responseString,cfAppsView,logView,deployer, linkToApp, buttonLinkToApp);
				        Window w = new Window("Application with appId: " + idstring);
						w.center();
						w.setWidth("80%");
						w.setHeight("80%");
						w.setContent(view);
						view.setSizeFull();
						UI.getCurrent().addWindow(w);
					}	
				});

				Integer id = new Integer(Integer.parseInt((String) idstring));
				Object[] app = new Object[] {id, name, description, uri,button};
				apps.add(app);					
			}
		}
		return apps;
	}

	private static String parse(String txt, String tag) {
		String startTag = "<" + tag + ">";
        int startIndex = txt.indexOf(startTag);
        if (startIndex==-1){return null;}
        startIndex+=+startTag.length();
        txt = txt.substring(startIndex);
        String endTag = "</" + tag + ">";
        int endIndex = txt.indexOf(endTag);
        if (endIndex==-1){return null;}
        return txt.substring(0, endIndex);
	}
	
	public synchronized void addDeployListener(DeployListener li) {
		listeners.add(li);
	}
	
	public synchronized void removeDeployListener(DeployListener li) {
		listeners.remove(li);
	}
	
	private void runAsync(Runnable runnable){
		synchronized (this) {
			System.out.println("4");
			executor.submit(runnable);
			System.out.println("5");
		}
	}

	public void deploy(UserSettings settings, SharedProject project, LogView logView, Button linkToApp, QRCode qrCode){
		doDeploy(settings, project, logView,linkToApp, qrCode);
	}
	
	/*
	private String environmentID = null;
	
    private String createEnvironmentManifest(String memory) {
    String xml = ""+
	"<?xml version=\"1.0\" encoding=\"UTF8\"?>\n"+
    "<paas_environment_manifest name=\"JavaWebEnvManifest\">\n"+
    "	<paas_environment name=\"JavaWebEnv\" template=\"TomcatEnvTemp\">\n"+			
    "		<paas_environment_template name=\"TomcatEnvTemp\" memory=\"" + memory + "\">\n"+
    "  		  <description>TomcatServerEnvironmentTemplate</description>\n"+
    "		  <paas_environment_node content_type=\"container\" name=\"tomcat\" version=\"\" provider=\"CF\"/>\n"+
    "		</paas_environment_template>\n"+
    "	</paas_environment>\n"+
    "</paas_environment_manifest>\n";                		
    return xml;
    }
    
    private static String createApplicationManifest(String appName, String warName, String warLocation) {
    String xml = ""+
	"<?xml version=\"1.0\" encoding=\"UTF8\"?>\n"+
    "<paas_application_manifest name=\"" + appName +"Manifest\">\n"+
    "<description>This manifest describes a " + appName + ".</description>\n"+
    "	<paas_application name=\"" + appName + "\"  environement=\"JavaWebEnv\">\n"+
    "		<description>"+appName+" description.</description>\n"+
    "		<paas_application_version name=\"version1.0\" label=\"1.0\">\n"+
//        "			<paas_application_deployable name=\""+warName+"\" content_type=\"artifact\" location=\""+deployLocation+"\" multitenancy_level=\"SharedInstance\"/>\n"+
	"			<paas_application_deployable name=\""+warName+"\" content_type=\"artifact\" location=\""+ warLocation +"\" multitenancy_level=\"SharedInstance\"/>"+
    "			<paas_application_version_instance name=\"Instance1\" initial_state=\"1\" default_instance=\"true\"/>\n"+
    "		</paas_application_version>\n"+
    "	</paas_application>\n"+
    "	<paas_environment name=\"JavaWebEnv\" template=\"TomcatEnvTemp\">\n"+			
    "		<paas_environment_template name=\"TomcatEnvTemp\" memory=\"" + memory + "\">\n"+
    "  		  <description>TomcatServerEnvironmentTemplate</description>\n"+
    "		  <paas_environment_node content_type=\"container\" name=\"tomcat\" version=\"\" provider=\"CF\"/>\n"+
//    "		  <paas_environment_node content_type=\"database\" name=\"mysql\" version=\"\" provider=\"CF\"/>\n"+
    "		</paas_environment_template>\n"+
    "	</paas_environment>\n"+
    "</paas_application_manifest>\n";                		
    return xml;
    }
	*/
	/*
	public static ClientResponse findEnvironments() {
		return CoapsCaller.findEnvironments(createClient());
	}
	
	public static ClientResponse getDeployedApplications(String envId) {
		return CoapsCaller.getDeployedApplications(createClient(), envId);
	}
	
    static List<String> parseEnvIDs(String response) {
    	
    	List<String> listofEnvIDs = new ArrayList<String>();
    	
    	while(true) {
	        String seekString = "<id>";
	        if (response.indexOf(seekString) == -1) {return listofEnvIDs;}
	        int startIndex = response.indexOf(seekString)+seekString.length();
	        response = response.substring(startIndex);
	        seekString = "<";
	        if (response.indexOf(seekString) == -1) {return listofEnvIDs;}
	        int endIndex = response.indexOf(seekString)+seekString.length();
	        String envId = response.substring(0, endIndex-1);
	        listofEnvIDs.add(envId);
    	}
    }
    */
	
    private String parseAppName(String response) {
    	
    	while(true) {
	        String seekString = "<id>";
	        if (response.indexOf(seekString) == -1) {return null;}
	        int startIndex = response.indexOf(seekString)+seekString.length();
	        response = response.substring(startIndex);
	        seekString = "<";
	        if (response.indexOf(seekString) == -1) {return null;}
	        int endIndex = response.indexOf(seekString)+seekString.length();
	        String appId = response.substring(0, endIndex-1);
	        
	        seekString = "<name>";
	        if (response.indexOf(seekString) == -1) {return null;}
	        startIndex = response.indexOf(seekString)+seekString.length();
	        response = response.substring(startIndex);
	        seekString = "<";
	        if (response.indexOf(seekString) == -1) {return null;}
	        endIndex = response.indexOf(seekString)+seekString.length();
	        String applicationName = response.substring(0, endIndex-1);
	        
	        if (applicationName.matches(this.appName)) {
	        	return appId;
	        }
    	}
    }
    
    public void checkIFSomeoneIsDeploying(VerticalLayout resultLayout, QRCode qrCode, Embedded loadingImg) {
    	
		resultLayout.removeAllComponents();
		buttonLinkToApp.setVisible(false);
		linkToApp.setVisible(false);
		qrCode.setVisible(false);
    	
    	if(deployingProjectsInUris.get(getApiLocation()).get(appName) != null) {
    		deployButton.setEnabled(false);
			loadingImg.setVisible(true);
			loadingImg.setCaption(deployingProjectsInUris.get(getApiLocation()).get(appName).getName() + " is deploying");
    	}
    	else {
    		deployButton.setEnabled(true);
    		loadingImg.setVisible(false);
    	}
    }
	
	private void doDeploy(UserSettings settings, SharedProject project, final LogView logView,final Button buttonLink, final QRCode qrCode) {		
		final String file;
		//this is not working yet
		if (settings.compileGae){
			File f = new File(project.getProjectDir().getAbsolutePath(), "hellotest.tar.gz");
			file = f.getAbsolutePath();
			//package as tar
			//somehow deploy it :)
		}else{
			//deploys war over cf-api
			file = pathToWar;
		}
		
		if(!new File(file).isFile()) {
			logView.newLine("Aborted: FileNotFoudException: " + file + " not found!");
			fireNetworkingFinished(false, "File not found: press build", null);
			return;
		}
		
		if(deployingProjectsInUris.get(getApiLocation()).get(appName) != null) {
				//&& deployingProjectsInUris.get(getApiLocation()).get(appName) == true) {
			//Notification.show(user.getName() + " is deploying " + appName + " in this server rigth now! Try later");
			return;
		}
		
		
		System.out.println("1");
		
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				
				System.out.println("2");
				
				deployingProjectsInUris.get(getApiLocation()).put(appName, user);
				
				//Client for doing deploying
				//Deployer apiClient = new Deployer(file, paasApiUri, null, null);
				
				//Adds some visibilitythings to user interface
				//StartOfDeploymentBroadcaster.broadcastDeplyStated(getApiLocation(), appName, user);
				System.out.println("after broadcat");
				fireNetworkingStarted("deploying application");
				System.out.println("after finish network");
				
				/*
				if(!new File(file).isFile()) {
					logView.newLine("Aborted: FileNotFoudException: " + file + " not found!");
					StartOfDeploymentBroadcaster.broadcastDeplyFinished(getApiLocation(), appName, user);
					fireNetworkingFinished(false, "File not found: press build", null);
					deployingProjectsInUris.get(getApiLocation()).remove(appName);
					
					return;
				}
				*/
				//ClientResponse findAppsResponse = Deployer.findApplications();
				
				ClientResponse findAppsResponse = findApplications();
				String listOfAppsString = findAppsResponse.getEntity(new GenericType<String>(){});
				logView.newLine("response: " + listOfAppsString);
				String existedAppId = parseAppName(listOfAppsString);
				if( existedAppId != null) {
					//System.out.println("found: " + existedAppId);
					deleteApplication(existedAppId);
					//stopApplication(existedAppId);
					//String responseString = response.getEntity(new GenericType<String>(){});
				}
				
				/*
				ClientResponse findEnvsResponse = findEnvironments();
				String listOfEnvsString = findEnvsResponse.getEntity(new GenericType<String>(){});
				logView.newLine("response: " + listOfEnvsString);
				List<String> listOfEnvIDs = new ArrayList<String>(parseEnvIDs(listOfEnvsString));
				
				for (int i = 0; i < listOfEnvIDs.size(); i++) {
					//System.out.println(listOfEnvIDs.get(i));
					logView.newLine(listOfEnvIDs.get(i));
					ClientResponse getDeployedAppResponse = getDeployedApplications(listOfEnvIDs.get(i));
					String listOfDeployedAppInEnv = getDeployedAppResponse.getEntity(new GenericType<String>(){});
					logView.newLine("response: " + listOfDeployedAppInEnv);
				}
				*/
				/*
				if (environmentID == null) {
					
					//manifest for creating environment and application (same can be used for the both)
					String environmentManifest = createEnvironmentManifest(memory);
					logView.newLine("Manifest: " + environmentManifest);
					//Adds some visibilitythings to user interface
					fireNetworkingStarted("deploying application");
					//creates environment and extracts envID
					logView.newLine("create environment");
					ClientResponse envresponse = createEnvironment(environmentManifest);
					String envresponsestring = envresponse.getEntity(new GenericType<String>(){});
					logView.newLine("response: " + envresponsestring);
					environmentID = parseEnvID(envresponsestring);
				}
				
				logView.newLine("create app");
				ClientResponse appresponse = Deployer.createApplication(manifest);
				String appresponsestring = appresponse.getEntity(new GenericType<String>(){});
				logView.newLine("response: " + appresponsestring);
				String appId = Deployer.parseAppID(appresponsestring);
				*/
				
				//manifest for creating environment and application (same can be used for the both)
				//final String manifest = apiClient.getManifest(file);
				final String manifest = getManifest(file);
				logView.newLine("Manifest: " + manifest);

				//creates environment and extracts envID
				logView.newLine("create environment");
				//ClientResponse envresponse = Deployer.createEnvironment(manifest);
				ClientResponse envresponse = createEnvironment(manifest);
				String envresponsestring = envresponse.getEntity(new GenericType<String>(){});
				logView.newLine("response: " + envresponsestring);
				String envId = Deployer.parseEnvID(envresponsestring);
				//creates application and extracts appID
				logView.newLine("create app");
				//ClientResponse appresponse = Deployer.createApplication(manifest);
				ClientResponse appresponse = createApplication(manifest);
				String appresponsestring = appresponse.getEntity(new GenericType<String>(){});
				logView.newLine("response: " + appresponsestring);
				String appId = Deployer.parseAppID(appresponsestring);
				
				//sends the file over the network
				String uriToService="";
				logView.newLine("deploy app");
				ClientResponse deployresponse = null;
				try {
					//deployresponse = Deployer.deployApplication(envId, appId, file);
					deployresponse = deployApplication(envId, appId, file);
				} 
				catch (FileNotFoundException e) {
					
					logView.newLine("Aborted: FileNotFoudException: " + e.getMessage());
					e.printStackTrace();
					//StartOfDeploymentBroadcaster.broadcastDeplyFinished(getApiLocation(), appName, user);
					fireNetworkingFinished(false, "File not found: press build", null);
					deployingProjectsInUris.get(getApiLocation()).remove(appName);
					return;
									
				} 
				catch (URISyntaxException e) {
					// TODO Auto-generated catch block
					logView.newLine("exception: " + e.getMessage());
					e.printStackTrace();
					//StartOfDeploymentBroadcaster.broadcastDeplyFinished(getApiLocation(), appName, user);
					fireNetworkingFinished(false, "Deploying failed", null);
					deployingProjectsInUris.get(getApiLocation()).remove(appName);
					return;
				}
				String deployresponsestring = deployresponse.getEntity(new GenericType<String>(){});
				logView.newLine("response: " + deployresponsestring);
				uriToService = Deployer.parseUrl(deployresponsestring);
				logView.newLine("uri: " + uriToService);
		
				//if deploying successed, then application is started
		        if (uriToService.length()>0){
					logView.newLine("start app");
					//ClientResponse startresponse = Deployer.startApplication(appId);
					ClientResponse startresponse = startApplication(appId);
					String startresponsestring = startresponse.getEntity(new GenericType<String>(){});
					logView.newLine("response: " + startresponsestring);
					
					buttonLink.setVisible(true);
		    		qrCode.setValue(uriToService);
		    		qrCode.setVisible(true);
					//StartOfDeploymentBroadcaster.broadcastDeplyFinished(getApiLocation(), appName, user);
					fireNetworkingFinished(true, "Deploying successed", uriToService);
					deployingProjectsInUris.get(getApiLocation()).remove(appName);
		        }else{
					//StartOfDeploymentBroadcaster.broadcastDeplyFinished(getApiLocation(), appName, user);
					fireNetworkingFinished(false, "Deploying failed", null);
					deployingProjectsInUris.get(getApiLocation()).remove(appName);
		        }
			}
		};
		
		System.out.println("3");
		
		runAsync(runnable);
		
		System.out.println("6");
	}

	private void fireNetworkingFinished(boolean success, String msg, String uriToService) {
		// Doesn't need to fire in a different thread because this is always
		// triggered by a background thread, never a Vaadin UI server visit.
		for (DeployListener li : listeners) {
			li.networkingFinished(success,msg, uriToService);
		}
	}

	private void fireNetworkingStarted(String msg) {
		for (DeployListener li : listeners) {
			li.networkingStarted(msg);
		}
	}

	public void cancel() {
		synchronized (this) {
			executor.shutdownNow();
			executor = Executors.newSingleThreadExecutor();
		}
	}	
}