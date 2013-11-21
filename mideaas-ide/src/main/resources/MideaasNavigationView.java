package org.vaadin.mideaas;

import org.vaadin.teemu.clara.Clara;

import com.vaadin.addon.touchkit.ui.NavigationView;

@SuppressWarnings("serial")
public abstract class MideaasNavigationView extends NavigationView {
	private boolean loaded = false;
    @Override
    public void onBecomingVisible() {
        super.onBecomingVisible();
        if (!loaded) {
        	setCaption(this.getClass().getSimpleName()); // ?
	        String cls = this.getClass().getName();
	        String path = cls.replace('.', '/');
	        String model = path+".clara.xml";
	        setContent(Clara.create(getClass().getClassLoader().getResourceAsStream(model), this));
	        loaded = true;
        }
    }
}
