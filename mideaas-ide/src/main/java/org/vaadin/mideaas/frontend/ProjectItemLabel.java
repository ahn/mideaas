package org.vaadin.mideaas.frontend;

import java.util.ArrayList;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.vaadin.mideaas.editor.DocDifference;
import org.vaadin.mideaas.editor.MultiUserDoc.DifferingChangedListener;
import org.vaadin.mideaas.model.ProjectItem;

import com.vaadin.ui.Label;

@SuppressWarnings("serial")
public class ProjectItemLabel extends Label implements DifferingChangedListener {
	
	private final ProjectItem item;
	
	public ProjectItemLabel(ProjectItem item) {
		super(item.getName());
		this.item = item;
	}
	
	@Override
	public void attach() {
		super.attach();
		
		item.addDifferingChangedListener(this);
	}
	
	@Override
	public void detach() {
		super.detach();
		
		item.removeDifferingChangedListener(this);
	}

	@Override
	public void differencesChanged(final Set<DocDifference> diffs) {
		getUI().access(new Runnable() {
			@Override
			public void run() {
				String caption = item.getName();
				if (!diffs.isEmpty()) {
					ArrayList<String> names = new ArrayList<String>(diffs.size());
					for (DocDifference dd : diffs) {
						names.add(dd.getUser().getName());
					}
					caption += " (" + StringUtils.join(names, ", ") + ")";
				}
				setValue(caption);
			}
		});
	}
}
