package org.vaadin.mideaas.editor;

import java.util.Map;
import java.util.Map.Entry;

import org.vaadin.aceeditor.AceEditor.SelectionChangeListener;
import org.vaadin.aceeditor.AceEditor;
import org.vaadin.aceeditor.AceMode;
import org.vaadin.aceeditor.SuggestionExtension;
import org.vaadin.aceeditor.TextRange;
import org.vaadin.aceeditor.client.AceDoc;
import org.vaadin.mideaas.editor.MultiUserDoc.DifferingChangedListener;

import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class MultiUserEditor extends CustomComponent implements DifferingChangedListener {

	private final EditorUser user;
	private final MultiUserDoc mud;
	private MultiUserEditorTopBar topBar;
	private CollaborativeAceEditor editor;
	private EditorUser visibleUser;

	private AceMode aceMode;
	private SuggestionExtension suggestionExtension;
	private boolean wordWrap;
	private SelectionChangeListener selectionChangeListener;
	private TextChangeListener textChangeListener;

	public MultiUserEditor(EditorUser user, MultiUserDoc mud) {
		this.user = user;
		this.mud = mud;
	}
	
	@Override
	public void attach() {
		super.attach();
		setVisibleUser(user);
		mud.addDifferingChangedListener(this);
	}

	@Override
	public void detach() {
		mud.removeDifferingChangedListener(this);
		super.detach();
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
//		ed.setWordWrap(wordWrap);
//		ed.setMode(aceMode);
//		if (visibleUser == user && suggestionExtension != null) {
//			suggestionExtension.extend(ed);
//		}
//		if (selectionChangeListener != null) {
//			ed.addSelectionChangeListener(selectionChangeListener);
//		}
//		if (textChangeListener != null) {
//			ed.addTextChangeListener(null);
//		}
//		return ed;
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
		else if (visibleUser == user) {
			return mud.getChildDocCreateIfNeeded(user);
		}
		else {
			return mud.getChildDoc(visibleUser);
		}
	}

	@Override
	public void differingChanged(final Map<EditorUser, DocDifference> diffs) {
		System.out.println(this + " differing changed " + diffs.size());
		for (Entry<EditorUser, DocDifference> e : diffs.entrySet()) {
			System.out.println(e.getValue());
		}
		getUI().access(new Runnable() {
			@Override
			public void run() {
				topBar.setDiffering(diffs);
			}
		});
	}

	public void userClicked(EditorUser newUser) {
		setVisibleUser(newUser);
	}

	/*
	public void setMode(AceMode aceMode) {
		this.aceMode = aceMode;
	}

	public void setSuggestionExtension(SuggestionExtension suggestionExtension) {
		this.suggestionExtension = suggestionExtension;
		
	}

	public void setWordWrap(boolean wordWrap) {
		this.wordWrap = wordWrap;
		
	}

	public void setSelectionChangeListener(SelectionChangeListener li) {
		this.selectionChangeListener = li;
		
	}

	public void setTextChangeListener(TextChangeListener li) {
		this.textChangeListener = li;
	}

	public String getCurrentText() {
		return editor.getDoc().getText();
	}

	public TextRange getSelection() {
		return editor.getSelection();
	}
	*/

	
}
