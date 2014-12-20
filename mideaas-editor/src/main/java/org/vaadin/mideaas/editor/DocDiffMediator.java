package org.vaadin.mideaas.editor;

import org.vaadin.aceeditor.ServerSideDocDiff;
import org.vaadin.aceeditor.client.AceDoc;
import org.vaadin.mideaas.editor.SharedDoc.ChangeListener;

/**
 * Mediates changes between two {@link SharedDoc} classes: upstream and downstream.
 * 
 * The {@link SharedDoc}s don't need to know about their {@link DocDiffMediator}.
 * 
 * It's possible to set guards in either direction
 * ({@link #setUpwardsGuard(Guard)} and {@link #setDownwardsGuard(Guard)}).
 *
 */
public class DocDiffMediator {
	
	public interface Guard {
		/**
		 * Whether the candidate should be accepted.
		 * 
		 * @param candidate
		 * @param diff used to produce candidate
		 * @return
		 */
		boolean isAcceptable(AceDoc candidate, ServerSideDocDiff diff);
	}
	
	public interface Filter {
		AceDoc filter(AceDoc doc);
	}

	private final Filter filter;
	private final SharedDoc upstream;
	private final SharedDoc downstream;
	private AceDoc shadow;
	private Guard upwardsGuard;
	private Guard downwardsGuard;
	
	private final ChangeListener upstreamListener = new ChangeListener() {
		@Override
		public void changed(AceDoc newDoc, ServerSideDocDiff diff) {
			upstreamChanged();
		}
	};
	
	private final ChangeListener downstreamListener = new ChangeListener() {
		@Override
		public void changed(AceDoc newDoc, ServerSideDocDiff diff) {
			downstreamChanged();
		}
	};
	
	public DocDiffMediator(Filter filter, SharedDoc upstream, SharedDoc downstream) {
		this.filter = filter;
		this.upstream = upstream;
		this.downstream = downstream;
		shadow = filtered(upstream.getDoc());
		
		upstream.addListener(upstreamListener);
		downstream.addListener(downstreamListener);
	}
	
	private AceDoc filtered(AceDoc doc) {
		return filter != null ? filter.filter(doc) : doc;
	}
	
	public void stop() {
		upstream.removeListener(upstreamListener);
		downstream.removeListener(downstreamListener);
	}
	
	public SharedDoc getUpstream() {
		return upstream;
	}
	
	public SharedDoc getDownstream() {
		return downstream;
	}
	
	public synchronized void setUpwardsGuard(Guard guard) {
		upwardsGuard = guard;
	}
	
	public synchronized void setDownwardsGuard(Guard guard) {
		downwardsGuard = guard;
	}

	private void upstreamChanged() {
		tryToApplyFrom(upstream, downstream, downwardsGuard);
	}
	
	private void downstreamChanged() {
		tryToApplyFrom(downstream, upstream, upwardsGuard);
	}
	
	private void tryToApplyFrom(SharedDoc fromDoc, SharedDoc toDoc, Guard guard) {
		AceDoc destDoc = null;
		ServerSideDocDiff diff;
		synchronized (upstream) {
		synchronized (downstream) {
		synchronized(this) {
			AceDoc sourceDoc = filtered(fromDoc.getDoc());
			diff = ServerSideDocDiff.diff(shadow,sourceDoc);
			if (!diff.isIdentity()) {
				//System.out.println(this + " " + d.toString());
				destDoc = diff.applyTo(toDoc.getDoc());
				if (guard==null || guard.isAcceptable(destDoc, diff)) {
					shadow = sourceDoc;
					destDoc = toDoc.setDocNoFire(destDoc);
				}
				else {
					destDoc = null;
				}
			}
		}
		}
		}
		if (destDoc!=null) {
			toDoc.fireChanged(destDoc, diff);
		}
	}

	public void detach() {
		upstream.removeListener(upstreamListener);
		downstream.removeListener(downstreamListener);
	}

}
