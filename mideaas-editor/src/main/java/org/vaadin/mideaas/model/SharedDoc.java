package org.vaadin.mideaas.model;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.vaadin.aceeditor.ServerSideDocDiff;
import org.vaadin.aceeditor.client.AceDoc;

public class SharedDoc {
	
	private static final ExecutorService pool = Executors.newSingleThreadExecutor();
	
	private long revision = 0;

	private List<Listener> listeners = new ArrayList<Listener>();
	private AceDoc doc;
	
	public interface Listener {
		void changed(SharedDocRevision rev);
	}
	
	public SharedDoc(AceDoc doc) {
		this.doc = doc;
	}
	

	public synchronized void register(Listener listener) {
        listeners.add(listener);
    }

    public synchronized void unregister(Listener listener) {
        listeners.remove(listener);
    }

    private synchronized List<Listener> getListeners() {
        List<Listener> listenerCopy = new ArrayList<Listener>();
        listenerCopy.addAll(listeners);
        return listenerCopy;
    }
    
    public synchronized AceDoc getDoc() {
		return doc;
	}
	
	public void applyDiff(ServerSideDocDiff diff, boolean fireChange) {
		if (diff.isIdentity()) {
			return;
		}
		long rev;
		AceDoc newDoc;
    	synchronized(this) {
    		rev = (++revision);
        	newDoc = doc = diff.applyTo(doc);
		}
    	
    	if (fireChange) {
    		SharedDocRevision sdr = new SharedDocRevision(rev, newDoc, diff);
			broadcast(sdr);
    	}
	}
	
	public void applyDiff(ServerSideDocDiff diff) {
		applyDiff(diff, true);
	}

	private void broadcast(final SharedDocRevision rev) {
        final List<Listener> listenerCopy = getListeners();
        pool.submit(new Runnable() {
            @Override
            public void run() {
                for (Listener listener : listenerCopy) {
                    listener.changed(rev);
                }
            }
        });
    }


	public void setValue(final AceDoc doc, boolean fireChange) {
		long rev;
		synchronized (this) {
			rev = (++revision);
			this.doc = doc;
		}
		if (fireChange) {
			broadcast(new SharedDocRevision(rev, doc, null));
		}
		
	}

	
}
