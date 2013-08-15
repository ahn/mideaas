package org.vaadin.mideaas.app;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.apache.commons.io.FileUtils;
import org.vaadin.mideaas.frontend.MenuBarUtil;
import org.vaadin.mideaas.frontend.MideaasEditorPlugin;
import org.vaadin.mideaas.model.SharedProject;
import org.vaadin.mideaas.model.User;
import org.vaadin.mideaas.model.ZipUtils;

import com.vaadin.server.FileDownloader;
import com.vaadin.server.FileResource;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.ui.Button;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;

public class ZipPlugin implements MideaasEditorPlugin {

	private final SharedProject project;
//	private final User user;
	
	public ZipPlugin(SharedProject project, User user) {
		this.project = project;
//		this.user = user;
	}
	
	@Override
	public void extendMenu(MenuBar menuBar) {
	    Runnable zipRunnable = createzipRunnable(project.getName());
	    MenuBarUtil.addMenuItem(menuBar, "Zip", zipRunnable);
	}
	
    

	
	/**
	 * Cretezip runnable is able to zip the project.
	 *
	 * @param projectName the name of the project to be zipped
	 * @return the runnable that is able to zip the project
	 */
	private Runnable createzipRunnable(final String projectName) {
		return new Runnable() {
    		@Override
    		public void run() {
				zipProject();
    		}};
	}

	/**
	 * Zips project.
	 *
	 * @param projectName the name of the project to be zipped
	 */
	protected void zipProject() {
		try {
			File zipFile = File.createTempFile("mideaas-"+project.getName(), ".zip");
			
			zipProjectToFile(zipFile);
			FileResource zip = new FileResource(zipFile);
			FileDownloader fd = new FileDownloader(zip);
			Button downloadButton = new Button("Download project");
			fd.extend(downloadButton);

			//filedonwnloader can not be connected to menuitem :( So I connected it to popupwindow :)
			Window zipButtonWindow = new Window();
			zipButtonWindow.setCaption("Zip and download project");
			zipButtonWindow.setWidth(200, Unit.PIXELS);
			zipButtonWindow.setContent(downloadButton);
			UI.getCurrent().addWindow(zipButtonWindow);
		} catch (IOException e) {
			e.printStackTrace();
			Notification.show("Error: " + e.getMessage(), Notification.Type.ERROR_MESSAGE);
		}

	}
	
	
	public void zipProjectToFile(File destZipFile) throws IOException {
		File tmp = Files.createTempDirectory("zip").toFile();
		File dir = new File(tmp, project.getName());
		File gitDir = new File(project.getProjectDir(), ".git");
		if (gitDir.isDirectory()) {
			FileUtils.copyDirectory(gitDir, new File(dir, ".git"));
		}
		synchronized(project) { // XXX???
			project.writeToDiskIncludingInitial(dir);
			ZipUtils.zipDir(dir, destZipFile);
		}
		FileUtils.deleteDirectory(tmp);
	}
	

}
