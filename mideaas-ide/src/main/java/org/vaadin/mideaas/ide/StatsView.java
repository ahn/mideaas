package org.vaadin.mideaas.ide;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.vaadin.mideaas.editor.DocDifference;
import org.vaadin.mideaas.editor.EditorUser;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Tree;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class StatsView extends CustomComponent implements View {

	private final ProjectContainer projects;
	
	public StatsView(ProjectContainer projects) {
		this.projects = projects;
	}

	@Override
	public void enter(ViewChangeEvent event) {
		VerticalLayout layout = new VerticalLayout();
		Tree tree = new Tree();
		layout.setMargin(true);
		layout.setSpacing(true);
		Set<String> ids = projects.getProjectIds();
		for (String id : ids) {
			IdeProject project = projects.getProject(id);
			if (project == null) {
				continue;
			}
			Object proot = tree.addItem();
			tree.setItemCaption(proot, project.getName());
			
			Object pid = tree.addItem();
			tree.setItemCaption(pid, "id: " + project.getId());
			tree.setChildrenAllowed(pid, false);
			tree.setParent(pid, proot);
			
			addUsers(tree, project, proot);
			addDocs(tree, project, proot);
		}
		Panel pa = new Panel(ids.size() + " projects", tree);
		pa.setSizeFull();
		setCompositionRoot(pa);
	}

	private void addDocs(Tree tree, IdeProject project, Object proot) {
		Object it = tree.addItem();
		tree.setParent(it, proot);
		Collection<String> ids = project.getDocIds();
		
		int n = 0;
		for (String id : ids) {
			IdeDoc doc = project.getDoc(id);
			if (doc == null) {
				continue;
			}
			Map<EditorUser, DocDifference> diffs = doc.getDoc().getDifferences();
			if (!diffs.isEmpty()) { 
				n += 1;
				Object did = tree.addItem();
				
				tree.setParent(did, it);
				
				for (Entry<EditorUser, DocDifference> e: diffs.entrySet()) {
					addUser(tree, did, e.getKey());
				}
				
				tree.setItemCaption(did, id + " (" + diffs.size() + ")");
			}
		}
		tree.setItemCaption(it, n + "/" + ids.size() + " docs open");
	}

	private void addUser(Tree tree, Object parent, EditorUser user) {
		Object iid = tree.addItem();
		tree.setParent(iid, parent);
		tree.setItemCaption(iid, user.getName());
		Object uid = tree.addItem();
		tree.setItemCaption(uid, "id: " + user.getId());
		tree.setParent(uid, iid);
		tree.setChildrenAllowed(uid, false);
		Object uname = tree.addItem();
		tree.setItemCaption(uname, "name: " + user.getName());
		tree.setParent(uname, iid);
		tree.setChildrenAllowed(uname, false);
		Object uemail = tree.addItem();
		tree.setItemCaption(uemail, "email: " + user.getEmail());
		tree.setParent(uemail, iid);
		tree.setChildrenAllowed(uemail, false);
	}

	private void addUsers(Tree tree, IdeProject project, Object proot) {
		List<EditorUser> users = project.getTeam().getUsers();
		Object it = tree.addItem();
		tree.setItemCaption(it, users.size() + " users");
		tree.setParent(it, proot);
		for (EditorUser user : users) {
			addUser(tree, it, user);
		}
	}

}
