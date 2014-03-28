package org.vaadin.mideaas.frontend;

import java.util.Collections;

import org.vaadin.mideaas.model.SharedProject;

import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.UI;

@SuppressWarnings("serial")
public class MenuBarUtil {
    
    public static void addBuildMenu(MenuBar menuBar, final Builder builder, final String buildDir) {
        MenuItem buildMenu = menuBar.addItem("Maven", null);
        
        buildMenu.addItem("compile", new Command() {    
            @Override
            public void menuSelected(MenuItem selectedItem) {
                builder.build(Collections.singletonList("package"), buildDir, null, null);
            }
        });
        
        buildMenu.addItem("package", new Command() {    
            @Override
            public void menuSelected(MenuItem selectedItem) {
                builder.build(Collections.singletonList("package"), buildDir, null, null);
            }
        });
        
        buildMenu.addItem("clean", new Command() {    
            @Override
            public void menuSelected(MenuItem selectedItem) {
                builder.build(Collections.singletonList("clean"), buildDir, null, null);
            }
        });
        
    }
    
    public static void addAddonMenu(MenuBar menuBar, final SharedProject project) {
    	MenuItem menu = menuBar.addItem("Dependencies", null);
    	menu.addItem("Manage", new Command() {
			@Override
			public void menuSelected(MenuItem selectedItem) {
				UI.getCurrent().addWindow(new AddonManagementWindow(project));
			}
    	});
    }

	public static void addMenuItem(final MenuBar menuBar, final String text, final Runnable runnable) {
    	menuBar.addItem(text, new Command() {
			@Override
			public void menuSelected(MenuItem selectedItem) {
				runnable.run();
			}
    	});
	}
	
	public static void addMenuItemBefore(final MenuBar menuBar, final String text, final Runnable runnable, MenuItem item) {
    	menuBar.addItemBefore(text, null, new Command() {
			@Override
			public void menuSelected(MenuItem selectedItem) {
				runnable.run();
			}
    	}, item);
	}

	public static void addZipMenu(MenuBar menuBar, final Runnable zipProject) {
    	menuBar.addItem("Zip", new Command() {
			@Override
			public void menuSelected(MenuItem selectedItem) {
				zipProject.run();
			}
    	});		
	}

	public static void addPanicMenu(MenuBar menuBar, final SharedProject project, final Builder builder, final String buildDir) {
		MenuItem menu = menuBar.addItem("Panic", null);
    	menu.addItem("Clean project", new Command() {
			@Override
			public void menuSelected(MenuItem selectedItem) {
				builder.build(Collections.singletonList("clean"), buildDir, null, null);
			}
    	});
    	menu.addItem("Compile all", new Command() {
			@Override
			public void menuSelected(MenuItem selectedItem) {
				project.compileAll();
			}
    	});
    	menu.addItem("Stop all Jetty servers", new Command() {
			@Override
			public void menuSelected(MenuItem selectedItem) {
				JettyUtil.stopAllJettys();
			}
    	});
	}
    
    // TODO: separate git from this ui component
    
//    public void addGitMenu() {
//        MenuItem gitMenu = addItem("Git", null);
//        
//        gitInit = gitMenu.addItem("init", new Command() {
//            @Override
//            public void menuSelected(MenuItem selectedItem) {
//                try {
//                    project.initializeGit();
//                    updateGitMenu();
//                } catch (IllegalStateException e) {
//                    Notification.show(e.getMessage(), Notification.Type.ERROR_MESSAGE);
//                } catch (IOException e) {
//                    Notification.show(e.getMessage(), Notification.Type.ERROR_MESSAGE);
//                } catch (NoFilepatternException e) {
//                    Notification.show(e.getMessage(), Notification.Type.ERROR_MESSAGE);
//                } catch (GitAPIException e) {
//                    Notification.show(e.getMessage(), Notification.Type.ERROR_MESSAGE);
//                }
//            }
//            
//        });
//        
//        gitLog = gitMenu.addItem("log", new Command() {
//            @Override
//            public void menuSelected(MenuItem selectedItem) {
//                GitLogWindow win = new GitLogWindow(project);
//                UI.getCurrent().addWindow(win);
//            }
//            
//        });
//        
//        gitCommit = gitMenu.addItem("commit", new Command() {
//            @Override
//            public void menuSelected(MenuItem selectedItem) {
//                GitCommitWindow win = new GitCommitWindow(project);
//                UI.getCurrent().addWindow(win);
//            }
//            
//        });
//        
//        gitPush = gitMenu.addItem("push", new Command() {
//            @Override
//            public void menuSelected(MenuItem selectedItem) {
//                GitPushWindow win = new GitPushWindow(project);
//                UI.getCurrent().addWindow(win);
//            }
//            
//        });
//        
//        updateGitMenu();
//    }
    
    
    
//    private void updateGitMenu() {
//        boolean git = project.getGit()!=null;
//        gitInit.setEnabled(!git);
//        gitLog.setEnabled(git);
//        gitCommit.setEnabled(git);
//        gitPush.setEnabled(git);
//    }
}
