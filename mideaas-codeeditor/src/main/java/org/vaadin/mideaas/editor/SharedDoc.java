package org.vaadin.mideaas.editor;

import java.util.LinkedList;
import java.util.concurrent.CopyOnWriteArrayList;

import org.vaadin.aceeditor.AceEditor;
import org.vaadin.aceeditor.AceEditor.DiffEvent;
import org.vaadin.aceeditor.AceEditor.DiffListener;
import org.vaadin.aceeditor.ServerSideDocDiff;
import org.vaadin.aceeditor.client.AceDoc;

/**
 * An {@link AceDoc} to be collaboratively edited by {@link AceEditor}s. 
 *
 */
public class SharedDoc implements DiffListener {
	
	public interface Listener {
		public void changed();
	}

	private final CopyOnWriteArrayList<Listener> listeners = new CopyOnWriteArrayList<Listener>();

	private LinkedList<AceEditor> editors = new LinkedList<AceEditor>();
	
	private AceDoc doc;

	public SharedDoc(AceDoc doc) {
		this.doc = doc;
	}
	
	public synchronized void attachEditor(final AceEditor editor) {
		boolean wasReadonly = editor.isReadOnly();
		editor.setReadOnly(false);
		editor.setDoc(getDoc());
		editor.setReadOnly(wasReadonly);
		
		editors.add(editor);
		editor.addDiffListener(this);
	}
	
	public synchronized void detachEditor(AceEditor editor) {
		editors.remove(editor);
		editor.removeDiffListener(this);
	}
	
	private void applyDiff(ServerSideDocDiff diff) {
		if (diff.isIdentity()) {
			return;
		}
		AceDoc newDoc = applyDiffNoFire(diff);
		if (newDoc!=null) {
			fireChanged();
		}
	}

	private synchronized AceDoc applyDiffNoFire(ServerSideDocDiff diff) {
		return setDocNoFire(diff.applyTo(doc));
	}
	
	synchronized AceDoc setDocNoFire(final AceDoc doc) {
		if (this.doc.equals(doc)) {
			return null;
		}
		
		// XXX
//		try {
//			Thread.sleep(new Random().nextInt(1000));
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		
		this.doc = doc;
		for (final AceEditor editor : editors) {
			editor.getUI().access(new Runnable() {
				@Override
				public void run() {
					editor.setDoc(doc);
				}
			});
		}
		return doc;
	}
	
	
	public void setDoc(AceDoc doc) {
		AceDoc newDoc = setDocNoFire(doc);
		if (newDoc!=null) {
			fireChanged();
		}
	}
	
	public synchronized AceDoc getDoc() {
		return doc;
	}

	public void addListener(Listener li) {
		listeners.add(li);
	}
	
	public void removeListener(Listener li) {
		listeners.remove(li);
	}
	
	public void fireChanged() {
		for (Listener li : listeners) {
			li.changed();
		}
	}

	@Override
	public void diff(DiffEvent e) {
		applyDiff(e.getDiff());
	}

	
}
