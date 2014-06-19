package org.vaadin.mideaas.frontend;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.vaadin.aceeditor.AceEditor;
import org.vaadin.aceeditor.AceEditor.SelectionChangeListener;
import org.vaadin.mideaas.editor.MultiUserDoc;
import org.vaadin.mideaas.model.User;
import org.vaadin.teemu.clara.Clara;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class ClaraEditor extends CustomComponent implements
		SelectionChangeListener, TextChangeListener {

	public interface ClaraEditorListener {
		public void goToDefinition(String id, String className);
		public void goToHandler(String id, String cls, String todo);
		public void setDataSource(String id, String cls, String todo);
	}

	private LinkedList<ClaraEditorListener> listeners = new LinkedList<ClaraEditorListener>();

	private HorizontalSplitPanel split = new HorizontalSplitPanel();
	private XmlMultiUserEditor editor;
	private final MultiUserDoc mud;
	private VerticalLayout componentContext = new VerticalLayout();

	private ClaraPreviewWindow previewWindow = null;

	private Component claraComponent;
	private String rootClassName;
	
	
	private final VisualDesignerConnector visualEditor;

	private static MideaasServlet servlet;

	private final String modelId = generateModelId();

	private final User user;

	
	private static String visualDesignerUrl;

	public static void setVisualDesignerUrl(String url) {
		visualDesignerUrl = url;
	}
	
	// TODO: this is probably not the right place for this...
	private static String generateModelId() {
		return new BigInteger(130, new Random()).toString(32);
	}
	
	public ClaraEditor(User user, MultiUserDoc modelMud) {
		super();
		this.user = user;
		mud = modelMud;
		editor = new XmlMultiUserEditor(user.getEditorUser(), mud);
		editor.setSizeFull();
		
		storeModel(mud.getBaseText());

		Button openPreview = new Button("Preview");
		openPreview.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				preview();
			}
		});

		VerticalLayout right = new VerticalLayout();
		right.addComponent(openPreview);
		right.setMargin(true);
		right.setSpacing(true);
		
		if (visualDesignerUrl != null) {
			visualEditor = new VisualDesignerConnector(modelId, visualDesignerUrl, this);
			right.addComponent(visualEditor);
		} else {
			visualEditor = null;
		}

		right.addComponent(componentContext);
		componentContext.setMargin(new MarginInfo(true, false, true, false));

		split.setSizeFull();
		split.setSplitPosition(80f);
		setCompositionRoot(split);

		split.setFirstComponent(editor);
		split.setSecondComponent(right);
	}

	private void preview() {
		if (previewWindow != null) {
			UI.getCurrent().removeWindow(previewWindow);
		}
		previewWindow = new ClaraPreviewWindow();
		UI.getCurrent().addWindow(previewWindow);
		compileClara();
	}

	@Override
	public void attach() {
		super.attach();
		
		editor.setSelectionChangeListener(this);
		editor.setTextChangeListener(this);
		
		//mud.addBaseChangedListenerWeak(this);

		udpateXmlContext();
	}

	private String getXml() {
		return editor.getCurrentText();
	}
	
	public void setXml(String xml) {
		// TODO
	}

	private ClaraXmlHandler parseDocument(InputStream is) {

		ClaraXmlHandler myHandler = new ClaraXmlHandler(editor.getCurrentSelection());

		try {
			XMLReader reader = XMLReaderFactory.createXMLReader();
			reader.setContentHandler(myHandler);
			reader.parse(new InputSource(is));

		} catch (SAXException se) {

		} catch (IOException ie) {

		}

		return myHandler;
	}

	@Override
	public void textChange(TextChangeEvent event) {
		
		if (previewWindow != null) {
			compileClara();
		}
	}

	private void compileClara() {
		InputStream is1 = new ByteArrayInputStream(getXml().getBytes());
		try {
			claraComponent = Clara.create(is1);
//			claraComponent.setSizeFull(); // ?
			previewWindow.setContent(claraComponent);
		} catch (Exception e) {
			claraComponent = null;
			previewWindow.setError(e);
		}
	}

	public String getRootComponentClassName() {
		return rootClassName;
	}

	@Override
	public void selectionChanged(AceEditor.SelectionChangeEvent e) {
		udpateXmlContext();
	}

