package org.vaadin.mideaas.model;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.vaadin.mideaas.frontend.MavenUtil;

public class ProjectFileUtils {
	private static final Pattern VALID_PROJECT_NAME = Pattern.compile("[a-z][a-z0-9]*");
	
	public static void writePomXml(File dir, String content) throws IOException {
        FileUtils.write(getPomXmlFile(dir), content);
    }
    
	public static String readPomXml(File dir) throws IOException {
        return FileUtils.readFileToString(getPomXmlFile(dir));
    }
    
	public static File getPomXmlFile(File dir) {
    	return new File(dir, "pom.xml");
    }
	
//	public static String getClassPath(File dir) {
//		return ProjectFileUtils.getClassesDir(dir) +
//				File.pathSeparator + MavenUtil.getClassPath(dir);
//	}
	
	public static String getClassPath(File dir) {
		// Not including the project target dir because InMemoryCompiler deals
		// with other project classes.
		return MavenUtil.getClassPath(dir);
	}
	
	public static void 	writeAppengineWebXml(File dir, String content) throws IOException {
		FileUtils.write(new File(getWebInfDir(dir),"appengine-web.xml"), content);
	}
	
	public static void writeWebXml(File dir, String content) throws IOException {
		FileUtils.write(new File(getWebInfDir(dir),"web.xml"), content);
	}
	
	private static File getWebInfDir(File dir) {
		return FileUtils.getFile(dir, "src", "main", "webapp", "WEB-INF");
	}
	
	public static void writeApp(File dir, String projPackage, String content) throws IOException {
		FileUtils.write(getApplicationFile(dir, projPackage), content);
	}


	private static File getApplicationFile(File dir, String projPackage) {
		return new File(getSourceDir(dir, projPackage), getAppFileName());
	}


	private static void writeWidgetset(File dir, String projPackage, String content) throws IOException {
		FileUtils.write(getWidgetSetFile(dir, projPackage), content);
	}
	
	
	private static void writeMideaasComponentCode(File dir) throws IOException {
		File f = new File(getMideaasSourceDir(dir), "MideaasComponent.java");
		FileUtils.write(f, generateMideaasComponent());
	}
	
	@SuppressWarnings("unused")
	private static void writeMideaasNavigationViewCode(File dir) throws IOException {
		File f = new File(getMideaasSourceDir(dir), "MideaasNavigationView.java");
		FileUtils.write(f, generateMideaasNavigationView());
	}

	private static File getMetaInfDir(File dir) {
		return FileUtils.getFile(dir, "src", "main", "webapp", "META-INF");
	}
	
	public static File getSourceRoot(File dir) {
        return new File(dir, "src");
    }
	
	public static File getClassesDir(File dir) {
        return FileUtils.getFile(dir, "target", "classes");
    }
	
	private static File getWidgetSetFile(File dir, String projPackage) {
		return FileUtils.getFile(getResourcesDir(dir, projPackage), "gwt", "AppWidgetSet.gwt.xml");
	}
	
	public static File getSourceDir(File dir, String projPackage) {
    	File src = FileUtils.getFile(dir, "src", "main", "java");
    	return FileUtils.getFile(src, projPackage.split("\\."));
    }
	
	public static File getResourcesDir(File dir, String projPackage) {
    	File src = FileUtils.getFile(dir, "src", "main", "resources");
    	return FileUtils.getFile(src, projPackage.split("\\."));
    }
	
	public static File getMideaasSourceDir(File dir) {
		String mideaasPackage = "org.vaadin.mideaas";
		File src = FileUtils.getFile(dir, "src", "main", "java");
    	return FileUtils.getFile(src, mideaasPackage.split("\\."));
		
	}
	
	public static Map<String, String> readSourceFiles(File dir, String projPackage) throws IOException {
		Map<String, String> files = new TreeMap<String, String>();
		File srcDir = getSourceDir(dir, projPackage);
		for (File f : srcDir.listFiles()) {
            if (f.isDirectory()) {
            	// TODO: should we allow subdirs? Currently not.
                continue;
            }
            files.put(f.getName(), FileUtils.readFileToString(f));
        }
		return files;
	}

	public static String getAppFileName() {
		return getAppClassName()+".java";
	}

	public static String getAppClassName() {
		return "App";
	}
		
	public static void createProjectDirs(File dir, String projPackage) {
		dir.mkdirs();
		getSourceDir(dir, projPackage).mkdirs();
		getWebInfDir(dir).mkdirs();
		getMetaInfDir(dir).mkdirs();
		
		getMideaasSourceDir(dir).mkdirs();
	}
	
	public static void writeInitialFilesToDisk(File dir, String projPackage,UserSettings settings) throws IOException {
		createProjectDirs(dir, projPackage);
		//writeApp(dir, projPackage, generateApp(projPackage));
		if (settings.gaeDeployTurnedOn){
			writeTheme(dir,projPackage);
			writeAppengineWebXml(dir, generateAppengineWebXml(projPackage));
			writeLoggingProperties(dir, generateLoggingProperties());
			writeManifestMF(dir, generateManifestMF());
		}
		writeWebXml(dir, generateWebXml(projPackage));
		writeWidgetset(dir, projPackage, generateWidgetset());
		writeMideaasComponentCode(dir);
    }
	
	private static void writeLoggingProperties(File dir,String content) throws IOException{
			FileUtils.write(new File(getWebInfDir(dir),"logging.properties"), content);
	}

	private static String generateLoggingProperties() throws IOException {
		return String.format(readResource("logging.properties.format"));
	}

