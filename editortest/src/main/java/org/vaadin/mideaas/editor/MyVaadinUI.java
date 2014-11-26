package org.vaadin.mideaas.editor;

import javax.servlet.annotation.WebServlet;

import org.codehaus.plexus.util.StringUtils;
import org.vaadin.aceeditor.ServerSideDocDiff;
import org.vaadin.aceeditor.client.AceDoc;
import org.vaadin.mideaas.editor.DocDiffMediator.Guard;

import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;


@Theme("mytheme")
@SuppressWarnings("serial")
@Push
public class MyVaadinUI extends UI
{
	private static long lastUserId = 0;
	synchronized private static EditorUser createUser() {
		long uid = ++lastUserId;
		return new EditorUser(""+uid,"User "+uid);
	}

    @WebServlet(value = "/*", asyncSupported = true)
    @VaadinServletConfiguration(productionMode = false, ui = MyVaadinUI.class, widgetset = "org.vaadin.mideaas.editortest.AppWidgetSet")
    public static class Servlet extends VaadinServlet {
    }

	private static int foobar = 0;
	private static synchronized int iii() {
		return ++foobar;
	}
	
	private static int debugId = 0;//iii();
	
	private static final AceDoc initialDoc = new AceDoc("\njoo\n\n\n\n\n\n");
	
	/*
	private static SharedDoc upstream = new SharedDoc(initialDoc);
	private static SharedDoc downstream1 = new SharedDoc(initialDoc);
	private static SharedDoc downstream2 = new SharedDoc(initialDoc);
	private static DocDiffMediator hub1 = new DocDiffMediator(upstream, downstream1);
	private static DocDiffMediator hub2 = new DocDiffMediator(upstream, downstream2);
	*/
	
	private static final Guard moiGuard = new Guard() {
		@Override
		public boolean isAcceptable(AceDoc candidate, ServerSideDocDiff diff) {
//			for (Patch p : diff.getPatches()) {
//				for (Diff d : p.diffs) {
//					if (d.operation==Operation.INSERT) {
//						System.out.println(d.text);
//					}
//				}
//			}
			
			String t = candidate.getText();

			return StringUtils.countMatches(t, "(") == StringUtils.countMatches(t, ")");
			
			
			//return t.toLowerCase().equals(t);
		}

	};
	

	
	private static class KGuard implements Guard {
		private final String X;
		private final int K;
		KGuard(String X, int K) {
			this.X = X;
			this.K = K;
		}
		@Override
		public boolean isAcceptable(AceDoc candidate, ServerSideDocDiff diff) {
			String t = candidate.getText();
			int i = -1, n = 0;
			while (true) {
				i = t.indexOf(X, i+1);
				if (i==-1) {
					break;
				}
				n++;
			}
			return n % K == 0;
		}
	}
	
	static {
		//hub1.setUpwardsGuard(moiGuard);
		//hub2.setUpwardsGuard(moiGuard);
		
		//hub1.setUpwardsGuard(new KGuard("a",2));
		//hub2.setUpwardsGuard(new KGuard("a",3));
	}

	//private final CollaborativeAceEditor editor1 = new CollaborativeAceEditor(downstream1, 1);
	//private final CollaborativeAceEditor editor2 = new CollaborativeAceEditor(downstream2, 2);
	
	
	private static final MultiUserDoc mud = new MultiUserDoc(initialDoc, null, moiGuard, null, null);
	

	@Override
	protected void init(VaadinRequest request) {
		
		
		String fragment = getPage().getUriFragment();
		String loc = getPage().getLocation().getPath().substring(1);
		
		String uid = loc.isEmpty() ? "Default" : loc;
		final EditorUser eu = new EditorUser(uid, uid);
		
		System.out.println("uid");
		//final EditorUser eu = createUser();
		
		mud.createChildDoc(eu);

		final MultiUserEditor mue = new MultiUserEditor(eu, mud);
		
		
		mue.setSizeFull();
		
		final VerticalLayout layout = new VerticalLayout();
		
        layout.setMargin(false);
        layout.setSizeFull();
        setContent(layout);
        
        
        layout.addComponent(mue);
        
        
        Button leave = new Button("Leave");
        
        leave.addClickListener(new ClickListener() {
			
			@Override
			public void buttonClick(ClickEvent event) {
				layout.removeAllComponents();
				mud.removeChildDoc(eu);
			}
		});
        
        layout.addComponent(leave);
        
        /*
        
        final VerticalLayout la1 = new VerticalLayout();
        la1.addStyleName("collabeditor-layout-" + editor1.getStyleIndex());
        layout.addComponent(la1);
        editor1.setSizeFull();
        la1.addComponent(editor1);
        la1.setSizeFull();
        la1.setMargin(true);
        
        final VerticalLayout la2 = new VerticalLayout();
        la2.addStyleName("collabeditor-layout-" + editor2.getStyleIndex());
        layout.addComponent(la2);
        editor2.setSizeFull();
        la2.addComponent(editor2);
        la2.setSizeFull();
        la2.setMargin(true);
        
        CollaborativeAceEditor editor3 = new CollaborativeAceEditor(upstream, 3);
        
        final VerticalLayout la3 = new VerticalLayout();
        la3.addStyleName("collabeditor-layout-" + editor3.getStyleIndex());
        layout.addComponent(la3);
        editor3.setSizeFull();
        la3.addComponent(editor3);
        la3.setSizeFull();
        la3.setMargin(true);
        
        */
        
	}
		
}
