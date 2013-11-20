package org.vaadin.mideaas.app;

import org.vaadin.chatbox.ChatBox;
import org.vaadin.chatbox.SharedChat;
import org.vaadin.chatbox.client.ChatUser;
import org.vaadin.mideaas.model.User;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.BaseTheme;

@SuppressWarnings("serial")
public class LobbyPanel extends Panel {
    
	// XXX Is this the right place to store this?
	private static SharedChat chat = new SharedChat();
	
    final private MideaasUI ui;
    private User user;
    
    private VerticalLayout layout = new VerticalLayout();
    private HorizontalLayout horizLayout = new HorizontalLayout();
	
    public LobbyPanel(MideaasUI ui, User user) {
        this.ui = ui;
        this.user = user;
        this.setContent(layout);
        this.setSizeFull();
        this.initLobbyPanel();
    }

    @Override
    public void detach() {
        super.detach();
    }

    private void initLobbyPanel(){
		layout.removeAllComponents();
		layout.setSizeFull();
//        horizLayout.setSizeFull();
		horizLayout.setWidth("100%");
        
        layout.addComponent(initLogoutButton(user));
        
        layout.addComponent(horizLayout);
        layout.setExpandRatio(horizLayout, 1);
        
        

		VerticalLayout leftLayout = new VerticalLayout();
        VerticalLayout rightLayout = new VerticalLayout();
        leftLayout.setMargin(true);
        rightLayout.setMargin(true);
//        rightLayout.setSizeFull();
        rightLayout.setSpacing(true);

        
        horizLayout.addComponent(leftLayout);
        horizLayout.addComponent(rightLayout);		
 
        
        leftLayout.addComponent(getSelectProjectPanel());
        
        rightLayout.addComponent(initCreateProjectPanel());
		rightLayout.addComponent(initUploadPanel());
		rightLayout.addComponent(initGitProjectPanel());
		ChatBox box = new ChatBox(chat);
		box.setSizeFull();
		box.setWidth("100%");
		box.setHeight("200px");
		box.setUser(new ChatUser(user.getUserId(), user.getName(), "user1"));
		box.setShowSendButton(false);
		layout.addComponent(new Panel("Lobby Chat", box));
    }
    
    private Panel initGitProjectPanel(){
    	Panel panel = new Panel("Or clone project from Git URL:");

    	VerticalLayout layout = new VerticalLayout();
		
        final TextField field = new TextField("https://github.com/ahn/hehe.git");
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
    
    private Button initLogoutButton(User user){
		Button button = new Button("Log Out "+ user.getName());
		button.setStyleName(BaseTheme.BUTTON_LINK);
		button.addClickListener(new ClickListener() {
			public void buttonClick(ClickEvent event) {
				ui.logout();
			}
		});
		return button;
    }

    private SelectProjectPanel getSelectProjectPanel(){
		SelectProjectPanel panel = new SelectProjectPanel(ui);
		return panel;
	
    }

    private Panel initCreateProjectPanel(){
		return new CreateProjectPanel(ui);
    }
    
    private Panel initUploadPanel() {
		return new UploadProjectPanel(ui);
    }
}
