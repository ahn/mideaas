package org.vaadin.mideaas.ide.model;

import japa.parser.JavaParser;
import japa.parser.ParseException;
import japa.parser.ast.Comment;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.ImportDeclaration;
import japa.parser.ast.LineComment;
import japa.parser.ast.Node;
import japa.parser.ast.PackageDeclaration;
import japa.parser.ast.body.BodyDeclaration;
import japa.parser.ast.body.ClassOrInterfaceDeclaration;
import japa.parser.ast.body.FieldDeclaration;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.body.ModifierSet;
import japa.parser.ast.body.Parameter;
import japa.parser.ast.body.TypeDeclaration;
import japa.parser.ast.body.VariableDeclarator;
import japa.parser.ast.body.VariableDeclaratorId;
import japa.parser.ast.expr.AnnotationExpr;
import japa.parser.ast.expr.Expression;
import japa.parser.ast.expr.MarkerAnnotationExpr;
import japa.parser.ast.expr.MethodCallExpr;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.expr.ObjectCreationExpr;
import japa.parser.ast.expr.SingleMemberAnnotationExpr;
import japa.parser.ast.expr.StringLiteralExpr;
import japa.parser.ast.expr.SuperExpr;
import japa.parser.ast.stmt.BlockStmt;
import japa.parser.ast.stmt.ExpressionStmt;
import japa.parser.ast.stmt.ReturnStmt;
import japa.parser.ast.stmt.Statement;
import japa.parser.ast.type.ClassOrInterfaceType;
import japa.parser.ast.type.ReferenceType;
import japa.parser.ast.type.Type;
import japa.parser.ast.type.VoidType;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.vaadin.aceeditor.ServerSideDocDiff;
import org.vaadin.mideaas.editor.DocDiffMediator;


/**
 * 
 * 
 *
 */
public class ControllerCode {

	// Latest valid code
	private CompilationUnit cu;
	
	public static ControllerCode createInitial(String projPackage, String className) {
		CompilationUnit cu = createInitialCu(projPackage);
		ControllerCode cc = new ControllerCode(cu);
		cc.createClass(className);
		// XXX?
		cc.addAttachSkeleton();
		//cc.addOnBecomingVisibleMethod();
		return cc;
	}
	
	public ControllerCode(CompilationUnit cu) {
		this.cu = cu;
	}

	public ControllerCode(String code) throws ParseException {
		this(getCu(code));
	}
	
	public String getCode() {
		return cu.toString();
	}
	
