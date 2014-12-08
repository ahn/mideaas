package org.vaadin.mideaas.app.maven;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class PomXml {
	
	public static class Dependency {
		public String groupId;
		public String artifactId;
		public String version;
		public Dependency(String groupId, String artifactId, String version) {
			this.groupId = groupId;
			this.artifactId = artifactId;
			this.version = version;
		}
		public Dependency() {
			// TODO Auto-generated constructor stub
		}
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof Dependency) {
				Dependency od = (Dependency)obj;
				return groupId.equals(od.groupId) && artifactId.equals(od.artifactId) && version.equals(od.version);
			}
			return false;
		}
		@Override
		public int hashCode() {
			return groupId.hashCode(); // XXX
		}
	}
	
	private Document doc;
	private String str;
	private boolean dirty;
	
	public PomXml(String value) throws ParserConfigurationException, SAXException, IOException {
		setValue(value);
	}
	
	public String getAsString() throws TransformerFactoryConfigurationError, TransformerException {
		if (dirty) {
			docToString();
		}
		return str;
	}
	
	private void docToString() throws TransformerFactoryConfigurationError, TransformerException {
		Transformer trer = TransformerFactory.newInstance().newTransformer();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		StreamResult result = new StreamResult(baos);
		trer.transform(new DOMSource(doc), result);
		str = baos.toString();
	}
	
	public void setValue(String value) throws ParserConfigurationException, SAXException, IOException {
		if(value.equals(str)) {
			return;
		}
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		doc = builder.parse(new InputSource(new ByteArrayInputStream(value.getBytes("utf-8"))));
		dirty = true;
	}
	
	public void addDependency(String groupId, String artifactId, String version) {
		Element d = doc.createElement("dependency");

		Element gid = doc.createElement("groupId");
		gid.appendChild(doc.createTextNode(groupId));
		d.appendChild(gid);
		
		Element aid = doc.createElement("artifactId");
		aid.appendChild(doc.createTextNode(artifactId));
		d.appendChild(aid);
		
		Element vid = doc.createElement("version");
		vid.appendChild(doc.createTextNode(version));
		d.appendChild(vid);
		
		getDepsNode().appendChild(d);
		
		dirty = true;
	}
	

	
	public void addDependency(String xmlSnippet) throws ParserConfigurationException, UnsupportedEncodingException, SAXException, IOException {
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document doc = builder.parse(new InputSource(new ByteArrayInputStream(xmlSnippet.getBytes("utf-8"))));
		String groupId = doc.getElementsByTagName("groupId").item(0).getFirstChild().getNodeValue();
		String artifactId = doc.getElementsByTagName("artifactId").item(0).getFirstChild().getNodeValue();
		String version = doc.getElementsByTagName("version").item(0).getFirstChild().getNodeValue();
		addDependency(groupId, artifactId, version);
	}
	
	public List<Dependency> getDependencies() {
		NodeList deps = getDepsNode().getChildNodes();
		LinkedList<Dependency> dl = new LinkedList<Dependency>();
		for (int i=0; i<deps.getLength(); i++) {
			Dependency dep = depFromNode(deps.item(i));
			if (dep!=null) {
				dl.add(dep);
			}
		}
		return dl;
	}
	
	private Node getDepsNode() {
		return doc.getElementsByTagName("dependencies").item(0);
	}
	
	public void removeDependency(Dependency dep) {
		NodeList deps = getDepsNode().getChildNodes();
		for (int i=0; i<deps.getLength(); i++) {
			Dependency d2 = depFromNode(deps.item(i)); // XXX
			if (d2 != null && d2.equals(dep)) {
				getDepsNode().removeChild(deps.item(i));
				break;
			}
		}
	}
	
	private static Dependency depFromNode(Node node) {
		NodeList children = node.getChildNodes();
		Dependency dep = new Dependency();
		int all = 0;
		for (int i=0; i<children.getLength(); i++) {
			Node c = children.item(i);
			if (c.getFirstChild()==null) {
				continue;
			}
			if ("groupId".equals(c.getNodeName())) {
				dep.groupId = c.getFirstChild().getNodeValue();
				all |= 1;
			}
			else if ("artifactId".equals(c.getNodeName())) {
				dep.artifactId = c.getFirstChild().getNodeValue();
				all |= 2;
			}
			else if ("version".equals(c.getNodeName())) {
				dep.version = c.getFirstChild().getNodeValue();
				all |= 4;
			}
		}
		
		return all==7 ? dep : null;
	}
	
	private NodeList getNodes(String xPathStr, Node node) throws XPathExpressionException {
		XPath xPath = XPathFactory.newInstance().newXPath();
		return (NodeList)xPath.evaluate(xPathStr, node, XPathConstants.NODESET);
	}
	
	private NodeList getNodes(String xPathStr) throws XPathExpressionException {
		return getNodes(xPathStr, doc.getDocumentElement());
	}
	
	/**
	 * 
	 * @return true iff success
	 */
	public boolean configureJetty(JettyConfiguration cfg) {
		try {
			return configureJettyThrow(cfg);
		} catch (XPathExpressionException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	private boolean configureJettyThrow(JettyConfiguration cfg) throws XPathExpressionException, IllegalStateException {
		NodeList nodes = getNodes("/project/build/plugins/plugin");
		for (int i=0; i<nodes.getLength(); ++i) {
			if (isJettyPluginNode(nodes.item(i))) {
				return configureJettyPlugin(nodes.item(i), cfg);
			}
		}
		return createJettyConfiguration(cfg);
	}
	
	private boolean isJettyPluginNode(Node node) throws XPathExpressionException {
		return hasChild(node, "groupId", "org.mortbay.jetty") &&
				hasChild(node, "artifactId", "jetty-maven-plugin");
	}
	
	private boolean hasChild(Node node, String name, String value) throws XPathExpressionException {
		NodeList ns1 = getNodes(name, node);
		if (ns1.getLength()==1 && ns1.item(0).getChildNodes().getLength() == 1 && value.equals(ns1.item(0).getChildNodes().item(0).getNodeValue())) {
			return true;
		}
		return false;
	}

	private boolean createJettyConfiguration(JettyConfiguration cfg) {
		Element plugin = doc.createElement("plugin");

		Element e1 = doc.createElement("groupId");
		e1.appendChild(doc.createTextNode("org.mortbay.jetty"));
		plugin.appendChild(e1);

		Element e2 = doc.createElement("artifactId");
		e2.appendChild(doc.createTextNode("jetty-maven-plugin"));
		plugin.appendChild(e2);
		
		plugin.appendChild(createJettyConfigurationElement(cfg));
		return true;
	}

	private boolean configureJettyPlugin(Node jettyPlugin, JettyConfiguration cfg) throws XPathExpressionException {
		for (int i=0; i< jettyPlugin.getChildNodes().getLength(); ++i) {
			Node it = jettyPlugin.getChildNodes().item(i);
			if ("configuration".equals(it.getNodeName())) {
				it.getParentNode().removeChild(it);
				break;
			}
		}
		jettyPlugin.appendChild(createJettyConfigurationElement(cfg));
		dirty = true;
		return true;
	}

	private Node createJettyConfigurationElement(JettyConfiguration cfg) {
		Element conf = doc.createElement("configuration");

		Element webApp = doc.createElement("webApp");
		conf.appendChild(webApp);
		
		Element cp = doc.createElement("contextPath");
		cp.appendChild(doc.createTextNode(cfg.contextPath));
		webApp.appendChild(cp);
		
		Element e1 = doc.createElement("stopPort");
		e1.appendChild(doc.createTextNode(String.valueOf(cfg.stopPort)));
		conf.appendChild(e1);
		
		Element e2 = doc.createElement("stopKey");
		e2.appendChild(doc.createTextNode(cfg.stopKey));
		conf.appendChild(e2);
		
		Element e3 = doc.createElement("scanIntervalSeconds");
		e3.appendChild(doc.createTextNode(String.valueOf(cfg.scanInterval)));
		conf.appendChild(e3);
		
		return conf;
	}

	
	

//	private Node getJettyPlugin(Node plugins) {
//		NodeList pcs = plugins.getChildNodes();
//		for (int i=0; i<pcs.getLength(); ++i) {
//			pcs.item(i).isSameNode(arg0)
//		}
	
	
}
