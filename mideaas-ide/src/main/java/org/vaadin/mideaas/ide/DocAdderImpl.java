package org.vaadin.mideaas.ide;


abstract class DocAdderImpl implements DocAdder {

	private final IdeProject project;
	private final IdeConfiguration config;

	public DocAdderImpl(IdeProject project, IdeConfiguration config) {
		this.project = project;
		this.config = config;
	}
	
	@Override
	public boolean addDoc(String filename, String content) {
		if (project.getDoc(filename) != null) {
			return false;
		}
		IdeDoc doc = IdeUtil.createDoc(filename, content, config, project);
		// Some one else could have just created a doc with same id,
		// but that's too bad for that some one else.
		project.putDoc(filename, doc);
		return true;
	}

}
