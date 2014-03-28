package org.vaadin.mideaas.editor;

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
