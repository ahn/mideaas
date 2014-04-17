package org.vaadin.mideaas.app;

import java.util.TreeSet;

import org.vaadin.chatbox.ChatBox;
import org.vaadin.chatbox.SharedChat;
import org.vaadin.chatbox.client.ChatUser;
import org.vaadin.mideaas.frontend.HorizontalUserList;
import org.vaadin.mideaas.frontend.Icons;
import org.vaadin.mideaas.model.LobbyBroadcastListener;
import org.vaadin.mideaas.model.LobbyBroadcaster;
import org.vaadin.mideaas.model.User;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;
import com.vaadin.ui.themes.Runo;

@SuppressWarnings("serial")
public class LobbyView extends VerticalLayout implements View, LobbyBroadcastListener {

	// XXX Is this the right place to store this?
	private static SharedChat chat = new SharedChat();
	
	public static SharedChat getLobbyChat() {
		return chat;
	}

	final private MideaasUI ui;
	private User user;
	
	private SelectProjectPanel selectProjectPanel;
	
	private HorizontalUserList userList;
	
	private ChatBox box = null;

	public LobbyView(MideaasUI ui) {
		this.ui = ui;
		setSizeFull();
	}
	
	@Override
	public void enter(ViewChangeEvent event) {
		user = ui.getUser();
		if (user!=null) {
			//System.out.println("loby view entered");
			setMargin(true);
			//setSizeUndefined();
			setSpacing(true);
			setSizeFull();
			initLobbyPanel();
			LobbyBroadcaster.register(this);
		}
		else {
			setMargin(false);
			addComponent(new LoginView(ui, "lobby"));
		}
	}

	
	@Override
	public void detach() {
		super.detach();
		//System.out.println("lobby viw detached");
		unregisetrFromLobbyBroadcaster();
		//LobbyBroadcaster.unregister(this);
	}
	
	@Override
	public void attach() {
		super.attach();
		//System.out.println("Lobby View attached");
	}
	 
	
	public void unregisetrFromLobbyBroadcaster() {
		LobbyBroadcaster.unregister(this);
	}

	
	private void initLobbyPanel() {

		removeAllComponents();

		addComponent(initLogoutButton(user));
		
		TabSheet tabSheet = new TabSheet();
		tabSheet.addStyleName(Runo.TABSHEET_SMALL);
		addComponent(tabSheet);
		tabSheet.setSizeFull();
		
		tabSheet.addTab(getSelectProjectPanel(),"Open Project", Icons.BOX_ARROW);
		
		tabSheet.addTab(initCreateProjectPanel(), "Create New Project");
		
		if (MideaasConfig.easiCloudsFeaturesTurnedOn()) {
			tabSheet.addTab(initUploadPanel(), "Upload Project", Icons.BOX_LABEL);
		}
		
		tabSheet.addTab(initGitProjectPanel(), "Clone Project From Git URL");
		
		setExpandRatio(tabSheet, 2);
		
		/*
		HorizontalLayout horizLayout = new HorizontalLayout();
		horizLayout.setWidth("100%");

		addComponent(initLogoutButton(user));

		addComponent(horizLayout);
		setExpandRatio(horizLayout, 1);

		VerticalLayout leftLayout = new VerticalLayout();

		VerticalLayout rightLayout = new VerticalLayout();
		leftLayout.setMargin(true);
		rightLayout.setMargin(true);
		rightLayout.setSpacing(true);

		horizLayout.addComponent(leftLayout);
		horizLayout.addComponent(rightLayout);

		leftLayout.addComponent(getSelectProjectPanel());

		rightLayout.addComponent(initCreateProjectPanel());
		if (MideaasConfig.easiCloudsFeaturesTurnedOn()) {
			rightLayout.addComponent(initUploadPanel());
		}
		rightLayout.addComponent(initGitProjectPanel());
		*/
		userList = new HorizontalUserList(MideaasUI.getLoggedInUsers());
		addComponent(userList);
		
		
		Panel chatPanel = new Panel();
		//chatPanel.setHeight("200px");
		
		VerticalLayout chatPanelLayout = new VerticalLayout();
		//chatPanelLayout.setHeight("150px");
		chatPanel.setContent(chatPanelLayout);
		
		MenuBar chatPanelMenubar = new MenuBar();
		chatPanelLayout.addComponent(chatPanelMenubar);
		chatPanelMenubar.setWidth("100%");
		
		MenuBar.Command hideChatBox = new MenuBar.Command() {
			
			@Override
			public void menuSelected(MenuItem selectedItem) {
				// TODO Auto-generated method stub				
				 if ( box.isVisible()) {
					 box.setVisible(false);
					 selectedItem.setIcon(new ThemeResource("icons/arrow-up.png"));
					 selectedItem.setDescription("Show Chat Box");
				 }
				 else {
					 box.setVisible(true);
					 selectedItem.setIcon(new ThemeResource("icons/arrow-down.png"));
					 selectedItem.setDescription("Hide Chat Box");
				 }
				 
			}
		};
		MenuItem hideChatMeuItem = chatPanelMenubar.addItem("Hide/Show", new ThemeResource("icons/arrow-down.png"), hideChatBox);
		hideChatMeuItem.setDescription("Hide Chat Box");

		MenuBar.Command hideLoggedinUsers = new MenuBar.Command() {
			
			@Override
			public void menuSelected(MenuItem selectedItem) {
				// TODO Auto-generated method stub
				if (userList.isVisible()) {
					userList.setVisible(false);
					selectedItem.setDescription("Show Users");
				}
				else {
					userList.setVisible(true);
					selectedItem.setDescription("Hide Users");
				}
			}
		};
		
		MenuItem showLogedinUsersMenuItem = chatPanelMenubar.addItem("Show/Hide Users", new ThemeResource("icons/users.png"), hideLoggedinUsers);
		showLogedinUsersMenuItem.setDescription("Hide Users");
		
		box = new ChatBox(chat);
		box.setSizeFull();
		box.setWidth("100%");
		box.setHeight("150px");
		box.setUser(new ChatUser(user.getUserId(), user.getName(), "user1"));
		box.setShowSendButton(false);
		chatPanelLayout.addComponent(box);
		
		//chatPanelLayout.setExpandRatio(box, 1);
		//Panel chatPanel = new Panel("Lobby Chat", box);
		//chatPanel.addStyleName("f-v-panel");
		//addComponent(new Panel("Lobby Chat", box));
		//addComponent(chatPanel);
		addComponent(chatPanel);
		
	}

