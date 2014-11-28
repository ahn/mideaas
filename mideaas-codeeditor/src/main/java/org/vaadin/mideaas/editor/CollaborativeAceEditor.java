package org.vaadin.mideaas.editor;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import name.fraser.neil.plaintext.diff_match_patch.Patch;

import org.vaadin.aceeditor.AceEditor;
import org.vaadin.aceeditor.AceEditor.SelectionChangeListener;
import org.vaadin.aceeditor.ServerSideDocDiff;
import org.vaadin.aceeditor.client.AceAnnotation.MarkerAnnotation;
import org.vaadin.aceeditor.client.AceAnnotation.RowAnnotation;
import org.vaadin.aceeditor.client.AceDoc;
import org.vaadin.aceeditor.client.AceMarker;
import org.vaadin.aceeditor.client.AceMarker.OnTextChange;
import org.vaadin.aceeditor.client.AceMarker.Type;
import org.vaadin.aceeditor.client.AceRange;
import org.vaadin.aceeditor.client.MarkerAddition;
import org.vaadin.aceeditor.client.MarkerSetDiff;
import org.vaadin.aceeditor.client.SetDiff;
import org.vaadin.aceeditor.client.TransportDoc.TransportMarkerAnnotation;
import org.vaadin.aceeditor.client.TransportDoc.TransportRowAnnotation;

import com.vaadin.annotations.StyleSheet;


@SuppressWarnings("serial")
@StyleSheet("ace-markers.css")
public class CollaborativeAceEditor extends AceEditor implements SelectionChangeListener {
	
	private static long lastId = 0L;
	private static long newId() {
		return ++lastId;
	}

	private final SharedDoc sharedText;
	//private final long id = newId();
	
	private final EditorUser user;
	private final String userId;
	
	private final Set<String> myMarkers;
	
	public CollaborativeAceEditor(SharedDoc value, EditorUser user) {
		super();
		this.sharedText = value;
		this.user = user;
		userId = user==null ? null : user.getId();
		myMarkers = new HashSet<String>(Arrays.asList(new String[] {"cursorrow-"+userId, "cursor-"+userId, "cursor-col0-"+userId }));
		setReadOnly(user==null);
		setUseWorker(false);
	}
	
	public SharedDoc getSharedText() {
		return sharedText;
	}
	
	@Override
	public void setDoc(AceDoc doc) {
		super.setDoc(doc.withoutMarkers(myMarkers));
	}
	
	public long getStyleIndex() {
		return user!=null ? user.getStyleIndex() : -1; // ?
	}
	
	@Override
	public void attach() {
		super.attach();
		sharedText.attachEditor(this);
		if (user!=null) {
			AceRange range = getSelection();
			sharedText.applyDiff(newRowDiff(range));
			sharedText.applyDiff(newCursorDiff(range));
			this.addSelectionChangeListener(this);
		}
	}

	@Override
	public void detach() {
		sharedText.detachEditor(this);
		if (user!=null) {
			this.removeSelectionChangeListener(this);
			sharedText.applyDiff(removeMarkersDiff(myMarkers));
		}
		super.detach();
	}
	
	private static String rowCssClass(long styleIndex) {
		return "acecursorrow-" + styleIndex;
	}
	
	private static String cursorCssClass(long styleIndex, boolean col0) {
		return col0 ? "acecursor-col0-" + styleIndex : "acecursor-" + styleIndex;
	}
	
	private static ServerSideDocDiff newMarkerDiff(AceMarker m, String text, String removeMarkerId) {
		Set<String> removed;
		if (removeMarkerId==null) {
			removed = Collections.emptySet();
		}
		else {
			removed = Collections.singleton(removeMarkerId);
		}
		Map<String, MarkerAddition> added = Collections.singletonMap(m.getMarkerId(), new MarkerAddition(m, text));
		MarkerSetDiff msd = new MarkerSetDiff(added, removed);
		SetDiff<MarkerAnnotation,TransportMarkerAnnotation> mad = new SetDiff<MarkerAnnotation, TransportMarkerAnnotation>();
		ServerSideDocDiff diff = ServerSideDocDiff.newMarkersAndAnnotations(msd, mad);
		return diff;
	}
	
	public ServerSideDocDiff getRemoveCursorMarkersDiff() {
		return removeMarkersDiff(myMarkers);
	}

	private static ServerSideDocDiff removeMarkersDiff(Set<String> markerIds) {
		Map<String, MarkerAddition> added = Collections.emptyMap();
		MarkerSetDiff msd = new MarkerSetDiff(added, markerIds);
		return new ServerSideDocDiff(new LinkedList<Patch>(), msd, new SetDiff<RowAnnotation, TransportRowAnnotation>(), new SetDiff<MarkerAnnotation, TransportMarkerAnnotation>());
	}

	@Override
	public void selectionChanged(SelectionChangeEvent e) {
		AceRange range = e.getSelection();
		sharedText.applyDiff(newRowDiff(range));
		sharedText.applyDiff(newCursorDiff(range));
	}

	private ServerSideDocDiff newCursorDiff(AceRange range) {
		int row = range.getEndRow();
		int col = range.getEndCol();
		AceRange r;
		boolean col0 = col==0;;
		if (col0) {
			r = new AceRange(row, col, row, col+1);
			AceMarker m = new AceMarker("cursor-col0-"+userId, r, cursorCssClass(getStyleIndex(),col0), Type.cursor, true, OnTextChange.ADJUST);
			return newMarkerDiff(m, getValue(), "cursor-"+userId);
		}
		else {
			r = new AceRange(row, col-1, row, col);
			AceMarker m = new AceMarker("cursor-"+userId, r, cursorCssClass(getStyleIndex(),col0), Type.cursor, true, OnTextChange.ADJUST);
			return newMarkerDiff(m, getValue(), "cursor-col0-"+userId);
		}
		
	}

	private ServerSideDocDiff newRowDiff(AceRange range) {
		AceRange r = new AceRange(range.getEndRow(), 0, range.getEndRow()+1, 0);
		AceMarker m = new AceMarker("cursorrow-"+userId, r, rowCssClass(getStyleIndex()), Type.cursorRow, false, OnTextChange.ADJUST);
			
		return newMarkerDiff(m, getValue(), null);
	}
	

	
}
