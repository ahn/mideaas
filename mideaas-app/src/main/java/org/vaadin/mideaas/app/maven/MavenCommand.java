package org.vaadin.mideaas.app.maven;

import java.util.Arrays;
import java.util.List;

import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;

@SuppressWarnings("serial")
public class MavenCommand implements Command {

	private final Builder builder;
	private final List<String> goals;
	
	public MavenCommand(Builder builder, String[] goals) {
		this.builder = builder;
		this.goals = Arrays.asList(goals);
	}
	
	@Override
	public void menuSelected(MenuItem selectedItem) {
		builder.build(goals);
	}

}
