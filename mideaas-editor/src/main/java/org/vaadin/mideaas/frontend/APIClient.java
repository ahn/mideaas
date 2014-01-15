package org.vaadin.mideaas.frontend;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

public class APIClient  {

        private static DefaultHttpClient httpclient = new DefaultHttpClient();
		private String date;
		private static String paasApiUrl = "http://130.230.142.89:8080/CF-api/rest/";;
		private String memory;
		private String deployLocation;
		private String warLocation;
		private String appName;
		private String warName;

        APIClient(String pathToWar){
			warName=pathToWar.substring(pathToWar.lastIndexOf("\\")+1);
			appName = warName.replace(".war", "");
	        warLocation = pathToWar.substring(0,pathToWar.lastIndexOf("\\"));
	
	        deployLocation = "/home/ubuntu/delpoyedprojects";
	        memory = "128";
	        Date today = new Date();
	        date = new SimpleDateFormat("yyyy-MM-dd").format(today);
        }
        
        public static String deleteApps(){
        	String url = paasApiUrl+"app/delete";
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
        
        public static String findApps(){
        	String url = paasApiUrl+"app";
        	HttpGet get = new HttpGet(url);
        	try {
				return getXML(httpclient.execute(get));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return e.toString();
			}
        }
        
		//return url to deployed app
        public String depployApp(LogView logView)  {
            try{
        		logView.newLine("Handles application deploying");
            	HttpResponse response = null;
                final String appID;
                final String envID;
                String url = "";
                String xmlData = "";
            
                //this is for testing without creating new envs or apps
                boolean createEnvAndApp=true;
                if (createEnvAndApp){
                        //Create an environment
            			logView.newLine("creates environment");
                		url = paasApiUrl+"environment";
                        String manifest = createManifest(appName,warName, warLocation, deployLocation, date,memory);
            			logView.newLine(manifest);
                        response = makePostRequest(url,manifest,null);
                        if (response.getStatusLine().getStatusCode()!=200){
                                String msg = "Creating environment with " + url + " failed: " + response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase();
                    			logView.newLine(msg);
                                return msg;
                        }
                        xmlData = getXML(response);
            			logView.newLine(xmlData);
                        envID = parseEnvID(xmlData);
                        if (envID.equals("-1")){
                        	String msg= "Creating environment with " + url + " failed; Data was: " + xmlData;
                			logView.newLine(msg);
                			return msg;
                        }
                        
                        //create the application
            			logView.newLine("creates application");
                        url = paasApiUrl+"app";
            			logView.newLine(manifest);
                        response = makePostRequest(url,manifest,null);
                        if (response.getStatusLine().getStatusCode()!=200){
                        	String msg = "Creating app with " + url + " failed: " + response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase();
                        	logView.newLine(msg);
                			return msg;                			
                        }
                        xmlData = getXML(response);
                    	logView.newLine(xmlData);
                        appID = parseAppID(xmlData);
                        if (appID.equals("-1")){
                                String msg = "Creating application with " + url + " failed; Data was: " + xmlData;
                            	logView.newLine(msg);
                    			return msg;                			
                        }
                }else{
                        //these are for testing... Remember to change
                        appID="2";
                        envID="1";
                }
                
                //Deploy the application
    			logView.newLine("deploys application");
                url = paasApiUrl+"app/"+appID+"/action/deploy/env/"+envID;
                response = makePostRequest(url,null,new File(warLocation,warName));
                xmlData = getXML(response);
            	logView.newLine(xmlData);
                if (response.getStatusLine().getStatusCode()!=200){
                	String msg = "Deploying app with " + url + " failed: " + response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase();
                	logView.newLine(msg);
        			return msg;                			    
                }
                
                //Start the application
    			logView.newLine("starts application");
                url = paasApiUrl+"app/"+appID+"/start";
                response = makePostRequest(url,null,null);
                if (response.getStatusLine().getStatusCode()!=200){
                    	String msg = "Starting app with " + url + " failed: " + response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase();
                    	logView.newLine(msg);
            			return msg;                			    
                }
                xmlData = getXML(response);
            	logView.newLine(xmlData);
                String serviceurl = parseUrl(xmlData);
                if (appID.equals("-1")){
                	String msg = "Starting application with " + url + " failed; Data was: " + xmlData;
                	logView.newLine(msg);
                    return msg;
                }
                
                return "http://"+serviceurl;                        
                
            } catch (Exception e) {
                String output = "something else failed in deploying" + e.toString(); 
            	logView.newLine(output);
                return output;
            }
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
                "			<paas_application_deployable name=\""+warName+"\" content_type=\"artifact\" location=\""+deployLocation+"\" multitenancy_level=\"SharedInstance\"/>\n"+
                "			<paas_application_version_instance name=\"Instance1\" initial_state=\"1\" default_instance=\"true\"/>\n"+
                "		</paas_application_version>\n"+
                "	</paas_application>\n"+
                "	<paas_environment name=\"JavaWebEnv\" template=\"TomcatEnvTemp\">\n"+			
                "		<paas_environment_template name=\"TomcatEnvTemp\" memory=\"" + memory + "\">\n"+
                "  		  <description>TomcatServerEnvironmentTemplate</description>\n"+
                "		  <paas_environment_node content_type=\"container\" name=\"tomcat\" version=\"\" provider=\"CF\"/>\n"+
                "		  <paas_environment_node content_type=\"database\" name=\"mysql\" version=\"\" provider=\"CF\"/>\n"+
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
        
        private static String parseUrl(String response) {
                String seekString = "<uris>";
                int startIndex = response.indexOf(seekString)+seekString.length();
                if (startIndex==-1){return "-1";}
                response = response.substring(startIndex);
                seekString = "</uris>";
                int endIndex = response.indexOf(seekString);
                if (endIndex==-1){return "-1";}
                String url = response.substring(0, endIndex);
                return url;
        }

        //should parse appID number from XML... could also be done with somekind of xmlparser :)
        private static String parseAppID(String response) {
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
        private static String parseEnvID(String response) {
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
                }//create new post with filecontent  
                else if (file!=null){                        
                        MultipartEntity entity = new MultipartEntity();        // Should work! 200 OK
                        FileBody fileBody = new FileBody(file);
                        entity.addPart("file", fileBody);        
                        post.setEntity(entity);
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
}