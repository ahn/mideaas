package org.vaadin.mideaas.editortest;

import javax.servlet.annotation.WebServlet;

import name.fraser.neil.plaintext.diff_match_patch.Diff;
import name.fraser.neil.plaintext.diff_match_patch.Operation;
import name.fraser.neil.plaintext.diff_match_patch.Patch;

import org.codehaus.plexus.util.StringUtils;
import org.vaadin.aceeditor.ServerSideDocDiff;
import org.vaadin.aceeditor.client.AceDoc;
import org.vaadin.mideaas.editor.CollaborativeAceEditor;
import org.vaadin.mideaas.editor.DocDiffMediator;
import org.vaadin.mideaas.editor.DocDiffMediator.Guard;
import org.vaadin.mideaas.editor.EditorUser;
import org.vaadin.mideaas.editor.SharedDoc;

import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
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
	
	private static SharedDoc upstream = new SharedDoc(new AceDoc("joo"));
	private static SharedDoc downstream1 = new SharedDoc(new AceDoc("joo"));
	private static SharedDoc downstream2 = new SharedDoc(new AceDoc("joo"));
	private static DocDiffMediator hub1 = new DocDiffMediator(upstream, downstream1);
	private static DocDiffMediator hub2 = new DocDiffMediator(upstream, downstream2);
	
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
		hub1.setUpwardsGuard(moiGuard);
		hub2.setUpwardsGuard(moiGuard);
		
		//hub1.setUpwardsGuard(new KGuard("a",2));
		//hub2.setUpwardsGuard(new KGuard("a",3));
	}

	private final CollaborativeAceEditor editor1 = new CollaborativeAceEditor(downstream1);
	private final CollaborativeAceEditor editor2 = new CollaborativeAceEditor(downstream2);
	
	
	
	@Override
	protected void init(VaadinRequest request) {
		
		final VerticalLayout layout = new VerticalLayout();
        layout.setMargin(true);
        layout.setSizeFull();
        setContent(layout);
        
        editor1.setSizeFull();
        layout.addComponent(editor1);
        
        editor2.setSizeFull();
        layout.addComponent(editor2);
        
        CollaborativeAceEditor x = new CollaborativeAceEditor(upstream);
        x.setSizeFull();
        layout.addComponent(x);
        
	}
		
}
