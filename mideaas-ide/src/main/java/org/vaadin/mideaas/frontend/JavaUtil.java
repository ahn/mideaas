package org.vaadin.mideaas.frontend;

import japa.parser.ASTHelper;
import japa.parser.JavaParser;
import japa.parser.ParseException;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.ImportDeclaration;
import japa.parser.ast.body.BodyDeclaration;
import japa.parser.ast.body.FieldDeclaration;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.body.ModifierSet;
import japa.parser.ast.body.Parameter;
import japa.parser.ast.body.TypeDeclaration;
import japa.parser.ast.expr.AnnotationExpr;
import japa.parser.ast.expr.AssignExpr;
import japa.parser.ast.expr.AssignExpr.Operator;
import japa.parser.ast.expr.CastExpr;
import japa.parser.ast.expr.FieldAccessExpr;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.expr.ThisExpr;
import japa.parser.ast.stmt.BlockStmt;
import japa.parser.ast.stmt.ExpressionStmt;
import japa.parser.ast.stmt.Statement;
import japa.parser.ast.type.ClassOrInterfaceType;
import japa.parser.ast.type.ReferenceType;
import japa.parser.ast.type.Type;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.vaadin.annotations.AutoGenerated;

public class JavaUtil {

    public static String javaCodeWithRootClass(String java, String rootClass) {

        try {
            CompilationUnit cu = getCu(java);
            changeMethods(cu, rootClass);
            return cu.toString();
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    private static CompilationUnit getCu(String java) throws ParseException {
        InputStream is = new ByteArrayInputStream(java.getBytes());

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

    private static void changeMethods(CompilationUnit cu, String rootClass) {
        addImport(cu, rootClass);
        String rootClassShort = rootClass
                .substring(rootClass.lastIndexOf(".") + 1);

        List<TypeDeclaration> types = cu.getTypes();
        for (TypeDeclaration type : types) {
            List<BodyDeclaration> members = type.getMembers();
            for (BodyDeclaration member : members) {
                if (member instanceof MethodDeclaration) {
                    MethodDeclaration method = (MethodDeclaration) member;
                    if (isSetRootComponentMethod(method)) {
                        changeSetRootMethod(method, rootClassShort);
                    }
                } else if (member instanceof FieldDeclaration) {
                    FieldDeclaration field = (FieldDeclaration) member;
                    if (isRootField(field)) {
                        changeRootField(field, rootClassShort);
                    }
                }
            }
        }
    }

    private static void changeRootField(FieldDeclaration field, String rootClass) {
        Type type = new ClassOrInterfaceType(rootClass);
        field.setType(type);
    }

    private static boolean isRootField(FieldDeclaration field) {
        if (field.getVariables().size() != 1
                || !"root"
                        .equals(field.getVariables().get(0).getId().getName())) {
            return false;
        }
        field.getModifiers();

        return ModifierSet.hasModifier(field.getModifiers(),
                ModifierSet.PRIVATE) && hasAnnotation(field, "AutoGenerated");
    }

    @AutoGenerated
    private static void changeSetRootMethod(MethodDeclaration method,
            String rootClass) {

        Type type = new ClassOrInterfaceType(rootClass);

//        Parameter newArg = ASTHelper.createParameter(type, "root");
//        method.setParameters(Collections.singletonList(newArg));

        BlockStmt body = method.getBody();
        AssignExpr rootAssign = findRootAssignExpr(body);
        
        
        CastExpr ce = new CastExpr(type, new NameExpr("root"));
        FieldAccessExpr field = new FieldAccessExpr(new ThisExpr(), "root");
        
        if (rootAssign==null) {
            rootAssign = new AssignExpr(field, ce, Operator.assign);
            ASTHelper.addStmt(body, rootAssign);
        }
        else {
            rootAssign.setValue(ce);
        }

    }

    private static AssignExpr findRootAssignExpr(BlockStmt body) {
        if (body.getStmts() != null) {
            for (Statement s : body.getStmts()) {
                if (s instanceof ExpressionStmt) {
                    ExpressionStmt est = (ExpressionStmt) s;
                    if (est.getExpression() instanceof AssignExpr) {
                        AssignExpr ass = (AssignExpr) est.getExpression();
                        if (ass.getTarget() instanceof FieldAccessExpr) {
                            FieldAccessExpr fae = (FieldAccessExpr) ass.getTarget();
                            if ("root".equals(fae.getField())) {
                                return ass;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private static boolean isSetRootComponentMethod(MethodDeclaration method) {
        return "setRootComponent".equals(method.getName())
                && method.getParameters()!=null 
                && method.getParameters().size() == 1
                && isSetRootComponentMethodParameter(method.getParameters().get(0))
                && hasAnnotation(method, "AutoGenerated");
    }

    private static boolean isSetRootComponentMethodParameter(Parameter p) {
        return p.getType() instanceof ReferenceType; // TODO
    }

    private static boolean hasAnnotation(BodyDeclaration decl, String anno) {
        if (decl.getAnnotations() == null) {
            return false;
        }
        for (AnnotationExpr ae : decl.getAnnotations()) {
            if (anno.equals(ae.getName().toString())) {
                return true;
            }
        }
        return false;
    }

    private static void addImport(CompilationUnit cu, String imp) {
        for (ImportDeclaration id : cu.getImports()) {
            if (imp.equals(id.getName().toString())) {
                return;
            }
        }

        cu.getImports().add(
                new ImportDeclaration(new NameExpr(imp), false, false));
    }
    
    public static final Set<String> JAVA_RESERVED_WORDS = new HashSet<String>(
            Arrays.asList(new String[] { "abstract", "assert", "boolean",
                    "break", "byte", "case", "catch", "char", "class", "const",
                    "continue", "default", "do", "double", "else", "enum",
                    "extends", "false", "final", "finally", "float", "for",
                    "goto", "if", "implements", "import", "instanceof", "int",
                    "interface", "long", "native", "new", "null", "package",
                    "private", "protected", "public", "return", "short",
                    "static", "strictfp", "super", "switch", "synchronized",
                    "this", "throw", "throws", "transient", "true", "try",
                    "void", "volatile", "while", }));
    
    public static boolean isJavaReservedWord(String s) {
        return JAVA_RESERVED_WORDS.contains(s);
    }

	public static List<String> classPathItems(String classPath) {
		return Arrays.asList(classPath.split(File.pathSeparator));
	}

	public static String generateClass(String fullClass, String baseCls) {
		String[] cls = splitFullJavaClassName(fullClass);
		String[] base = baseCls==null ? null : splitFullJavaClassName(baseCls);
		StringBuilder sb = new StringBuilder("package "+cls[0]+";\n\n");
		if (base!=null) {
			sb.append("import "+baseCls+";\n\n");
		}
		sb.append("public class "+cls[1]+" ");
		if (base!=null) {
			sb.append("extends " + base[1]+ " ");
		}
		sb.append("{\n\n\n\n}\n");
		return sb.toString();
	}

	public static String[] splitFullJavaClassName(String cls) {
		int i = cls.lastIndexOf('.');
		return new String[] { cls.substring(0, i), cls.substring(i+1)};
	}
}