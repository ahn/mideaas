package org.vaadin.mideaas.editor;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MyErrorChecker implements AsyncErrorChecker {
	
	private static Pattern pattern = Pattern.compile("[Xx]+");
	
	@Override
	public void checkErrors(final String s, final ResultListener listener) {
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				System.out.println("CHECKING ERRORS!");
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return;
				}
				listener.errorsChecked(check(s));
			}
		}).start();
	}
	
	
	private static ArrayList<ErrorChecker.Error> check(String s) {
		Matcher matcher = pattern.matcher(s);
		ArrayList<ErrorChecker.Error> errors = new ArrayList<ErrorChecker.Error>();
		int i = 0;
		while (i < s.length() && matcher.find(i)) {
			i = matcher.end() + 1;
			errors.add(new ErrorChecker.Error("X's not allowed here! ("+matcher.group()+")", matcher.start(), matcher.end()));
		}
		return errors;
	}
}
