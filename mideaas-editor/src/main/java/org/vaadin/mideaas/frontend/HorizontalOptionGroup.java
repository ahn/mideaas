package org.vaadin.mideaas.frontend;

import com.vaadin.annotations.StyleSheet;
import com.vaadin.ui.OptionGroup;

@SuppressWarnings("serial")
@StyleSheet("horizontaloptiongroup.css")
public class HorizontalOptionGroup extends OptionGroup {
	
	public HorizontalOptionGroup() {
		super();
		addStyleName("horizontal");
	}
}
