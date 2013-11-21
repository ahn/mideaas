package org.vaadin.mideaas.frontend;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Scanner;

import com.vaadin.server.BrowserWindowOpener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class VisualDesignerConnector extends CustomComponent {

	private final String visualDesignerUrl;
	private final String modelId;
	private final ClaraEditor claraEd;
	private final VerticalLayout layout = new VerticalLayout();
	
	public VisualDesignerConnector(String modelId, String visualDesignerUrl, ClaraEditor claraEd) {
		this.modelId = modelId;
		this.visualDesignerUrl = visualDesignerUrl;
		this.claraEd = claraEd;
		setCompositionRoot(layout);
	}

	@Override
	public void attach() {
		super.attach();
		URI myUrl = UI.getCurrent().getPage().getLocation();
		String myUrlRoot = myUrl.getScheme() + "://" + myUrl.getAuthority()
				+ myUrl.getPath();

		String modelUrl = myUrlRoot + "?getmodelid=" + modelId;

		String modelUrlEnc;
		try {
			modelUrlEnc = URLEncoder.encode(modelUrl, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}

		String url = visualDesignerUrl + "?restartApplication&modelid="
				+ modelId + "&modeluri=" + modelUrlEnc;

		BrowserWindowOpener opener = new BrowserWindowOpener(url);

		Button button = new Button("Open Visual Designer");
		button.setIcon(Icons.APPLICATION_ARROW);
		opener.extend(button);

		layout.addComponent(button);
		
		Button fetchButton = new Button("Get from Visual Designer");
		fetchButton.setIcon(Icons.ARROW_180_MEDIUM);
		fetchButton.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				fetch(modelId);
			}
		});
		
		layout.addComponent(fetchButton);

	}

	private void fetch(String modelId) {
		try {
			String xml = getModelFromUrl(visualDesignerUrl
					+ "?getmodel&modelid=" + modelId);
			claraEd.setXml(xml);
		} catch (IOException e) {
			Notification.show("Error: " + e.getMessage(),
					Notification.Type.ERROR_MESSAGE);
		}
	}

	private String getModelFromUrl(String uri) throws IOException {
		URL url = new URL(uri);
		URLConnection conn = url.openConnection();
		return convertStreamToString(conn.getInputStream());
	}

	private static String convertStreamToString(InputStream is) {
		Scanner scanner = new Scanner(is);
		scanner.useDelimiter("\\A");
		String s = scanner.hasNext() ? scanner.next() : "";
		scanner.close();
		return s;
	}
}
