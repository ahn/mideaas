package org.vaadin.mideaas.editor;

import java.util.Map;

import org.vaadin.aceeditor.AceEditor;
import org.vaadin.aceeditor.AceMode;
import org.vaadin.aceeditor.Suggester;
import org.vaadin.aceeditor.SuggestionExtension;
import org.vaadin.aceeditor.TextRange;
import org.vaadin.mideaas.editor.MultiUserDoc.DifferingChangedListener;

import com.vaadin.annotations.StyleSheet;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
@StyleSheet("mue.css")
public class MultiUserEditor extends CustomComponent implements DifferingChangedListener {

	private final EditorUser user;
	private final MultiUserDoc mud;
	private final AceMode mode;
	private final Suggester suggester;
	private MultiUserEditorTopBar topBar;
	private CollaborativeAceEditor editor;
	private EditorUser visibleUser;


	public MultiUserEditor(EditorUser user, MultiUserDoc mud, AceMode mode) {
		this(user, mud, mode, null);
	}
	
	public MultiUserEditor(EditorUser user, MultiUserDoc mud, AceMode mode, Suggester suggester) {
		this.user = user;
		this.mud = mud;
		this.mode = mode;
		this.suggester = suggester;
		addStyleName("mue");
	}
	
	@Override
	public void attach() {
		super.attach();
		mud.registerChildDoc(user);
		setVisibleUser(user);
		mud.addDifferingChangedListener(this);
	}

	@Override
	public void detach() {
		mud.removeDifferingChangedListener(this);
		if (editor != null) {
			SharedDoc doc = mud.getBase();
			if (doc != null) {
				// removing the markers here too because the diff at CollaborativeAceEditor.detached
				// don't go always got through; the Guard may block it..
				doc.applyDiff(editor.getRemoveCursorMarkersDiff());
			}
		}
		unregisterVisibleDoc();
		super.detach();
	}
	
	private void unregisterVisibleDoc() {
		if (visibleUser==user) {
			mud.unregisterChildDoc(user);
		}
	}

	private void createLayout() {
		VerticalLayout layout = new VerticalLayout();
		layout.setSizeFull();
		topBar = new MultiUserEditorTopBar(this, visibleUser);
		topBar.setDiffering(mud.getDifferences());
		layout.addComponent(topBar);
		
		SharedDoc doc = createDoc();
		if (doc != null) {
			editor = new CollaborativeAceEditor(doc, visibleUser==user ? user : null);
			configureEditor(editor);
			editor.setSizeFull();
			layout.addComponent(editor);
			layout.setExpandRatio(editor, 1);
		}
		else {
			layout.addComponent(new Label("Error: document not found"));
		}
		setCompositionRoot(layout);
	}

	private void setVisibleUser(EditorUser u) {
		visibleUser = u;
		createLayout();
	}

	protected void configureEditor(CollaborativeAceEditor ed) {
		editor.setMode(mode);
		if (suggester != null) {
			new SuggestionExtension(suggester).extend(editor);
		}
	}
	
	protected AceEditor getCurrentEditor() {
		return editor;
	}
	
	public String getCurrentText() {
		return editor==null ? null : editor.getDoc().getText();
	}
	
	public TextRange getCurrentSelection() {
		return editor==null ? null : editor.getSelection();
	}
	
	private SharedDoc createDoc() {
		if (visibleUser == null) {
			return mud.getBase();
		} 
		else {
			return mud.getChildDoc(visibleUser);
		}
	}

	@Override
	public void differingChanged(final Map<EditorUser, DocDifference> diffs) {
		UI ui = getUI();
		if (ui==null) {
			return;
		}
		ui.access(new Runnable() {
			@Override
			public void run() {
				if (isAttached()) {
					topBar.setDiffering(diffs);
				}
			}
		});
	}

	public void userClicked(EditorUser newUser) {
		setVisibleUser(newUser);
	}
	
}
