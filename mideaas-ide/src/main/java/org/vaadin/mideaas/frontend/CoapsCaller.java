/**
 * Copyright (C) 2010-2011 Sebastian Heckmann, Sebastian Laag
 *
 * Contact Email: <sebastian.heckmann@udo.edu>, <sebastian.laag@udo.edu>
 *
 * Contact Email for Autonomic Resources: <mohamed.mohamed@telecom-sudparis.eu>
 *
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.vaadin.mideaas.frontend;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.multipart.FormDataMultiPart;
import com.sun.jersey.multipart.impl.MultiPartWriter;

public class CoapsCaller {

	/**
	 * COAPS API location
	 */
	//private static String apiLocation = "http://130.230.142.89:8080/CF-api/rest";
	
	//private String apiLocation = "http://130.230.142.89:8080/CF-api/rest";
	private String apiLocation = null;
	
	//private final static String apiLocation = "http://130.230.142.89:8080/CF-api/rest";
	//private static String apiLocation = "http://easi-clouds.cs.helsinki.fi:8081/CF-api/rest";

	/**
	 * temporary folder for uploaded files
	 */
	private static String localTempPath = System.getProperty("java.io.tmpdir");

	public CoapsCaller(String apiLocation) {
		this.apiLocation = apiLocation;
	}
	
	// TODO: get the war location by the class of Chan
	//private final static String warLocation = "";

	/*
	 * *-PaaS API operation calls
	 */

	/**
	 * 
	 * @param client
	 * @param manifest
	 * @return the COAPS application ID
	 */
	public ClientResponse createApplication(WebResource client, String manifest) {
		
		ClientResponse cr = client.path("app").type(MediaType.APPLICATION_XML)
				.entity(manifest).post(ClientResponse.class);
		return cr;
	}
	
	/*
	public static void setApiLocation(String apiLocation) {
		CoapsCaller.apiLocation = apiLocation;
	}
	*/

	public ClientResponse updateApplication(WebResource client,
			String manifest, String appId) {
		// TODO manage the envId
		ClientResponse cr = client.path("app/" + appId + "/update/env" + "1")
				.type(MediaType.APPLICATION_XML).entity(manifest)
				.post(ClientResponse.class);

		return cr;
	}
	
	public ClientResponse deleteApplication(WebResource client, String appId) {
		// TODO manage the envId
		ClientResponse cr = client.path("app/" + appId + "/delete")
				.type(MediaType.APPLICATION_XML).delete(ClientResponse.class);

		return cr;
	}
	
	public ClientResponse deleteApplications(WebResource client) {
		// TODO manage the envId
		ClientResponse cr = client.path("app/delete")
				.type(MediaType.APPLICATION_XML).delete(ClientResponse.class);

		return cr;
	}

	public String getApiLocation() {
		return apiLocation;
	}

	public void setApiLocation(String apiLocation) {
		this.apiLocation = apiLocation;
	}

	public ClientResponse updateApplication(WebResource client,
			String appId, String envId, String fileName) {
		FormDataMultiPart form;

		if (!(fileName == null) && !fileName.equals("")) {

			File f = new File(fileName);

			form = new FormDataMultiPart().field("file", f,
					MediaType.MULTIPART_FORM_DATA_TYPE);
		} else {
			File f = new File(localTempPath + "/temp");
			try {
				f.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			form = new FormDataMultiPart().field("file", f,
					MediaType.MULTIPART_FORM_DATA_TYPE);
		}
		ClientResponse cr = client
				.path("app/" + appId + "/update/env/" + envId)
				.type(MediaType.APPLICATION_XML)
				.post(ClientResponse.class, form);
		return cr;

	}

	public ClientResponse createEnvironment(WebResource client, String manifest) {
		ClientResponse cr = client.path("environment")
				.type(MediaType.APPLICATION_XML).entity(manifest)
				.post(ClientResponse.class);
		return cr;

	}

	public ClientResponse deployApplication(WebResource client, String envId, String appId, String fileName)
			throws URISyntaxException,FileNotFoundException {
		
		FormDataMultiPart form;

		if (!(fileName == null) && !fileName.equals("")) {

			// URL url = Test4API.class.getResource(fileName);
			// File f = new File(url.toURI());
			// FileReader fr=new FileReader(fileName);
			File f = new File(fileName);
			if (f.exists()){
				form = new FormDataMultiPart();
				form.field("file", f, MediaType.MULTIPART_FORM_DATA_TYPE);				
			}else{
				throw new FileNotFoundException(fileName + " not found!");
			}
			
		} else {
			throw new FileNotFoundException(fileName + " not found!");
		}
		try{
			ClientResponse cr = client
					.path("app/" + appId + "/action/deploy/env/" + envId)
					.type(MediaType.MULTIPART_FORM_DATA)
					.post(ClientResponse.class, form);
			return cr;
		}catch(Exception e){
			throw e;
		}

	}

	public ClientResponse startApplication(WebResource client,
			String appId) {

		ClientResponse cr = client.path("app/" + appId + "/start")
				.type(MediaType.APPLICATION_XML).post(ClientResponse.class);
		return cr;

	}

	public ClientResponse stopApplication(WebResource client,
			String appId) {

		ClientResponse cr = client.path("app/" + appId + "/stop")
				.type(MediaType.APPLICATION_XML).post(ClientResponse.class);
		return cr;

	}

	
	//farshad
	public ClientResponse findEnvironments(WebResource client) {
		ClientResponse cr = client.path("environment").type(MediaType.APPLICATION_XML)
				.get(ClientResponse.class);
		return cr;

	}
	
	//farshad - it is not implemented yet in Cloud Foundry implementation of COAPS in Tampere
	public ClientResponse getDeployedApplications(WebResource client, String envId) {
		ClientResponse cr = client.path("environment/" + envId + "/app")
				.type(MediaType.APPLICATION_XML).get(ClientResponse.class);
		return cr;

	}

	
	public ClientResponse findApplications(WebResource client) {
		ClientResponse cr = client.path("app").type(MediaType.APPLICATION_XML)
				.get(ClientResponse.class);
		return cr;

	}

	public ClientResponse describeApplication(WebResource client,
			String appId) {
		ClientResponse cr = client.path("app/" + appId)
				.type(MediaType.APPLICATION_XML).get(ClientResponse.class);
		return cr;
	}

	/*
	 * Useful methods
	 */


	public WebResource createClient() {
		ClientConfig config = new DefaultClientConfig();
		config.getClasses().add(MultiPartWriter.class);
		Client client = Client.create(config);
		client.setConnectTimeout(0);
		WebResource service = client.resource(getBaseURI());
		return service;
	}
	
	private URI getBaseURI() {
		//System.out.println( "PaaS URI: " + apiLocation);
		return UriBuilder.fromUri(apiLocation).build();
	}

	
	/*
	 * 
	 * 
	
	public static WebResource createClient() {
		ClientConfig config = new DefaultClientConfig();
		config.getClasses().add(MultiPartWriter.class);
		Client client = Client.create(config);
		client.setConnectTimeout(0);
		WebResource service = client.resource(getBaseURI());
		return service;
	}

	private static URI getBaseURI() {
		//System.out.println( "PaaS URI: " + apiLocation);
		return UriBuilder.fromUri(apiLocation).build();
	}
	 */
	/**
	 * Updates the application instances number
	 * 
	 * @param client
	 * @param coapsID
	 *            : application identifier for COAPS
	 * @param instancesNumber
	 *            new instances number
	 * @return ClientResponse
	 */
	public ClientResponse updateApplicationInstances(WebResource client,
			String coapsID, int instancesNumber) {
		ClientResponse cr = client
				.path("app/" + coapsID + "/instances/" + instancesNumber)
				.type(MediaType.APPLICATION_XML).post(ClientResponse.class);
		return cr;

	}

	/**
	 * adds a service to the application
	 * 
	 * @param client
	 * @param coapsID
	 *            : application identifier for COAPS
	 * @param serviceName
	 * @return ClientResponse
	 */
	public ClientResponse updateApplicationServices(WebResource client,
			String coapsID, String serviceName) {
		ClientResponse cr = client
				.path("app/" + coapsID + "/services/" + serviceName)
				.type(MediaType.APPLICATION_XML).post(ClientResponse.class);
		return cr;

	}
}
