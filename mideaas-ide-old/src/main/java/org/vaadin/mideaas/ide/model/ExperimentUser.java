package org.vaadin.mideaas.ide.model;

public class ExperimentUser extends User {

	private final String anonymizerCode;
	
	public static User newUser(String name, String anonymizerCode) {
		return new ExperimentUser(newUserId(), name, anonymizerCode);
	}

	protected ExperimentUser(String userId, String name, String anonymizerCode) {
		super(userId, name);
		this.anonymizerCode = anonymizerCode;
	}

	public String getAnonymizerCode() {
		return anonymizerCode;
	}

}
