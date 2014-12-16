package org.vaadin.mideaas.app.git;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.vaadin.mideaas.app.Icons;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Image;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class GitProjectLoader extends CustomComponent {
	
	public interface ProjectLoaderListener {
		public void success(File dir);
		public void failure(String reason);
	}
	
	private VerticalLayout layout = new VerticalLayout();
	
	public GitProjectLoader() {
		layout.addComponent(new Image(null, Icons.LOADING_INDICATOR));
		setCompositionRoot(layout);
	}
	
	public void initializeProjectFromGit(final String gitUrl, final String username, final String password, final ProjectLoaderListener li) {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				gitInit(gitUrl, username, password, li);
			}
		});
		t.start();
	}

	private void gitInit(String gitUrl, String username, String password, ProjectLoaderListener li) {
		try {
			File dir = Files.createTempDirectory("mideaas").toFile();
			System.out.println(gitUrl + " --> " + dir);
			GitRepository.cloneFrom(gitUrl, dir, username, password);
			fireSuccess(dir, li);
		} catch (IOException e) {
			e.printStackTrace();
			fireFailure("IO error", li);
		} catch (GitAPIException e) {
			e.printStackTrace();
			fireFailure("Git error", li);
		}
	}

	private void fireSuccess(final File dir, final ProjectLoaderListener li) {
		getUI().access(new Runnable() {
			@Override
			public void run() {
				li.success(dir);
			}
		});
	}
	
	private void fireFailure(final String reason, final ProjectLoaderListener li) {
		getUI().access(new Runnable() {
			@Override
			public void run() {
				li.failure(reason);
			}
		});
	}
}
