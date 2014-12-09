package org.vaadin.mideaas.ide;


abstract class DocAdderImpl implements DocAdder {

	private final IdeProject project;

	public DocAdderImpl(IdeProject project) {
		this.project = project;
	}
	
	@Override
	public boolean addDoc(String filename, String content) {
		if (project.getDoc(filename) != null) {
			return false;
		}
		// Some one else could have just created a doc with same id,
		// but that's too bad for that some one else.
		project.putDoc(filename, content);
		return true;
	}

}
