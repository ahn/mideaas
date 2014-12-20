package org.vaadin.mideaas.editor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.vaadin.aceeditor.AceEditor;
import org.vaadin.aceeditor.AceEditor.DiffEvent;
import org.vaadin.aceeditor.AceEditor.DiffListener;
import org.vaadin.aceeditor.ServerSideDocDiff;
import org.vaadin.aceeditor.TextRange;
import org.vaadin.aceeditor.client.AceAnnotation;
import org.vaadin.aceeditor.client.AceAnnotation.MarkerAnnotation;
import org.vaadin.aceeditor.client.AceDoc;
import org.vaadin.aceeditor.client.AceMarker;
import org.vaadin.aceeditor.client.AceRange;
import org.vaadin.mideaas.editor.AsyncErrorChecker.ResultListener;
import org.vaadin.mideaas.editor.ErrorChecker.Error;

import com.vaadin.ui.UI;

/**
 * An {@link AceDoc} to be collaboratively edited by {@link AceEditor}s. 
 *
 */
public class SharedDoc implements DiffListener, ResultListener {
	
	public interface ChangeListener {
		public void changed(AceDoc newDoc, ServerSideDocDiff diff);
	}

	private final CopyOnWriteArrayList<ChangeListener> listeners = new CopyOnWriteArrayList<ChangeListener>();

	private LinkedList<AceEditor> editors = new LinkedList<AceEditor>();
	
	private AceDoc doc;
	private final AsyncErrorChecker checker;

	public SharedDoc(AceDoc doc, AsyncErrorChecker checker) {
		this.doc = doc;
		this.checker = checker;
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

	public void applyDiff(ServerSideDocDiff diff) {
		if (diff.isIdentity()) {
			return;
		}
		AceDoc oldDoc = getDoc();
		AceDoc newDoc = applyDiffNoFire(diff);
		if (newDoc!=null) {
			fireChanged(newDoc, diff);
			if (!newDoc.getText().equals(oldDoc.getText())) {
				startErrorCheck();
			}
		}
	}

	public synchronized AceDoc applyDiffNoFire(ServerSideDocDiff diff) {
		return setDocNoFire(diff.applyTo(doc));
	}

	synchronized AceDoc setDocNoFire(final AceDoc doc) {
		
		boolean textChanged;
		
		synchronized (this) {
			
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
			
			textChanged = !this.doc.getText().equals(doc.getText());
			
			this.doc = doc;
			
			for (final AceEditor editor : editors) {
				UI ui = editor.getUI();
				if (ui == null) {
					continue;
				}
				ui.access(new Runnable() {
					@Override
					public void run() {
						if (editor.isAttached()) {
							editor.setDoc(doc);
						}
					}
				});
			}
		
		}
		
		if (textChanged) {
			startErrorCheck();
		}
		
		
		return doc;
	}
	
	
	public void setDoc(AceDoc doc) {
		AceDoc newDoc = setDocNoFire(doc);
		if (newDoc!=null) {
			fireChanged(newDoc, null);
			if (!newDoc.getText().equals(doc.getText())) {
				startErrorCheck();
			}
		}
	}
	
	public synchronized AceDoc getDoc() {
		return doc;
	}

	public void addListener(ChangeListener li) {
		listeners.add(li);
	}
	
	public void removeListener(ChangeListener li) {
		listeners.remove(li);
	}
	
	public void fireChanged(AceDoc newDoc, ServerSideDocDiff diff) {
		for (ChangeListener li : listeners) {
			li.changed(newDoc, diff);
		}
	}

	@Override
	public void diff(DiffEvent e) {
		applyDiff(e.getDiff());
	}
	
	private void startErrorCheck() {
		if (checker == null) {
			return;
		}
		// TODO: possible concurrency problems
		// a brief windows in which changes by somebody else are
		// overwritten, because of setDoc instead of using diffs...
		//setDoc(docWithoutErrorMarkers(getDoc()));
		final String text = getDoc().getText();
		checker.checkErrors(text, new ResultListener() {
			@Override
			public void errorsChecked(List<Error> errors) {
				AceDoc docNow = getDoc();
				if (text.equals(docNow.getText())) {
					setDoc(docWithErrors(docNow, errors));
				}
				else {
					setDoc(docWithoutErrorMarkers(docNow));
				}
			}
		});
	}

	public synchronized DocDiffMediator fork() {
		SharedDoc fork = new SharedDoc(doc, checker);
		return new DocDiffMediator(null, this, fork); // TODO???
	}

	@Override
	public void errorsChecked(List<Error> errors) {
		setDoc(docWithErrors(getDoc(), errors));
	}


	// TODO: this error stuff could be moved to some utility class

	private static long latestMarkerId = 0L;
	private static synchronized String newMarkerId() {
		return "error-" + (++latestMarkerId);
	}

	private static AceDoc docWithErrors(AceDoc doc, List<Error> errors) {
		HashMap<String, AceMarker> markers = new HashMap<String, AceMarker>(errors.size());
		HashSet<MarkerAnnotation> manns = new HashSet<MarkerAnnotation>(errors.size());
		for (Error err : errors) {
			AceMarker m = markerFromError(newMarkerId(), err, doc.getText());
			markers.put(m.getMarkerId(), m);
			AceAnnotation ann = new AceAnnotation(err.message, AceAnnotation.Type.error);
			manns.add(new MarkerAnnotation(m.getMarkerId(), ann));
		}
		return doc.withAdditionalMarkers(markers).withMarkerAnnotations(manns);		
	}

	private static AceMarker markerFromError(String markerId, Error e, String text) {
		AceRange range = new TextRange(text, e.start, e.start==e.end ? e.start+1 : e.end);
		String cssClass = "myerrormarker1";
		AceMarker.Type type = AceMarker.Type.text;
		boolean inFront = true;
		AceMarker.OnTextChange onChange = AceMarker.OnTextChange.REMOVE;
		return new AceMarker(markerId, range, cssClass, type, inFront, onChange);
	}
	
	public static AceDoc docWithoutErrorMarkers(AceDoc doc) {
		HashSet<String> ems = new HashSet<String>();
		for (String m : doc.getMarkers().keySet()) {
			if (m.startsWith("error-")) {
				ems.add(m);
			}
		}
		return doc.withoutMarkers(ems);
	}
	
	
}