	private static void writeTheme(File dir,String projPackage) throws IOException {
		String appName = projPackage.substring(projPackage.lastIndexOf(".")+1);
		dir = FileUtils.getFile(dir, "src", "main", "webapp", "VAADIN","themes",appName);
		String filename=appName+".scss";
		FileUtils.write(new File(getWebInfDir(dir),"addons.scss"), "@mixin addons {/n}");
		FileUtils.write(new File(getWebInfDir(dir),filename), "@import \"../reindeer/reindeer.scss\";/n@mixin gaetestvaadin7 {/n@include reindeer;/n}");
		FileUtils.write(new File(getWebInfDir(dir),"styles.scss"), "@import \"addons.scss\";@import \"" + filename +"\";");
	}

	private static void writeManifestMF(File dir, String content) throws IOException {
		FileUtils.write(new File(getMetaInfDir(dir),"MANIFEST.MF"), content);
	}

	private static String generateManifestMF() throws IOException {
		return String.format(readResource("MANIFEST.MF.format"));
	}
	public static String getFirstViewName() {
		return "Main";
	}
	
	private static String generateWidgetset() {
		StringBuilder sb = new StringBuilder("<module>\n" +
				"    <inherits name=\"com.vaadin.DefaultWidgetSet\" />\n");
		sb.append("    <set-property name=\"user.agent\" value=\"");
		sb.append("${mideaas.user.agent}");
		sb.append("\"/>\n");
		sb.append("</module>\n");
		return sb.toString();
	}
	
//	private static String generateWidgetset(String userAgent) {
//		StringBuilder sb = new StringBuilder("<module>\n" +
//				"    <inherits name=\"com.vaadin.DefaultWidgetSet\" />\n");
//		if (userAgent != null) {
//			sb.append("    <set-property name=\"user.agent\" value=\"");
//			sb.append(userAgent);
//			sb.append("\"/>\n");
//		}
//		sb.append("    <set-property name=\"user.agent\" value=\"safari\"/>\n");
//		sb.append("</module>\n");
//		return sb.toString();
//	}
	
	@SuppressWarnings("unused")
	private static String generateWidgetsetTouchkit() {
		return "<module>\n" +
				"    <inherits name=\"com.vaadin.DefaultWidgetSet\" />\n" +
				"    <inherits name=\"com.vaadin.addon.touchkit.gwt.TouchKitWidgetSet\" />\n" +
				"</module>\n";
	}
	
	public static String generatePomXml(String projPackage) throws IOException {
		int lastDot = projPackage.lastIndexOf(".");
		String groupId = projPackage.substring(0, lastDot);
		String artifactId = projPackage.substring(lastDot+1);
		String version = "0.1-SNAPSHOT"; // ?
		return String.format(readResource("pom.xml.format"),
				groupId, artifactId, version);
	}
	
	private static String generateWebXml(String projPackage) throws IOException {
		String app = projPackage + "." + getAppClassName();
		return String.format(readResource("web.xml.format"),
				"com.vaadin.server.GAEVaadinServlet", app, getWidgetSetName(projPackage));
	}

	private static String generateAppengineWebXml(String projPackage) throws IOException {
		return String.format(readResource("appengine-web.xml.format"),
				"jannehellotest","1");
	}

	
	public static String generateApp(String projPackage) {
		return String.format(APP_FORMAT,
				projPackage,
				getAppClassName(),
				ProjectFileUtils.getFirstViewName());
	}
	
	public static String generateMideaasComponent() throws IOException {
		return readResource("MideaasComponent.java");
	}
	
	private static String generateMideaasNavigationView() throws IOException {
		return readResource("MideaasNavigationView.java");
	}
	
	private static String getWidgetSetName(String projPackage) {
		return projPackage + ".gwt.AppWidgetSet";
	}
	
	private static String readResource(String name) throws IOException {
		InputStream is = ProjectFileUtils.class.getClassLoader().getResourceAsStream(name);
		return IOUtils.toString(is);
	}
    
	
	/**
	 * package, appclassname, componentclassname
	 */
	private static final String APP_FORMAT =
			"package %s;\n\n" +
			"import com.vaadin.server.VaadinRequest;\n" +
			"import com.vaadin.ui.UI;\n" +
			"import com.vaadin.ui.Component;\n" +
			"import com.vaadin.annotations.PreserveOnRefresh;\n\n" +
			"@PreserveOnRefresh\n" + 
			"@SuppressWarnings(\"serial\")\n" +
			"public class %s extends UI {\n" +
			"    @Override\n" +
			"    protected void init(VaadinRequest request) {\n" +
			"        Component c = new %s();\n" +
			"        c.setSizeFull();\n" +
			"        setContent(c);\n" +
			"    }\n\n" +
			"}\n";

	/**
	 * package, appclassname, componentclassname
	 */
	@SuppressWarnings("unused")
	private static final String APP_FORMAT_TOUCHKIT =
			"package %s;\n\n" +
			"import com.vaadin.addon.touchkit.ui.NavigationManager;\n" +
			"import com.vaadin.addon.touchkit.ui.NavigationView;\n" +
			"import com.vaadin.server.VaadinRequest;\n" +
			"import com.vaadin.ui.UI;\n" +
			"import com.vaadin.ui.Component;\n\n" +
			"@SuppressWarnings(\"serial\")\n" +
			"public class %s extends UI {\n" +
			"    @Override\n" +
			"    protected void init(VaadinRequest request) {\n" +
			"        Component c = new %s();\n" +
			"        c.setSizeFull();\n" +
			"        setContent(new NavigationManager(c));\n" +
			"    }\n\n" +
			"}\n";

	public static boolean isValidProjectName(String name) {
			return name!=null && VALID_PROJECT_NAME.matcher(name).matches();
	}
}
