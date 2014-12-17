package org.vaadin.mideaas.app;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.vaadin.aceeditor.ServerSideDocDiff;
import org.vaadin.aceeditor.client.AceDoc;
import org.vaadin.mideaas.editor.SharedDoc;
import org.vaadin.mideaas.editor.SharedDoc.ChangeListener;
import org.vaadin.mideaas.ide.IdeUI;

public class FileChangeSocketSender {
	
	private static final Logger log = Logger.getLogger(FileChangeSocketSender.class.getName());

	private Socket socket;
	private final String root;
	private final HashMap<SharedDoc, SharedDoc.ChangeListener> following = new HashMap<SharedDoc, SharedDoc.ChangeListener>();

	
	public FileChangeSocketSender(String host, int port) throws IOException {
		socket = new Socket(host, port);
		root = ((IdeUI)IdeUI.getCurrent()).getServerRootUrl();
	}
	
	public boolean isOk() {
		return socket != null;
	}
	
	public synchronized void follow(final String id, SharedDoc doc) {
		SharedDoc.ChangeListener li = new SharedDoc.ChangeListener() {
			@Override
			public void changed(AceDoc newDoc, ServerSideDocDiff diff) {
				int n = (diff==null) ? 0 : diff.getPatches().size();
				if (n > 0) {
					docChanged(id);
				}
			}
		};
		following.put(doc, li);
		doc.addListener(li);
	}
	
	public synchronized void unfollow(SharedDoc doc) {
		ChangeListener li = following.remove(doc);
		if (li != null) {
			doc.removeListener(li);
		}
	}
	
	private synchronized void docChanged(String id) {
		if (socket == null) {
			return;
		}
		PrintWriter out;
		try {
			out = new PrintWriter(socket.getOutputStream(), true);
			out.println(root + "raw/" + id);
		} catch (IOException e) {
			e.printStackTrace();
			log.warning("Could not write to socket.");
		}
	}
	
	public synchronized void stop() {
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
			log.warning("Could not close socket.");
		}
		socket = null;

		for (Entry<SharedDoc, ChangeListener> e : following.entrySet()) {
			e.getKey().removeListener(e.getValue());
		}
		following.clear();
	}

}
