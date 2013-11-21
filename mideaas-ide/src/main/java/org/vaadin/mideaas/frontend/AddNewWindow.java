package org.vaadin.mideaas.frontend;

import java.util.regex.Pattern;

import org.vaadin.mideaas.model.ProjectFile;
import org.vaadin.mideaas.model.SharedProject;
import org.vaadin.mideaas.model.User;

import com.vaadin.data.Item;
import com.vaadin.data.Validator;
import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.PropertysetItem;
import com.vaadin.data.validator.AbstractStringValidator;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;

@SuppressWarnings("serial")
public class AddNewWindow extends Window {

	private static final Pattern validClass = Pattern
			.compile("^[A-Z][A-Za-z1-9_]*$");

	private static final Pattern validFile = Pattern
			.compile("^[A-Za-z1-9_.]+$");

	private static final String VIEW_TYPE = "view";
	private static final String CLASS_TYPE = "class";
	private static final String FILE_TYPE = "file";

	private final SharedProject project;
	private final User user;

	public AddNewWindow(SharedProject project, User user) {
		super("Create a new...");

		this.project = project;
		this.user = user;

		setWidth("60%");
		setHeight("60%");
		center();

	}

	@Override
	public void attach() {
		super.attach();

		Component form1 = createComponentForm();
		Component form2 = createClassForm();
		Component form3 = createFileForm();

		TabSheet tabs = new TabSheet(form1, form2, form3);

		tabs.setSizeFull();
		setContent(tabs);
	}

	private Validator javaClassValidator = new AbstractStringValidator(
			"Not a proper Java class name. Start with a capital letter. Example: MyView") {
		@Override
		protected boolean isValidValue(String value) {
			return validClass.matcher(value).matches();
		}
	};

	private Validator javaFileValidator = new AbstractStringValidator(
			"Not a proper Java file name. Start with a capital letter. Example: Jee.java") {
		@Override
		protected boolean isValidValue(String value) {
			return !badJavaFileName(value);
		}
	};
	
	private Validator filenameValidator = new AbstractStringValidator("Not a valid file name.") {
		@Override
		protected boolean isValidValue(String value) {
			return validFile.matcher(value).matches();
		}
	};
	
	private Validator viewExistsValidator = new AbstractStringValidator("Already exists.") {
		@Override
		protected boolean isValidValue(String value) {
			return !project.containsView(value);
		}
	};
	
	private Validator fileExistsValidator = new AbstractStringValidator("Already exists.") {
		@Override
		protected boolean isValidValue(String value) {
			return !project.containsFile(value);
		}
	};
	
	private Validator reservedWordValidator = new AbstractStringValidator("Java reserved word.") {
		@Override
		protected boolean isValidValue(String value) {
			return !JavaUtil.isJavaReservedWord(value.toLowerCase());
		}
	};

	private Component createComponentForm() {
		AddForm form = new AddForm(VIEW_TYPE, "Add View", javaClassValidator, viewExistsValidator, reservedWordValidator);
		form.setCaption("View");
		return form;
	}

	private Component createClassForm() {
		AddForm form = new AddForm(CLASS_TYPE, "Add Class", javaClassValidator, viewExistsValidator, reservedWordValidator);
		form.setCaption("Java Class");
		return form;
	}

	private Component createFileForm() {
		AddForm form = new AddForm(FILE_TYPE, "Add File", filenameValidator, fileExistsValidator, javaFileValidator);
		form.setCaption("File");
		return form;
	}
	
	

	private class AddForm extends FormLayout {
		private TextField name = new TextField("Name:");

		private AddForm(String type, String addText,
				Validator... nameValidators) {
			PropertysetItem item = new PropertysetItem();
			item.addItemProperty("type", new ObjectProperty<String>(type));
			addItemProperties(item);

			name.setRequired(true);

			addComponent(name);
			final FieldGroup binder = new FieldGroup(item);
			binder.bindMemberFields(this);

			Button b = new Button(addText);
			b.addClickListener(new ClickListener() {
				@Override
				public void buttonClick(ClickEvent event) {
					try {
						binder.commit();
						addToProject(binder.getItemDataSource());
					} catch (CommitException e) {
					}
				}
			});
			addComponent(b);

			for (Validator v : nameValidators) {
				name.addValidator(v);
			}
		}

		protected void addItemProperties(PropertysetItem item) {
			item.addItemProperty("name", new ObjectProperty<String>(""));
		}
	}

	private void addToProject(Item item) {
		String type = (String) item.getItemProperty("type").getValue();
		String className = (String) item.getItemProperty("name").getValue();

		if (VIEW_TYPE.equals(type)) {
			project.createView(className, user);
		}
		else if (CLASS_TYPE.equals(type)) {
			String cls = project.getPackageName()+"."+className;
			String filename = className+".java";
			ProjectFile f = ProjectFile.newJavaFile(filename, JavaUtil.generateClass(cls, null), project.getSourceFileLocation(filename), project.getLog());
			project.addFile(f, user);
		}
		else if (FILE_TYPE.equals(type)) {
			ProjectFile f = new ProjectFile(className, "", null, project.getSourceFileLocation(className), project.getLog());
			project.addFile(f, user);
		}
		else {
			return;
		}
		
		close();
	}



	private static boolean badJavaFileName(String name) {
		return name.endsWith(".java")
				&& validClass.matcher(name.subSequence(0, name.length() - 5))
						.matches();
	}

}
