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
import org.vaadin.mideaas.editor.DocDiffMediator;
import org.vaadin.mideaas.editor.JavaSyntaxGuard;
import org.vaadin.mideaas.editor.MultiUserDoc;
import org.vaadin.mideaas.editor.MultiUserDoc.DifferingChangedListener;
import org.vaadin.mideaas.editor.ErrorChecker;
import org.vaadin.mideaas.editor.XmlSyntaxErrorChecker;
import org.vaadin.mideaas.editor.XmlSyntaxGuard;
import org.vaadin.mideaas.frontend.Icons;
import org.vaadin.mideaas.java.JavaSyntaxErrorChecker;
import org.vaadin.mideaas.java.util.CompilingService;
import org.vaadin.mideaas.model.ControllerCode.Modifier;

import com.vaadin.server.Resource;

// TODO: Auto-generated Javadoc
/**
 * The Class SharedView.
 */
public class SharedView extends ProjectItem {

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

	/**
	 * Instantiates a new shared component.
	 * 
	 * @param javaPackage
	 *            the java package
	 * @param name
	 *            the name
	 */
	public SharedView(String javaPackage, String name, File saveBaseToDir, ProjectLog log) {
		super(name);
		// TODO: it doesn't always make sense to assign something to the muds
		// if we know we're going to set them right away, such as when reading from disk.
		
		this.javaPackage = javaPackage;
		this.name = name;
		String code = ControllerCode.createInitial(javaPackage, name).getCode();
		File codeFile = new File(saveBaseToDir, name+".java");
		controllerMud = new MultiUserDoc(new AceDoc(code), codeFile, new JavaSyntaxGuard());
		String xml = ClaraXmlUtil.createHelloWorld("VerticalLayout", "This is " + getName());
		File modelFile = new File(saveBaseToDir, name+".clara.xml");
		modelMud = new MultiUserDoc(new AceDoc(xml), modelFile, new XmlSyntaxGuard());
		
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

	public MultiUserDoc getControllerMud() {
		return controllerMud;
	}

	public MultiUserDoc getModelMud() {
		return modelMud;
	}

	@Override
	public void writeBaseToDisk(File dir) throws IOException {
		String ctrl;
		String model;
		synchronized (this) {
			ctrl = controllerMud.getBaseText();
			model = modelMud.getBaseText();
		}
		FileUtils.write(new File(dir, getControllerName() + ".java"), ctrl);
		FileUtils.write(new File(dir, name + ".clara.xml"), model);
	}

	public void setModelBase(String xml) {
		modelMud.setBaseNoFire(xml);
	}
	
	public void setControllerBase(String code) {
		controllerMud.setBaseNoFire(code);
	}


	// TODO: below
	
	public void ensureClaraFieldExists(final String id, final String className) {
		modify(new Modifier() {
			@Override
			public void modify(ControllerCode c) {
				c.ensureClaraFieldExists(id, className);
			}
		});
	}
	
	private void modify(Modifier modifier) {
		final ServerSideDocDiff d;
		try {
			d = ControllerCode.getDiffAfterModify(controllerMud.getBaseText(), modifier);
		} catch (ParseException e) {
			return;
		}
		DocDiffMediator mm = controllerMud.getBase().fork();
		mm.getDownstream().applyDiff(d);
		mm.detach();
	}

	public void ensureClaraHandlerExists(final String id, final String className, final String comment) {
		modify(new Modifier() {
			@Override
			public void modify(ControllerCode c) {
				c.ensureClaraHandlerExists(id, className, comment);
			}
		});
	}

	public void ensureDataSource(final String id, final String cls, final String comment) {
		modify(new Modifier() {
			@Override
			public void modify(ControllerCode c) {
				c.ensureClaraDataSource(id, cls, comment);
			}
		});
	}

	//?
	public String getControllerFilename() {
		return name+".java";
	}
	
	@Override
	public String[] getJavaClass() {
		String cls = getControllerName();
		String content = getControllerMud().getBaseText();
		return new String[] {cls, content};
	}

	@Override
	public void removeFromDir(File sourceDir) {
		new File(sourceDir, getName()+".java").delete();
		new File(sourceDir, getName()+".clara.xml").delete();
	}

	@Override
	public void removeFromClasspathOf(CompilingService compiler,
			String packageName) {
		compiler.removeClass(packageName+"."+getControllerName());
	}
	
	@Override
	public void removeUser(User user) {
		getControllerMud().removeUserDoc(user.getEditorUser());
		getModelMud().removeUserDoc(user.getEditorUser());
	}

	@Override
	public void addDifferingChangedListener(DifferingChangedListener li) {
		getControllerMud().addDifferingChangedListener(li);
		getModelMud().addDifferingChangedListener(li);
	}

	@Override
	public void removeDifferingChangedListener(DifferingChangedListener li) {
		getControllerMud().removeDifferingChangedListener(li);
		getModelMud().removeDifferingChangedListener(li);
	}

	@Override
	public Resource getIcon() {
		return Icons.APPLICATION_FORM;
	}

}
