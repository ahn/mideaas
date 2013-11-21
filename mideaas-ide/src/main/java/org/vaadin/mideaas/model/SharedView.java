package org.vaadin.mideaas.model;

import japa.parser.ParseException;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.vaadin.aceeditor.ServerSideDocDiff;
import org.vaadin.aceeditor.client.AceDoc;
import org.vaadin.mideaas.editor.ClaraXmlUtil;
import org.vaadin.mideaas.editor.ErrorChecker;
import org.vaadin.mideaas.editor.MultiUserDoc;
import org.vaadin.mideaas.editor.XmlSyntaxErrorChecker;
import org.vaadin.mideaas.java.JavaSyntaxErrorChecker;

// TODO: Auto-generated Javadoc
/**
 * The Class SharedView.
 */
public class SharedView {

	public static final String NAME_PATTERN = "[A-Z]\\w+";

	public static final Pattern REGEX_NAME = Pattern.compile(NAME_PATTERN);

	public static final Pattern REGEX_MODEL = Pattern.compile("("
			+ NAME_PATTERN + ")\\.clara.xml");

	public static final Pattern REGEX_CONTROLLER = Pattern.compile("("
			+ NAME_PATTERN + ").java");

	/**
	 * The Enum ViewPart.
	 */
	public enum ViewPart {

		MODEL(REGEX_MODEL),

		CONTROLLER(REGEX_CONTROLLER);

		private Pattern pattern;

		private ViewPart(Pattern pattern) {
			this.pattern = pattern;
		}

		public String componentNameOf(String filename) {
			// TODO: better solution
			if (filename.equals("App.java")) {
				return null;
			}

			Matcher m = pattern.matcher(filename);
			if (m.matches()) {
				return m.group(1);
			}
			return null;
		}
	}

	public static class ViewPartInfo {

		public final String viewName;

		public final ViewPart part;

		public ViewPartInfo(String componentName, ViewPart part) {
			this.viewName = componentName;
			this.part = part;
		}
	}

	private static final Set<String> RESERVED_COMPONENT_NAMES = new HashSet<String>();

	static {
		RESERVED_COMPONENT_NAMES.add(ProjectFileUtils.getAppClassName());
	}

	private String javaPackage;

	private final String name;

//	private final String modelId = generateModelId();

	private MultiUserDoc controllerMud;
	private MultiUserDoc modelMud;

	private ErrorChecker javaChecker = new JavaSyntaxErrorChecker();

	/**
	 * Instantiates a new shared component.
	 * 
	 * @param javaPackage
	 *            the java package
	 * @param name
	 *            the name
	 */
	public SharedView(String javaPackage, String name, File saveBaseToDir, ProjectLog log) {
		
		// TODO: it doesn't always make sense to assign something to the muds
		// if we know we're going to set them right away, such as when reading from disk.
		
		this.javaPackage = javaPackage;
		this.name = name;
		String code = ControllerCode.createInitial(javaPackage, name).getCode();
		File codeFile = new File(saveBaseToDir, name+".java");
		controllerMud = new MultiUserDoc(codeFile.getName(), new AceDoc(code), javaChecker, codeFile);
		String xml = ClaraXmlUtil.createHelloWorld("VerticalLayout", "This is " + getName());
		File modelFile = new File(saveBaseToDir, name+".clara.xml");
		modelMud = new MultiUserDoc(modelFile.getName(), new AceDoc(xml), new XmlSyntaxErrorChecker(), modelFile);
//		storeModelForVisualDesigner();
	}

	//
	/**
	 * Gets the component part.
	 * 
	 * @param filename
	 *            the filename
	 * @return the component part
	 */
	public static ViewPartInfo getViewPart(String filename) {
		for (ViewPart part : ViewPart.values()) {
			String name = part.componentNameOf(filename);
			if (name != null) {
				return new ViewPartInfo(name, part);
			}
		}
		return null;
	}
	
	private String getControllerName() {
		return getName();
	}

	public String getName() {
		return name;
	}
	
	synchronized public String getControllerFullName() {
		return javaPackage + "." + getControllerName();
	}

	/**
	 * Checks if is valid name.
	 * 
	 * @param name
	 *            the name
	 * @return true, if is valid name
	 */
	public static boolean isvalidName(String name) {
		return REGEX_NAME.matcher(name).matches();
	}

	public MultiUserDoc getControllerMUD() {
		return controllerMud;
	}

	public MultiUserDoc getModelMud() {
		return modelMud;
	}

	public void writeBaseToDisk(File dir) throws IOException {
		String ctrl;
		String model;
		synchronized (this) {
			ctrl = controllerMud.getBase().getText();
			model = modelMud.getBase().getText();
		}
		FileUtils.write(new File(dir, getControllerName() + ".java"), ctrl);
		FileUtils.write(new File(dir, name + ".clara.xml"), model);
	}

	public void setModelBase(String xml) {
		modelMud.setBaseNoFire(xml, null /* TODO */);
	}
	
	public void setControllerBase(String code) {
		controllerMud.setBaseNoFire(code, null /* TODO */);
	}


	
	public void ensureClaraFieldExists(String id, String className) {
		try {
			ControllerCode c = new ControllerCode(controllerMud.getBase().getText());
			String code1 = c.getCode();
			c.ensureClaraFieldExists(id, className);
			ServerSideDocDiff d = ServerSideDocDiff.diff(new AceDoc(code1), new AceDoc(c.getCode()));
			controllerMud.tryToApply(d, null);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public boolean ensureClaraHandlerExists(String id, String className, String todo) {
		try {
			ControllerCode c = new ControllerCode(controllerMud.getBase().getText());
			String code1 = c.getCode();
			String comment = todo==null||todo.isEmpty() ? null : "TODO: "+todo;
			c.ensureClaraHandlerExists(id, className, comment);
			ServerSideDocDiff d = ServerSideDocDiff.diff(new AceDoc(code1), new AceDoc(c.getCode()));
			controllerMud.tryToApply(d, null);
			return true;
		} catch (ParseException e) {
			e.printStackTrace();
			return false;
		}
	}

	public void ensureDataSource(String id, String cls, String todo) {
		try {
			ControllerCode c = new ControllerCode(controllerMud.getBase().getText());
			String code1 = c.getCode();
			String comment = todo==null||todo.isEmpty() ? null : "TODO: "+todo;
			c.ensureClaraDataSource(id, cls, comment);
			ServerSideDocDiff d = ServerSideDocDiff.diff(new AceDoc(code1), new AceDoc(c.getCode()));
			controllerMud.tryToApply(d, null);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	//?
	public String getControllerFilename() {
		return name+".java";
	}

}
