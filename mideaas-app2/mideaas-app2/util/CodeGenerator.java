package org.vaadin.mideaas.app.java.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.lang.model.SourceVersion;

public class CodeGenerator {

	private static final Pattern lowerUpper = Pattern
			.compile("\\p{Lower}\\p{Upper}");

	public static String getAnynomousSkeletonOf(Class<?> cls, String indent) {
		StringBuilder sb = new StringBuilder();

		sb.append("new ").append(cls.getCanonicalName()).append("() {\n");

		for (Method me : cls.getMethods()) {
			if (Modifier.isAbstract(me.getModifiers())) {
				sb.append(indent).append("    @Override\n");
				sb.append(indent).append("    ").append(methodDeclaration(me))
						.append(" {\n");
				sb.append(indent).append("        // TODO: Implement ")
						.append(me.getName()).append("\n");
				sb.append(indent).append("    }\n");
			}
		}
		return sb.append(indent).append("}").toString();
	}

	public static String getNewOf(Class<?> cls, String indent) {
		StringBuilder sb = new StringBuilder();

		if (cls.getConstructors().length == 0) {
			sb.append("new ").append(cls.getCanonicalName()).append("()");
		} else {
			sb.append("new ").append(
					constructorDeclaration(cls.getConstructors()[0]));
		}
		return sb.toString();
	}

	private static String methodDeclaration(Method m) {
		final String mods = Modifier.toString(m.getModifiers()
				- Modifier.ABSTRACT);
		final String retu = m.getReturnType().getName();
		final String name = m.getName();
		final String params = paramString(m.getParameterTypes(), true);
		return new StringBuilder().append(mods).append(" ").append(retu)
				.append(" ").append(name).append(params).toString();
	}

	private static String constructorDeclaration(Constructor<?> c) {
		final String name = c.getName();
		final String params = paramString(c.getParameterTypes(), false);
		return new StringBuilder().append(name).append(params).toString();
	}

	private static String paramString(Class<?>[] paramTypes,
			boolean withVarNames) {
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		for (int i = 0; i < paramTypes.length; ++i) {
			if (i != 0) {
				sb.append(", ");
			}
			Class<?> type = paramTypes[i];
			sb.append(type.getCanonicalName());
			if (withVarNames) {
				sb.append(" ").append(makeUpVarNameFor(type));
			}
		}
		return sb.append(")").toString();
	}

	/**
	 * FooBarJee => jee, FooBAR => bar, MyClazz => clazz, MyClass => c
	 */
	private static String makeUpVarNameFor(Class<?> type) {
		String simple = type.getSimpleName();
		Matcher m = lowerUpper.matcher(simple);
		int start = 0;
		while (m.find()) {
			start = m.start() + 1;
		}
		String var = simple.substring(start).toLowerCase();
		if (SourceVersion.isName(var)) {
			return var;
		}
		return var.substring(0, 1);
	}
}
