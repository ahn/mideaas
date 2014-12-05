package org.vaadin.mideaas.app.git;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.vaadin.mideaas.ide.IdeUtil;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class GitProjectLoader extends CustomComponent {
	
	public interface ProjectLoaderListener {
		public void success(Map<String, String> contents);
		public void failure(String reason);
	}
	
	private VerticalLayout layout = new VerticalLayout();
	
	public GitProjectLoader() {
		
		layout.addComponent(new Label("Loading..."));
		
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
			Map<String, String> contents = IdeUtil.readContentsFromDir(dir);
			fireSuccess(contents, li);
		} catch (IOException e) {
			e.printStackTrace();
			fireFailure("IO error", li);
		} catch (GitAPIException e) {
			e.printStackTrace();
			fireFailure("Git error", li);
		}
	}

	private void fireSuccess(final Map<String, String> contents, final ProjectLoaderListener li) {
		getUI().access(new Runnable() {
			@Override
			public void run() {
				li.success(contents);
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