	private Panel initGitProjectPanel() {
		Panel panel = new Panel("Or clone project from Git URL:");

		VerticalLayout layout = new VerticalLayout();

		final TextField field = new TextField();
		field.setWidth("100%");

		final Button button = new Button("Project from Git");
		button.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				ui.createGitProject(field.getValue());
			}
		});

		layout.addComponent(field);
		layout.addComponent(button);
		panel.setContent(layout);
		return panel;
	}

	private Button initLogoutButton(User user) {
		Button button = new Button("Log Out " + user.getName());
		//button.setStyleName(BaseTheme.BUTTON_LINK);
		button.setStyleName(Reindeer.BUTTON_LINK);
		button.addClickListener(new ClickListener() {
			public void buttonClick(ClickEvent event) {
				unregisetrFromLobbyBroadcaster();
				ui.logout();
			}
		});
		return button;
	}

	private SelectProjectPanel getSelectProjectPanel() {
		if (selectProjectPanel==null) {
			selectProjectPanel = new SelectProjectPanel(ui);
		}
		return selectProjectPanel;

	}

	private Panel initCreateProjectPanel() {
		return new CreateProjectPanel(ui);
	}

	private Panel initUploadPanel() {
		return new UploadProjectPanel(ui);
	}

	@Override
	public void projectsChanged() {
		getUI().access(new Runnable() {
			@Override
			public void run() {
				selectProjectPanel.update();
			}
		});
	}

	/*
	@Override
	public void loggedInUsersChanged(final Set<User> users) {
		getUI().access(new Runnable() {
			@Override
			public void run() {
				userList.setUsers(users);
				
				//Notification.show(users., "logged in", Type.TRAY_NOTIFICATION);
			}
		});
	}
	 */
	
	@Override
	public void loggedInUsersChanged(final TreeSet<User> users, final User user1, final boolean loggedin) {
		getUI().access(new Runnable() {
			@Override
			public void run() {
				userList.setUsers(users);
				
			    Notification notif = new Notification( user1.getName(), Type.TRAY_NOTIFICATION);
			    // Customize it
			    notif.setDelayMsec(1000);
			    // Show it in the page
			    
				if (loggedin) {
					notif.setDescription("logged in");
					//notif.show(ui.getPage());
				}
				else {
					//if (user.getUserId() != this.)
					
					notif.setDescription("logged out");
				}
				notif.show(ui.getPage());
			}
		});
	}
}
