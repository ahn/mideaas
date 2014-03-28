package org.vaadin.mideaas.frontend;

import java.util.ArrayList;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Table;

@SuppressWarnings("serial")
public class CFAppsView extends CustomComponent{

	private Table table = new Table();
	private LogView logView;
	private final Deployer deployer;

	public CFAppsView(Deployer deployer, LogView logView) {
		super();
		this.setCompositionRoot(table);
		this.logView=logView;
		this.deployer = deployer;
		
		table.setSizeFull();
        table.setSelectable(true);
        table.setMultiSelect(true);
        table.setImmediate(true);

        table.setColumnReorderingAllowed(true);
        table.setColumnCollapsingAllowed(true);

        table.addContainerProperty("AppId", Integer.class, null);
        table.addContainerProperty("Name", String.class, null);
        table.addContainerProperty("Description", String.class, null);
        table.addContainerProperty("Url", String.class, null);
        table.addContainerProperty("", Button.class, null);
        updateView();
    }
	
	public void updateView(){
		table.removeAllItems();
		ClientResponse response = Deployer.findApplications();
		String responseString = response.getEntity(new GenericType<String>(){});
        logView.newLine(responseString);

        ArrayList<Object[]> items = deployer.createRows(responseString,this, logView);
        for (Object[] item:items){
        	try{
        		table.addItem(item,item[0]);
        	}catch(Exception e){
        		e.printStackTrace();
        	}
        }
        table.setPageLength(10);
     // Adjust the table height a bit
        table.setPageLength(table.size());		
	}
}
