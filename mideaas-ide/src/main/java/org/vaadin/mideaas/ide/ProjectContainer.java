package org.vaadin.mideaas.ide;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;

public class ProjectContainer {
	
	private static final long MAINTENANCE_INTERVAL_MS = 60 * 60 * 1000;
	private static final long DESTROY_PROJECTS_OLDER_THAN_MS = 24 * 60 * 60 * 1000;

	private static class Proj {
		IdeProject project;
		Date lastTouched;
		Proj(IdeProject project) {
			this.project = project;
			lastTouched = new Date();
		}
		void touch() {
			lastTouched = new Date();
		}
		boolean hasBeenTouchedAfter(Date d) {
			return !lastTouched.before(d);
		}
	}
	
	private HashMap<String, Proj> projects = new HashMap<String, Proj>();
	
	private class MaintenanceTask extends TimerTask {
		@Override
		public void run() {
			destroyOldProjects();
			scheduleMaintenanceTask();
		}
	};
	
	public ProjectContainer() {
		scheduleMaintenanceTask();
	}
	
	private void scheduleMaintenanceTask() {
		Timer timer = new Timer();
		timer.schedule(new MaintenanceTask(), MAINTENANCE_INTERVAL_MS);
	}
	
	public synchronized IdeProject getProject(String id) {
		Proj p = projects.get(id);
		if (p != null) {
			p.touch();
			return p.project;
		}
		return null;
	}
	
	public synchronized IdeProject putProject(String name, IdeProject project) {
		Proj prev = projects.put(name, new Proj(project));
		return prev == null ? null : prev.project;
	}
	
	public synchronized IdeProject removeProject(String name) {
		Proj removed = projects.remove(name);
		return removed == null ? null : removed.project;
	}
	
	public synchronized void destroyOldProjects() {
		ArrayList<String> remove = new ArrayList<String>();
		long now = (new Date()).getTime();
		Date whileAgo = new Date(now - DESTROY_PROJECTS_OLDER_THAN_MS);
		for (Entry<String, Proj> e : projects.entrySet()) {
			if (!e.getValue().hasBeenTouchedAfter(whileAgo)) {
				e.getValue().project.destroy();
				remove.add(e.getKey());
			}
		}
		for (String r : remove) {
			projects.remove(r);
		}
	}
	
}
