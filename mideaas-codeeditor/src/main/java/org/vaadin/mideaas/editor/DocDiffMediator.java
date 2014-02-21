package org.vaadin.mideaas.editor;

import org.vaadin.aceeditor.ServerSideDocDiff;
import org.vaadin.aceeditor.client.AceDoc;
import org.vaadin.mideaas.editor.SharedDoc.Listener;

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

	private final SharedDoc upstream;
	private final SharedDoc downstream;
	private AceDoc shadow;
	private Guard upwardsGuard;
	private Guard downwardsGuard;
	
	private final Listener upstreamListener = new Listener() {
		@Override
		public void changed() {
			upstreamChanged();
		}
	};
	
	private final Listener downstreamListener = new Listener() {
		@Override
		public void changed() {
			downstreamChanged();
		}
	};
	
	public DocDiffMediator(SharedDoc upstream, SharedDoc downstream) {
		this.upstream = upstream;
		this.downstream = downstream;
		shadow = upstream.getDoc();
		
		upstream.addListener(upstreamListener);
		downstream.addListener(downstreamListener);
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
		synchronized (upstream) {
		synchronized (downstream) {
		synchronized(this) {
			AceDoc sourceDoc = fromDoc.getDoc();
			ServerSideDocDiff d = ServerSideDocDiff.diff(shadow,sourceDoc);
			if (!d.isIdentity()) {
				destDoc = d.applyTo(toDoc.getDoc());
				if (guard==null || guard.isAcceptable(destDoc, d)) {
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
			toDoc.fireChanged();
		}
	}

	public void detach() {
		upstream.removeListener(upstreamListener);
		downstream.removeListener(downstreamListener);
	}	

}
