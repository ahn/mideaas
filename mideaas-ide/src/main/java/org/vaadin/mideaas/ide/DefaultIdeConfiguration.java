package org.vaadin.mideaas.ide;

import java.io.File;
import java.util.Map;


public class DefaultIdeConfiguration implements IdeConfiguration {
	
	@Override
	public void ideCreated(Ide ide) {
		// Nothing
	}
	
	@Override
	public IdeLoginView createLoginView() {
		return new JustUsernameLoginView();
	}

	@Override
	public IdeLobbyView createLobbyView() {
		return new ExampleProjectLobbyView();
	}
	
	@Override
	public IdeProject createProject(String id, String name, Map<String, String> files, File workDir) {
		return new IdeProject(id, name);
	}

	@Override
	public DocAdderComponent createDocAdderComponent(IdeProject project, String suggestedParent, DocAdder docAdder) {
		return new SimpleDocAdderComponent(docAdder);
	}

	@Override
	public boolean isProjectFile(String filename) {
		return !filename.startsWith(".");
	}

}
