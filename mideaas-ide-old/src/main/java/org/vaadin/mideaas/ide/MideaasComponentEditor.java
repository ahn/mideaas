package org.vaadin.mideaas.ide;

import java.util.HashMap;

import org.vaadin.aceeditor.AceMode;
import org.vaadin.aceeditor.SuggestionExtension;
import org.vaadin.mideaas.editor.AsyncErrorChecker;
import org.vaadin.mideaas.editor.MultiUserEditor;
import org.vaadin.mideaas.ide.java.JavaErrorChecker;
import org.vaadin.mideaas.ide.java.JavaSuggester;
import org.vaadin.mideaas.ide.model.SharedProject;
import org.vaadin.mideaas.ide.model.SharedView;
import org.vaadin.mideaas.ide.model.User;

import com.vaadin.ui.Component;
import com.vaadin.ui.TabSheet;

@SuppressWarnings("serial")
public class MideaasComponentEditor extends TabSheet implements
		ClaraEditor.ClaraEditorListener {

	public enum EditingMode {
		NONE,
		//VISUAL,
		CLARA,
		CONTROLLER,
		TEST
	}

	

	private final ClaraEditor claraEditor;
	private final JavaMultiUserEditor controllerEditor;

	final private SharedView comp;
	private HashMap<Component, EditingMode> modeByTab = new HashMap<Component, EditingMode>();
	private HashMap<EditingMode, Component> tabByMode = new HashMap<EditingMode, Component>();

	private EditingMode mode;
	private boolean testingEnabled;

	public MideaasComponentEditor(User user, SharedView comp,
			SharedProject project) {
		this.comp = comp;

		setSizeFull();

		claraEditor = new ClaraEditor(user, comp.getModelMud());
		claraEditor.setSizeFull();

		controllerEditor = new JavaMultiUserEditor(
				user.getEditorUser(),
				comp.getControllerMud(),
				project.getCompiler().getInMemoryCompiler(),
				comp.getControllerFullName());
		
		controllerEditor.setSizeFull();

//		if (testingEnabled) {
//			testEditor = new MideaasTest("Tests");
//			testEditor.setSizeFull();
//		}

		init();
	}

	@Override
	public void attach() {
		super.attach();

		claraEditor.addClaraEditorListener(this);
	}

	private void init() {
		
		addTab(EditingMode.CONTROLLER, controllerEditor, "Code");
		
		addTab(EditingMode.CLARA, claraEditor, "Layout");

//		if (testingEnabled) {
//			addTab(EditingMode.TEST, testEditor, "Tests");
//		}

		addSelectedTabChangeListener(new SelectedTabChangeListener() {

			public void selectedTabChange(SelectedTabChangeEvent event) {
				EditingMode newMode = modeByTab.get(getSelectedTab());
				if (mode == newMode) {
					return;
				}
				mode = newMode;
			}
		});

		mode = EditingMode.CONTROLLER;
	}

	public void addTab(EditingMode mode, Component component, String caption) {
		addTab(component, caption);
		modeByTab.put(component, mode);
		tabByMode.put(mode, component);
	}

	public void setSelectedTab(EditingMode mode) {
		if (mode == EditingMode.CONTROLLER) {
			setSelectedTab(controllerEditor);
		} else if (mode == EditingMode.CLARA) {
			setSelectedTab(claraEditor);
//		} else if (mode == EditingMode.TEST) {
//			setSelectedTab(testEditor);
		}
		// Doing what super.setSelectedTab does when not available: nothing.
	}

	@Override
	public void goToDefinition(String id, String className) {
		comp.ensureClaraFieldExists(id, className);
		setSelectedTab(EditingMode.CONTROLLER);
	}

	@Override
	public void goToHandler(String id, String cls, String todo) {
		comp.ensureClaraHandlerExists(id, cls, "TODO: "+todo);
		setSelectedTab(EditingMode.CONTROLLER);
	}

	@Override
	public void setDataSource(String id, String cls, String todo) {
		comp.ensureDataSource(id, cls, "TODO: "+todo);
		setSelectedTab(EditingMode.CONTROLLER);
	}

	public void setTestingEnabled(boolean enabled) {
		// TODO: what if changing this after the editor has already been created...
		testingEnabled = enabled;
	}

}
