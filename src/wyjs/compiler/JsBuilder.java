package wyjs.compiler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import wyjs.ast.JsBase;
import wyjs.ast.JsNode;
import wyjs.ast.expr.JsAccess;
import wyjs.ast.expr.JsAssign;
import wyjs.ast.expr.JsAssign.JsAssignable;
import wyjs.ast.expr.JsBinOp;
import wyjs.ast.expr.JsExpr;
import wyjs.ast.expr.JsInvoke;
import wyjs.ast.expr.JsList;
import wyjs.ast.expr.JsLiteral;
import wyjs.ast.expr.JsObject;
import wyjs.ast.expr.JsUnOp;
import wyjs.ast.expr.JsVariable;
import wyjs.ast.stmt.JsConstant;
import wyjs.ast.stmt.JsFor;
import wyjs.ast.stmt.JsFunctionStmt;
import wyjs.ast.stmt.JsIfElse;
import wyjs.ast.stmt.JsLine;
import wyjs.ast.stmt.JsReturn;
import wyjs.ast.stmt.JsStmt;
import wyjs.ast.stmt.JsWhile;
import wyjs.ast.util.JsHelpers;
import wyjs.lang.Expr;
import wyjs.lang.Expr.BOp;
import wyjs.lang.Expr.BinOp;
import wyjs.lang.Expr.Comprehension;
import wyjs.lang.Expr.Constant;
import wyjs.lang.Expr.DictionaryGen;
import wyjs.lang.Expr.Invoke;
import wyjs.lang.Expr.Access;
import wyjs.lang.Expr.NamedConstant;
import wyjs.lang.Expr.NaryOp;
import wyjs.lang.Expr.RecordAccess;
import wyjs.lang.Expr.RecordGen;
import wyjs.lang.Expr.TupleGen;
import wyjs.lang.Expr.TypeConst;
import wyjs.lang.Expr.UOp;
import wyjs.lang.Expr.UnOp;
import wyjs.lang.Expr.Variable;
import wyjs.lang.Stmt;
import wyjs.lang.Stmt.Assert;
import wyjs.lang.Stmt.Assign;
import wyjs.lang.Stmt.Debug;
import wyjs.lang.Stmt.For;
import wyjs.lang.Stmt.IfElse;
import wyjs.lang.Stmt.Return;
import wyjs.lang.Stmt.Skip;
import wyjs.lang.Stmt.While;
import wyjs.lang.WhileyFile;
import wyjs.lang.WhileyFile.ConstDecl;
import wyjs.lang.WhileyFile.Decl;
import wyjs.lang.WhileyFile.FunDecl;
import wyjs.lang.WhileyFile.ImportDecl;
import wyjs.lang.WhileyFile.Parameter;
import wyjs.lang.WhileyFile.TypeDecl;
import wyjs.util.Pair;
import wyjs.util.SyntaxError;

public class JsBuilder {

  public JsNode build(WhileyFile wfile) {
    List<JsStmt> nodes = new ArrayList<JsStmt>();
    for (Decl decl : wfile.declarations) {
      nodes.add(doDecl(wfile, decl));
    }
    return new JsBase(nodes);
  }

  public JsStmt doDecl(WhileyFile wfile, Decl decl) {
    if (decl instanceof ImportDecl) {
      return doImport(wfile, (ImportDecl) decl);
    } else if (decl instanceof ConstDecl) {
      return doConst(wfile, (ConstDecl) decl);
    } else if (decl instanceof TypeDecl) {
      return doType(wfile, (TypeDecl) decl);
    } else if (decl instanceof FunDecl) {
      return doFun(wfile, (FunDecl) decl);
    }

    throw new SyntaxError("Unrecognised top-level declaration " + decl.name(),
        wfile.filename, 0, 0);
  }

  public JsStmt doImport(WhileyFile wfile, ImportDecl decl) {
    throw new SyntaxError("No Javascript equivalent to import.",
        wfile.filename, 0, 0);
  }

  public JsStmt doConst(WhileyFile wfile, ConstDecl decl) {
    return new JsConstant(decl.name, doExpr(wfile, decl.constant));
  }

  public JsStmt doType(WhileyFile wfile, TypeDecl decl) {
    throw new SyntaxError("No Javascript equivalent to types.", wfile.filename,
        0, 0);
  }

  public JsStmt doFun(WhileyFile wfile, FunDecl decl) {
    List<String> parameters = decl.parameters.isEmpty() ? null
        : new ArrayList<String>();
    List<JsStmt> body = decl.statements.isEmpty() ? null
        : new ArrayList<JsStmt>();

    for (Parameter parameter : decl.parameters) {
      parameters.add(parameter.name);
    }

    for (Stmt statement : decl.statements) {
      body.add(doStmt(wfile, statement));
    }

    return new JsFunctionStmt(decl.name);
  }

