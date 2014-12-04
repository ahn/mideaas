package org.vaadin.mideaas.ide;


public interface IdeConfiguration {
	ProjectCustomizer getProjectCustomizer();
	IdeCustomizer getIdeCustomizer();
	IdeLoginView createLoginView();
	IdeLobbyView createLobbyView();
}