//	@Override
//	public void baseChanged(AceDoc doc, EditorUser user) {
//		storeModel(doc.getText());
//	}
	
	private void storeModel(String xml) {
		MideaasServlet servlet = getServlet();
		if (servlet != null) {
			servlet.putModelXml(modelId, xml);
		}
		else {
			System.err.println("WARNING: can't store model because servlet is null.");
		}
	}

	public synchronized static void setServlet(MideaasServlet servlet) {
		ClaraEditor.servlet = servlet;
	}
	
	private synchronized static MideaasServlet getServlet() {
		return servlet;
	}

	private void udpateXmlContext() {
		componentContext.removeAllComponents();

		InputStream is = new ByteArrayInputStream(getXml().getBytes());
		ClaraXmlHandler mh = parseDocument(is);

		this.rootClassName = mh.rootCls;

		if (mh.currCls == null) {
			return;
		}

		String s;
		String shortCls = mh.currCls.substring(mh.currCls.lastIndexOf('.') + 1);
		if (mh.currId == null) {
			s = shortCls + " (no id)";
		} else {
			s = shortCls + " (id: <strong>" + mh.currId + "</strong>)";
		}
		Label la = new Label(s, ContentMode.HTML);
		componentContext.addComponent(la);

		if (mh.currId == null) {
			return;
		}

		final String cls = mh.currCls;
		final String id = mh.currId;
		Button b = new Button("Go to " + id + " code");
		b.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				fireGoToDefinition(id, cls);
			}
		});
		componentContext.addComponent(b);

		List<String> types = getHandlerTypesFor(cls);
		if (!types.isEmpty()) {
			componentContext.addComponent(new AddHandlerPanel(id, types));
		}
		
		List<String> datas = getDataSourceTypesFor(cls);
		if (!datas.isEmpty()) {
			componentContext.addComponent(new SetDataSourcePanel(id, datas));
		}
	}

	private class AddHandlerPanel extends Panel {
		private VerticalLayout layout = new VerticalLayout();
		public AddHandlerPanel(final String id, List<String> types) {
			super("Add Handler");
			layout.setMargin(true);
			setContent(layout);
			final NativeSelect sel = new NativeSelect(null, types);
			for (String t : types) {
				sel.setItemCaption(t, t.substring(t.lastIndexOf(".")+1));
			}
			sel.setValue(types.get(0));
			sel.setNullSelectionAllowed(false);
			layout.addComponent(sel);
			
			final TextField tf = new TextField("TODO:");
			layout.addComponent(tf);
			
			Button sb = new Button("Add Handler");
			sb.addClickListener(new ClickListener() {
				@Override
				public void buttonClick(ClickEvent event) {
					fireGoToHandler(id, (String) sel.getValue(), tf.getValue());
				}
			});
			layout.addComponent(sb);
		}
	}
	
	private class SetDataSourcePanel extends Panel {
		private VerticalLayout layout = new VerticalLayout();
		public SetDataSourcePanel(final String id, List<String> types) {
			super("Add Data Source");
			layout.setMargin(true);
			setContent(layout);
			
			final NativeSelect sel = new NativeSelect(null, types);
			for (String t : types) {
				sel.setItemCaption(t, t.substring(t.lastIndexOf(".")+1));
			}
			sel.setValue(types.get(0));
			sel.setNullSelectionAllowed(false);
			layout.addComponent(sel);
			
			final TextField tf = new TextField("TODO:");
			layout.addComponent(tf);
			
			Button sb = new Button("SetDataSource");
			sb.addClickListener(new ClickListener() {
				@Override
				public void buttonClick(ClickEvent event) {
					fireSetDataSource(id, (String)sel.getValue(), tf.getValue());
				}
			});
			layout.addComponent(sb);
		}
		
	}
	
	private static final Set<String> VALUE_CHANGE_LISTENER_FOR = new HashSet<String>(
			Arrays.asList(new String[] { "com.vaadin.addon.touchkit.ui.Switch",
					"com.vaadin.ui.CheckBox", "com.vaadin.ui.Slider",
					"com.vaadin.ui.TextField" }));

	private List<String> getHandlerTypesFor(String cls) {
		if ("com.vaadin.ui.Button".equals(cls)) {
			return Collections.singletonList("com.vaadin.ui.Button.ClickEvent");
		}
		final String naviButton = "com.vaadin.addon.touchkit.ui.NavigationButton";
		if (naviButton.equals(cls)) {
			return Collections.singletonList(naviButton
					+ ".NavigationButtonClickEvent");
		}
		if (VALUE_CHANGE_LISTENER_FOR.contains(cls)) {
			return Collections
					.singletonList("com.vaadin.data.Property.ValueChangeEvent");
		}

		// TODO: more. automatically?

		return Collections.emptyList();
	}
	
	private static final Set<String> CONTAINER_COMPONENTS = new HashSet<String>(
			Arrays.asList(new String[] { "com.vaadin.ui.Select",
					"com.vaadin.ui.Table"
					/* TODO: more or something else? */
					}));

	private List<String> getDataSourceTypesFor(String cls) {
		if ("com.vaadin.ui.Label".equals(cls)) {
			return Collections.singletonList("java.lang.String");
		}
		else if (CONTAINER_COMPONENTS.contains(cls)) {
			return Collections.singletonList("com.vaadin.data.Container");
		}
		
		// TODO: more. automatically?
		
		return Collections.emptyList();
	}

	public void addClaraEditorListener(ClaraEditorListener li) {
		listeners.add(li);
	}

	private void fireGoToDefinition(String id, String cls) {
		for (ClaraEditorListener li : listeners) {
			li.goToDefinition(id, cls);
		}
	}

	private void fireGoToHandler(String id, String cls, String todo) {
		for (ClaraEditorListener li : listeners) {
			li.goToHandler(id, cls, todo);
		}
	}
	
	private void fireSetDataSource(String id, String type, String todo) {
		for (ClaraEditorListener li : listeners) {
			li.setDataSource(id, type, todo);
		}
	}


}