  public JsStmt doStmt(WhileyFile wfile, Stmt stmt) {
    if (stmt instanceof Assign) {
      return doAssign(wfile, (Assign) stmt);
    } else if (stmt instanceof Assert) {
      return doAssert(wfile, (Assert) stmt);
    } else if (stmt instanceof Return) {
      return doReturn(wfile, (Return) stmt);
    } else if (stmt instanceof While) {
      return doWhile(wfile, (While) stmt);
    } else if (stmt instanceof For) {
      return doFor(wfile, (For) stmt);
    } else if (stmt instanceof IfElse) {
      return doIfElse(wfile, (IfElse) stmt);
    } else if (stmt instanceof Skip) {
      return doSkip(wfile, (Skip) stmt);
    } else if (stmt instanceof Debug) {
      return doDebug(wfile, (Debug) stmt);
    }

    throw new SyntaxError("Unrecognised statement " + stmt, wfile.filename, 0,
        0);
  }

  public JsStmt doAssign(WhileyFile wfile, Assign stmt) {
    JsExpr lhs = doExpr(wfile, stmt.lhs);

    if (!(lhs instanceof JsAssignable)) {
      throw new SyntaxError("Unassignable left hand side used: " + lhs,
          wfile.filename, 0, 0);
    }

    return new JsLine(new JsAssign((JsAssignable) lhs, doExpr(wfile, stmt.rhs)));
  }

  public JsStmt doAssert(WhileyFile wfile, Assert stmt) {
    return new JsLine(JsHelpers.assertion(doExpr(wfile, stmt.expr)));
  }

  public JsStmt doReturn(WhileyFile wfile, Return stmt) {
    return new JsReturn(doExpr(wfile, stmt.expr));
  }

  public JsStmt doWhile(WhileyFile wfile, While stmt) {
    return new JsWhile(doExpr(wfile, stmt.condition), collectBody(wfile,
        stmt.body));
  }

  public JsStmt doFor(WhileyFile wfile, For stmt) {
    return new JsFor(stmt.variable, doExpr(wfile, stmt.source), collectBody(
        wfile, stmt.body));
  }

  public JsStmt doIfElse(WhileyFile wfile, IfElse stmt) {
    return new JsIfElse(doExpr(wfile, stmt.condition), collectBody(wfile,
        stmt.trueBranch), collectBody(wfile, stmt.falseBranch));
  }

  private List<JsStmt> collectBody(WhileyFile wfile, List<Stmt> statements) {
    List<JsStmt> body = statements.isEmpty() ? null : new ArrayList<JsStmt>();

    for (Stmt statement : statements) {
      body.add(doStmt(wfile, statement));
    }

    return body;
  }

  public JsStmt doSkip(WhileyFile wfile, Skip stmt) {
    throw new SyntaxError("No Javascript equivalent to skip.", wfile.filename,
        0, 0);
  }

  public JsStmt doDebug(WhileyFile wfile, Debug stmt) {
    return new JsLine(JsHelpers.debug(doExpr(wfile, stmt.expr)));
  }

  public JsExpr doExpr(WhileyFile wfile, Expr expr) {
    if (expr instanceof Variable) {
      return doVariable(wfile, (Variable) expr);
    } else if (expr instanceof NamedConstant) {
      return doNamedConstant(wfile, (NamedConstant) expr);
    } else if (expr instanceof Constant) {
      return doConstant(wfile, (Constant) expr);
    } else if (expr instanceof TypeConst) {
      return doTypeConst(wfile, (TypeConst) expr);
    } else if (expr instanceof BinOp) {
      return doBinOp(wfile, (BinOp) expr);
    } else if (expr instanceof Access) {
      return doListAccess(wfile, (Access) expr);
    } else if (expr instanceof UnOp) {
      return doUnOp(wfile, (UnOp) expr);
    } else if (expr instanceof NaryOp) {
      return doNaryOp(wfile, (NaryOp) expr);
    } else if (expr instanceof Comprehension) {
      return doComprehension(wfile, (Comprehension) expr);
    } else if (expr instanceof RecordAccess) {
      return doRecordAccess(wfile, (RecordAccess) expr);
    } else if (expr instanceof DictionaryGen) {
      return doDictionaryGen(wfile, (DictionaryGen) expr);
    } else if (expr instanceof RecordGen) {
      return doRecordGen(wfile, (RecordGen) expr);
    } else if (expr instanceof TupleGen) {
      return doTupleGen(wfile, (TupleGen) expr);
    } else if (expr instanceof Invoke) {
      return doInvoke(wfile, (Invoke) expr);
    }

    throw new SyntaxError("Unrecognised expression " + expr, wfile.filename, 0,
        0);
  }

  public List<JsExpr> doExprs(WhileyFile wfile, List<? extends Expr> exprs) {
    List<JsExpr> js = new ArrayList<JsExpr>();
    for (Expr expr : exprs) {
      js.add(doExpr(wfile, expr));
    }
    return js;
  }

  public JsExpr doVariable(WhileyFile wfile, Variable expr) {
    return new JsVariable(expr.var);
  }

  public JsExpr doNamedConstant(WhileyFile wfile, NamedConstant expr) {
    return new JsVariable(expr.var);
  }