	private static CompilationUnit getCu(InputStream is) throws ParseException {
		try {
			return JavaParser.parse(is);
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private static CompilationUnit createInitialCu(String projPackage) {
		CompilationUnit newCu = new CompilationUnit();
		newCu.setPackage(new PackageDeclaration(new NameExpr(projPackage)));
		return newCu;
	}
	
	private void createClass(String name) {
		ensureImport("org.vaadin.mideaas.MideaasComponent");
		ClassOrInterfaceDeclaration decl =
				new ClassOrInterfaceDeclaration(ModifierSet.PUBLIC, false, name);
		decl.setExtends(Collections.singletonList(new ClassOrInterfaceType("MideaasComponent")));
		cu.setTypes(Collections.singletonList((TypeDeclaration)decl));
	}
	
	@SuppressWarnings("unused")
	private void createClassTouchkit(String name) {
		ensureImport("org.vaadin.mideaas.MideaasNavigationView");
		ClassOrInterfaceDeclaration decl =
				new ClassOrInterfaceDeclaration(ModifierSet.PUBLIC, false, name);
		decl.setExtends(Collections.singletonList(new ClassOrInterfaceType("MideaasNavigationView")));
		cu.setTypes(Collections.singletonList((TypeDeclaration)decl));
	}
	
	private void addAttachSkeleton() {
		List<Parameter> params = Collections.emptyList();
		MethodDeclaration method = new MethodDeclaration(
				ModifierSet.PUBLIC, new VoidType(), "attach", params);
		AnnotationExpr override = new MarkerAnnotationExpr(new NameExpr("Override"));
		method.setAnnotations(Collections.singletonList(override));
		
		BlockStmt block = new BlockStmt();
		Expression e = new MethodCallExpr(new SuperExpr(), "attach");
		List<Statement> sts = Collections.singletonList((Statement)new ExpressionStmt(e));
		block.setStmts(sts);
		method.setBody(block);
		
		if (getType().getMembers()==null) {
			getType().setMembers(new LinkedList<BodyDeclaration>());
		}
		getType().getMembers().add(method);
	}
	
	@SuppressWarnings("unused")
	private void addOnBecomingVisibleMethod() {
		List<Parameter> params = Collections.emptyList();
		MethodDeclaration method = new MethodDeclaration(
				ModifierSet.PUBLIC, new VoidType(), "onBecomingVisible", params);
		AnnotationExpr override = new MarkerAnnotationExpr(new NameExpr("Override"));
		method.setAnnotations(Collections.singletonList(override));
		
		BlockStmt block = new BlockStmt();
		Expression e = new MethodCallExpr(new SuperExpr(), "onBecomingVisible");
		List<Statement> sts = Collections.singletonList((Statement)new ExpressionStmt(e));
		block.setStmts(sts);
		method.setBody(block);
		
		if (getType().getMembers()==null) {
			getType().setMembers(new LinkedList<BodyDeclaration>());
		}
		getType().getMembers().add(method);
	}
	
	private void addNotification(MethodDeclaration method, String text) {
		MethodCallExpr e = new MethodCallExpr(new NameExpr("Notification"), "show");
		e.setArgs(Collections.singletonList((Expression)new StringLiteralExpr(text)));
		Statement statement = new ExpressionStmt(e);
		addToMethod(method, statement);
		ensureImport("com.vaadin.ui.Notification");
	}
	
	private void addToMethod(MethodDeclaration method, Statement statement) {
		BlockStmt body = method.getBody();
		if (body==null) {
			body = new BlockStmt();
			method.setBody(body);
		}
		if (body.getStmts()==null) {
			body.setStmts(new ArrayList<Statement>());
		}
		body.getStmts().add(statement);
	}
	
	private static void setComment(Node node, String text) {
		LineComment c = new LineComment(text);
		node.setComment(c);
	}



	private static CompilationUnit getCu(String code) throws ParseException {
		return getCu(new ByteArrayInputStream(code.getBytes()));
	}
	
	// This is needed to update line and col positions??
	// TODO: is there a better way?
	private void reparse() {
		try {
			cu = getCu(cu.toString());
		} catch (ParseException e) {
			// shouldn't fail
			System.err.println("WARNING: error while parsing valid java code?!?");
		}
	}

	private void addImport(String imp) {
		if (cu.getImports()==null) {
			cu.setImports(new LinkedList<ImportDeclaration>());
		}
		ImportDeclaration impDeclr = new ImportDeclaration(new NameExpr(imp), false, false);
		cu.getImports().add(impDeclr);
	}
	
	private FieldDeclaration getField(String name) {
		List<BodyDeclaration> members = getType().getMembers();
		if (members==null) {
			return null;
		}
		for (BodyDeclaration f : members) {
			if (f instanceof FieldDeclaration) {
				for (VariableDeclarator v : ((FieldDeclaration)f).getVariables()) {
					if (name.equals(v.getId().getName())) {
						return (FieldDeclaration) f;
					}
				}
			}
		}
		return null;
	}
	
	private MethodDeclaration getMethod(String name) {
		List<BodyDeclaration> members = getType().getMembers();
		if (members==null) {
			return null;
		}
		for (BodyDeclaration m : members) {
			if (m instanceof MethodDeclaration) {
				if (name.equals(((MethodDeclaration)m).getName())) {
					return (MethodDeclaration) m;
				}
			}
		}
		return null;
	}
	
	private boolean fieldExists(String name) {
		return getField(name) != null;
	}
	
	private boolean methodExists(String name) {
		return getMethod(name) != null;
	}

	public int[] ensureClaraHandlerExists(String id, String className, String comment) {

		List<BodyDeclaration> members = getType().getMembers();
		if (members != null) {
			for (BodyDeclaration member : members) {
				if (member instanceof MethodDeclaration) {
					MethodDeclaration method = (MethodDeclaration) member;
					if (isClaraHandler(method, id, className)) {
						if (comment!=null) {
							addCommentToTheBeginningOfMethod(method, comment);
						}
						return getMemberRowCol(method);
					}
				}
			}
		}
		
		MethodDeclaration method = addClaraHandler(id, className);
		if (comment!=null) {
			addCommentToTheBeginningOfMethod(method, comment);
		}
		
		reparse();
		String name = method.getName();
		int[] hmm = getMethodRowCol(name);
		
		return hmm;
	}
	
	/**
		TODO: different kinds of data sources??
	*/
	public void ensureClaraDataSource(String id, String cls, String comment) {
		MethodDeclaration ds = getClaraDataSourceFor(id);
		if (ds!=null) {
			// TODO comment?
			// TODO: if cls different from ds's?
			return;			
		}
		
		MethodDeclaration method = addClaraDataSourceMethod(id, cls);
		if (comment!=null) {
			addCommentToTheBeginningOfMethod(method, comment);
		}
	}
	
	

	private void addMember(BodyDeclaration body) {
		getType().getMembers().add(body);
	}

	

	private MethodDeclaration getClaraDataSourceFor(String id) {
		List<BodyDeclaration> members = getType().getMembers();
		if (members != null) {
			for (BodyDeclaration member : members) {
				if (member instanceof MethodDeclaration) {
					MethodDeclaration method = (MethodDeclaration) member;
					if (isClaraDataSource(method, id)) {
						return method;
					}
				}
			}
		}
		return null;
	}
	
	private boolean isClaraDataSource(MethodDeclaration method, String id) {
		// TODO Auto-generated method stub
		return false;
	}

	private static boolean addCommentToTheBeginningOfMethod(MethodDeclaration method, String comment) {
		if (method.getBody()==null || method.getBody().getStmts()==null || method.getBody().getStmts().isEmpty()) {
			return false;
		}
		setComment(method.getBody().getStmts().get(0), comment);
		return true;
	}

	@SuppressWarnings("unused")
	private static void setLineComment(BodyDeclaration body, String comment) {
		Comment jdc = body.getComment();
		if (jdc instanceof LineComment) {
			if (comment==null) {
				jdc.setComment(null);
			}
			else {
				jdc.setContent(comment);
			}
		}
		else {
			if (comment!=null) {
				body.setComment(new LineComment(comment));
			}
		}
	}
	
	private MethodDeclaration addClaraDataSourceMethod(String id, String returnType) {
		String handlerName = generateDataSourceNameFor(id);
		
		Type type = createDataSourceReturnType(returnType);

		List<Parameter> params = Collections.emptyList();
		MethodDeclaration source = new MethodDeclaration(
				ModifierSet.PUBLIC, type, handlerName, params);
		
		source.setBody(new BlockStmt());
		source.setAnnotations(createClaraDataSourceAnnotations(id));
		
		setTestMethodBodyFor(source, returnType);
		
		addMember(source);
		
		return source;
	}
	
	

	private void setTestMethodBodyFor(MethodDeclaration source, String returnType) {
		if ("java.lang.String".equals(returnType)) {
			ensureImport("com.vaadin.data.util.ObjectProperty");
			ClassOrInterfaceType cls = new ClassOrInterfaceType("ObjectProperty<String>");
			List<Expression> args = Collections.singletonList((Expression)new StringLiteralExpr("Moi"));
			Expression creation = new ObjectCreationExpr(null, cls, args);
			ReturnStmt ret = new ReturnStmt(creation);
			addToMethod(source, ret);
		}
		else if ("com.vaadin.data.Container".equals(returnType)) {
			// TODO
			ensureImport("com.vaadin.data.util.IndexedContainer");
			ClassOrInterfaceType cls = new ClassOrInterfaceType("IndexedContainer");
			List<Expression> args = Collections.emptyList();
			Expression creation = new ObjectCreationExpr(null, cls, args);
			ReturnStmt ret = new ReturnStmt(creation);
			addToMethod(source, ret);
		}
		// TODO ...
	}

	private Type createDataSourceReturnType(String returnType) {
		// TODO ...
		if ("java.lang.String".equals(returnType)) {
			ensureImport("com.vaadin.data.Property");
			return new ClassOrInterfaceType("Property<String>");
		}
		else if ("com.vaadin.data.Container".equals(returnType)) {
			ensureImport("com.vaadin.data.Container");
			return new ClassOrInterfaceType("Container");
		}
		// TODO ...
		return null;
	}

	private MethodDeclaration addClaraHandler(String id, String className) {
		ensureImport(className);
		String shortName = className.substring(className.lastIndexOf(".")+1);
		String handlerName = generateHandlerNameFor(id, shortName);
		
		Parameter p = new Parameter(new ClassOrInterfaceType(shortName), new VariableDeclaratorId("event"));
		MethodDeclaration handler = new MethodDeclaration(
				ModifierSet.PUBLIC, new VoidType(), handlerName, Collections.singletonList(p));
		handler.setBody(new BlockStmt());
		handler.setAnnotations(createClaraHandlerAnnotations(id));
		
		addNotification(handler, shortName+" on "+id);		
		
		addMember(handler);
		return handler;
	}

	private static boolean isClaraHandler(MethodDeclaration method, String id,
			String className) {
		if (method.getAnnotations()==null) {
			return false;
		}
		for (AnnotationExpr ann : method.getAnnotations()) {
			if ("UiHandler".equals(ann.getName().getName())) {
				if (ann instanceof SingleMemberAnnotationExpr) {
					SingleMemberAnnotationExpr smae = (SingleMemberAnnotationExpr)ann;
					if (smae.getMemberValue() instanceof StringLiteralExpr) {
						String v = ((StringLiteralExpr)smae.getMemberValue()).getValue();
						if (id.equals(v)) {
							return hasOneParamWithType(method, className);
						}
					}
				}
			}
		}
		return false;
	}

	private static boolean hasOneParamWithType(MethodDeclaration method, String className) {
		if (method.getParameters().size()!=1) {
			return false;
		}
		Parameter p = method.getParameters().get(0);
		// TODO: could check class matching more carefully...
		if (p.getType() instanceof ClassOrInterfaceType) {
			ClassOrInterfaceType coit = (ClassOrInterfaceType)p.getType();
			return className.endsWith("."+coit.getName());
		}
		else if (p.getType() instanceof ReferenceType) {
			ReferenceType rt = (ReferenceType)p.getType();
			return className.endsWith("."+rt.toString()); // ??
		}
		return false;
	}

	public int[] ensureClaraFieldExists(String id, String className) {
		
		List<TypeDeclaration> types = cu.getTypes();
		for (TypeDeclaration type : types) {
			List<BodyDeclaration> members = type.getMembers();
			if (members==null) {
				break;
			}
			for (BodyDeclaration member : members) {
				if (member instanceof FieldDeclaration) {
					FieldDeclaration field = (FieldDeclaration) member;
					if (isClaraField(field, id, className)) {
						return getMemberRowCol(field);
					}
				}
			}
		}
		
		FieldDeclaration fd = addClaraField(id, className);
		
		reparse();
		String name = fd.getVariables().get(0).getId().getName();
		int[] hmm = getFieldRowCol(name);
		
		return hmm;
	}
	
	private int[] getMethodRowCol(String name) {
		return getMemberRowCol(getMethod(name));
	}
	
	private int[] getFieldRowCol(String name) {
		return getMemberRowCol(getField(name));
	}
	
	private int[] getMemberRowCol(BodyDeclaration b) {
		return new int[]{b.getBeginLine(), b.getBeginColumn()-1, b.getEndLine(), b.getEndColumn()-1};
	}

	private TypeDeclaration getType() {
		return cu.getTypes().get(0);
	}

	private FieldDeclaration addClaraField(String id, String className) {
		ensureImport(className);
		String fieldName = generateFieldNameLike(id);
		VariableDeclarator var = new VariableDeclarator(
				new VariableDeclaratorId(fieldName));
		Type type = new ClassOrInterfaceType(className.substring(className.lastIndexOf('.') + 1));
		FieldDeclaration field = new FieldDeclaration(ModifierSet.PRIVATE, type, var);
		field.setAnnotations(createClaraFieldAnnotations(id));
		if (getType().getMembers()==null) {
			getType().setMembers(new LinkedList<BodyDeclaration>());
		}
		getType().getMembers().add(getFirstMethodIndex(), field);
		return field;
	}
	
	private List<AnnotationExpr> createClaraFieldAnnotations(String id) {
		LinkedList<AnnotationExpr> anns = new LinkedList<AnnotationExpr>();
		ensureImport("org.vaadin.teemu.clara.binder.annotation.UiField");
//		ensureImport("com.vaadin.annotations.AutoGenerated");
//		anns.add(createAutogeneratedAnnotation());
		anns.add(createSimpleAnnotation("UiField", id));
		return anns;
	}
	
	private List<AnnotationExpr> createClaraHandlerAnnotations(String id) {
		LinkedList<AnnotationExpr> anns = new LinkedList<AnnotationExpr>();
		ensureImport("org.vaadin.teemu.clara.binder.annotation.UiHandler");
//		ensureImport("com.vaadin.annotations.AutoGenerated");
//		anns.add(createAutogeneratedAnnotation());
		anns.add(createSimpleAnnotation("UiHandler", id));
		return anns;
	}
	
	private List<AnnotationExpr> createClaraDataSourceAnnotations(String id) {
		LinkedList<AnnotationExpr> anns = new LinkedList<AnnotationExpr>();
		ensureImport("org.vaadin.teemu.clara.binder.annotation.UiDataSource");
		anns.add(createSimpleAnnotation("UiDataSource", id));
		return anns;
	}

	@SuppressWarnings("unused")
	private AnnotationExpr createAutogeneratedAnnotation() {
		return new MarkerAnnotationExpr(new NameExpr("AutoGenerated"));
	}
	
	private AnnotationExpr createSimpleAnnotation(String cls, String id) {
		return new SingleMemberAnnotationExpr(new NameExpr(cls), new StringLiteralExpr(id));
	}

	private String generateFieldNameLike(String id) {
		String name = id.replaceAll("[^\\w]", "");
		int i = 1;
		while (fieldExists(name)) {
			name = name + (++i);
		}
		return name;
	}
	
	private String generateHandlerNameFor(String id, String shortName) {
		String methodName = id.replaceAll("[^\\w]", "") + "Handle" + shortName;
		int i = 1;
		String name = methodName;
		while (methodExists(name)) {
			name = methodName + (++i);
		}
		return name;
	}
	
	private String generateDataSourceNameFor(String id) {
		String methodName = id.replaceAll("[^\\w]", "") + "DataSource";
		int i = 1;
		String name = methodName;
		while (methodExists(name)) {
			name = methodName + (++i);
		}
		return name;
	}

	public void ensureImport(String className) {
		if (!isImported(className)) {
			addImport(className);
		}
	}

	private boolean isImported(String className) {
		if (cu.getImports()==null) {
			return false;
		}
		for (ImportDeclaration imp : cu.getImports()) {
			if(importEquals(imp, className)) {
				return true;
			}
		}
		return false;
	}
	
	private static boolean importEquals(ImportDeclaration imp, String fullClassName) {
		if (fullClassName.equals(imp.getName().getName())) {
			return true;
		}
		// Don't know why the above isn't enough...
		// Sometimes imp.getName().getName() is just the short name, sometimes full (??)
		// Additional check. Not 100% certain.
		return imp.toString().contains(fullClassName+";");
	}

	private static boolean isClaraField(FieldDeclaration field, String id,
			String className) {
		if (field.getAnnotations()==null) {
			return false;
		}
		for (AnnotationExpr ann : field.getAnnotations()) {
			if ("UiField".equals(ann.getName().getName())) {
				if (ann instanceof SingleMemberAnnotationExpr) {
					SingleMemberAnnotationExpr smae = (SingleMemberAnnotationExpr)ann;
					if (smae.getMemberValue() instanceof StringLiteralExpr) {
						String v = ((StringLiteralExpr)smae.getMemberValue()).getValue();
						if (id.equals(v)) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}
	
	private int getFirstMethodIndex() {
		List<BodyDeclaration> ms = getType().getMembers();
		if (ms==null) {
			return 0; // XXX
		}
		for (int i=0; i<ms.size(); i++) {
			if (ms.get(i) instanceof MethodDeclaration) {
				return i;
			}
		}
		return 0; // XXX
	}

	public interface Modifier {
		public void modify(ControllerCode c);
	}
	
	public static ServerSideDocDiff getDiffAfterModify(String initial, Modifier modifier) throws ParseException {
		ControllerCode c = new ControllerCode(initial);
		String base = c.getCode();
		modifier.modify(c);
		return ServerSideDocDiff.diff(base, c.getCode());
	}
	
}