  public JsExpr doConstant(WhileyFile wfile, Constant expr) {
    return new JsLiteral(expr.value.toString());
  }

  public JsExpr doTypeConst(WhileyFile wfile, TypeConst expr) {
    throw new SyntaxError("Unrecognised expression " + expr, wfile.filename, 0,
        0);
  }

  public JsExpr doBinOp(WhileyFile wfile, BinOp expr) {
    JsBinOp bop = doBinOp(expr.op);
    JsExpr lhs = doExpr(wfile, expr.lhs), rhs = doExpr(wfile, expr.rhs);

    if (bop != null) {
      return bop.newNode(lhs, rhs);
    }

    switch (expr.op) {
    case UNION:
      return new JsInvoke(new JsAccess(lhs, "concat"), rhs);
    case INTERSECTION:
      return JsHelpers.intersect(lhs, rhs);
    case ELEMENTOF:
      return JsHelpers.in(lhs, rhs);
    case SUBSET:
      return JsHelpers.subset(lhs, rhs, false);
    case SUBSETEQ:
      return JsHelpers.subset(lhs, rhs, true);
      // case LISTRANGE:
      // case TYPEIMPLIES:
    }

    throw new SyntaxError("Unrecognised binary operator " + expr.op,
        wfile.filename, 0, 0);
  }

  public JsBinOp doBinOp(BOp bop) {
    switch (bop) {
    case AND:
      return JsBinOp.AND;
    case OR:
      return JsBinOp.OR;
    case ADD:
      return JsBinOp.ADD;
    case SUB:
      return JsBinOp.SUB;
    case MUL:
      return JsBinOp.MUL;
    case DIV:
      return JsBinOp.DIV;
    case EQ:
      return JsBinOp.EQ;
    case NEQ:
      return JsBinOp.NEQ;
    case LT:
      return JsBinOp.LT;
    case LTEQ:
      return JsBinOp.LTE;
    case GT:
      return JsBinOp.GT;
    case GTEQ:
      return JsBinOp.GTE;
    case TYPEEQ:
      return JsBinOp.IOF;
    default:
      return null;
    }
  }

  public JsExpr doListAccess(WhileyFile wfile, Access expr) {
    return new JsAccess(doExpr(wfile, expr.src), doExpr(wfile, expr.index));
  }

  public JsExpr doUnOp(WhileyFile wfile, UnOp expr) {
    JsUnOp uop = doUnOp(expr.op);
    JsExpr mhs = doExpr(wfile, expr.mhs);

    if (uop != null) {
      return uop.newNode(mhs);
    }

    switch (expr.op) {
    case LENGTHOF:
      return new JsAccess(mhs, "length");
    }

    throw new SyntaxError("Unrecognised unary operator " + expr.op,
        wfile.filename, 0, 0);
  }

  public JsUnOp doUnOp(UOp uop) {
    switch (uop) {
    case NOT:
      return JsUnOp.NOT;
    case NEG:
      return JsUnOp.NEG;
    default:
      return null;
    }
  }

  public JsExpr doNaryOp(WhileyFile wfile, NaryOp expr) {
    List<JsExpr> args = doExprs(wfile, expr.arguments);

    switch (expr.op) {
    case LISTGEN:
      return new JsList(args);
    case SETGEN:
      return JsHelpers.newSet(args);
      // case SUBLIST:
    }

    throw new SyntaxError("Unrecognised operator " + expr.op, wfile.filename,
        0, 0);
  }

  public JsExpr doComprehension(WhileyFile wfile, Comprehension expr) {
    throw new SyntaxError("Comprehensions not yet supported.", wfile.filename,
        0, 0);
  }

  public JsExpr doRecordAccess(WhileyFile wfile, RecordAccess expr) {
    return new JsAccess(doExpr(wfile, expr.lhs), expr.name);
  }

  public JsExpr doDictionaryGen(WhileyFile wfile, DictionaryGen expr) {
    List<JsExpr> keys = new ArrayList<JsExpr>(), values = new ArrayList<JsExpr>();

    for (Pair<Expr, Expr> pair : expr.pairs) {
      keys.add(doExpr(wfile, pair.first()));
      values.add(doExpr(wfile, pair.second()));
    }

    return JsHelpers.newMap(keys, values);
  }

  public JsExpr doRecordGen(WhileyFile wfile, RecordGen expr) {
    Map<String, JsExpr> fields = new HashMap<String, JsExpr>();
    for (String name : expr.fields.keySet()) {
      fields.put(name, doExpr(wfile, expr.fields.get(name)));
    }
    return new JsObject(fields);
  }

  public JsExpr doTupleGen(WhileyFile wfile, TupleGen expr) {
    return new JsList(doExprs(wfile, expr.fields));
  }

  public JsExpr doInvoke(WhileyFile wfile, Invoke expr) {
    JsExpr function = expr.receiver == null ? new JsVariable(expr.name)
        : new JsAccess(doExpr(wfile, expr.receiver), expr.name);

    return new JsInvoke(function, doExprs(wfile, expr.arguments));
  }

}
