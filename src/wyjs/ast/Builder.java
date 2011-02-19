package wyjs.ast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import wyjs.ast.stmt.JsRaw;
import wyjs.ast.stmt.JsReturn;
import wyjs.ast.stmt.JsStmt;
import wyjs.ast.stmt.JsWhile;
import wyjs.ast.util.JsHelpers;
import wyjs.ast.util.JsRegex;
import wyjs.lang.Expr;
import wyjs.lang.Expr.Access;
import wyjs.lang.Expr.BOp;
import wyjs.lang.Expr.BinOp;
import wyjs.lang.Expr.Comprehension;
import wyjs.lang.Expr.Constant;
import wyjs.lang.Expr.DictionaryGen;
import wyjs.lang.Expr.Invoke;
import wyjs.lang.Expr.NamedConstant;
import wyjs.lang.Expr.NaryOp;
import wyjs.lang.Expr.RecordAccess;
import wyjs.lang.Expr.RecordGen;
import wyjs.lang.Expr.TupleGen;
import wyjs.lang.Expr.TypeConst;
import wyjs.lang.Expr.UOp;
import wyjs.lang.Expr.UnOp;
import wyjs.lang.Expr.Variable;
import wyjs.lang.Module;
import wyjs.lang.Module.ConstDecl;
import wyjs.lang.Module.Decl;
import wyjs.lang.Module.FunDecl;
import wyjs.lang.Module.ImportDecl;
import wyjs.lang.Module.Parameter;
import wyjs.lang.Module.TypeDecl;
import wyjs.lang.Stmt;
import wyjs.lang.Stmt.Assert;
import wyjs.lang.Stmt.Assign;
import wyjs.lang.Stmt.Debug;
import wyjs.lang.Stmt.ExternJS;
import wyjs.lang.Stmt.For;
import wyjs.lang.Stmt.IfElse;
import wyjs.lang.Stmt.Return;
import wyjs.lang.Stmt.Skip;
import wyjs.lang.Stmt.While;
import wyjs.lang.Type;
import wyjs.util.Attribute;
import wyjs.util.Pair;
import wyjs.util.SyntaxError;

public class Builder {

  public JsNode build(Module wfile) throws IOException {
    List<JsStmt> nodes = new ArrayList<JsStmt>();
    for (Decl decl : wfile.declarations) {
      nodes.add(doDecl(wfile, decl));
    }
    return new JsBase(nodes);
  }

  public JsStmt doDecl(Module wfile, Decl decl) {
    if (decl instanceof ImportDecl) {
      return new JsRaw("");
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

  public JsStmt doConst(Module wfile, ConstDecl decl) {
    return new JsConstant(decl.name, doExpr(wfile, decl.constant));
  }

  public JsStmt doType(Module wfile, TypeDecl decl) {
    return new JsRaw("");
  }

  public JsStmt doFun(Module wfile, FunDecl decl) {
    List<String> parameters = decl.parameters.isEmpty() ? null
        : new ArrayList<String>();
    List<JsStmt> body = decl.statements.isEmpty() ? null
        : new ArrayList<JsStmt>();

    for (Parameter parameter : decl.parameters) {
      parameters.add(parameter.name);
    }

    for (Stmt statement : decl.statements) {
      JsStmt stmt = doStmt(wfile, decl, statement);
      if (stmt != null) {
        body.add(stmt);
      }
    }

    Attribute.FunType attr = decl.attribute(Attribute.FunType.class);
    String mangled = decl.name + "$" + Type.type2str(attr.type);

    return new JsFunctionStmt(mangled, parameters, body);
  }

  public JsStmt doStmt(Module wfile, FunDecl function, Stmt stmt) {
    if (stmt instanceof Assign) {
      return doAssign(wfile, function, (Assign) stmt);
    } else if (stmt instanceof Assert) {
      return doAssert(wfile, (Assert) stmt);
    } else if (stmt instanceof Return) {
      return doReturn(wfile, (Return) stmt);
    } else if (stmt instanceof While) {
      return doWhile(wfile, function, (While) stmt);
    } else if (stmt instanceof For) {
      return doFor(wfile, function, (For) stmt);
    } else if (stmt instanceof IfElse) {
      return doIfElse(wfile, function, (IfElse) stmt);
    } else if (stmt instanceof ExternJS) {
      return doExtern(wfile, (ExternJS) stmt);
    } else if (stmt instanceof Skip) {
      return doSkip(wfile, (Skip) stmt);
    } else if (stmt instanceof Debug) {
      return doDebug(wfile, (Debug) stmt);
    } else if (stmt instanceof Expr) {
      return new JsLine(doExpr(wfile, (Expr) stmt));
    }

    throw new SyntaxError("Unrecognised statement " + stmt, wfile.filename, 0,
        0);
  }

  public JsStmt doAssign(Module wfile, FunDecl function, Assign stmt) {
    Expr slhs = stmt.lhs, srhs = stmt.rhs;
    JsExpr lhs = doExpr(wfile, slhs);

    if (!(lhs instanceof JsAssignable)) {
      throw new SyntaxError("Unassignable left hand side used: " + lhs,
          wfile.filename, 0, 0);
    }

    JsExpr rhs = doExpr(wfile, srhs);

    if (slhs instanceof Variable && srhs instanceof Variable) {
      boolean found = false;
      List<Stmt> body = function.statements;
      for (int i = 0; i < body.size(); ++i) {
        Stmt s = body.get(i);
        if (!found) {
          if (s == stmt) {
            found = true;
          }
          continue;
        }

        // Just placing this first is the conservative option, but it works
        // for now.
        if (modifies(s, (Variable) slhs) || modifies(s, (Variable) srhs)) {
          rhs = JsHelpers.clone(rhs);
          break;
        }
        if (assigns(s, slhs)) {
          Expr alhs = ((Assign) s).lhs;
          if (alhs == slhs) {
            break;
          }
        }
      }
    }

    return new JsLine(new JsAssign((JsAssignable) lhs, rhs));
  }

  private Set<Stmt> collect(Stmt stmt) {
    Set<Stmt> stmts = new HashSet<Stmt>();
    if (stmt instanceof For) {
      stmts.addAll(((For) stmt).body);
    } else if (stmt instanceof While) {
      stmts.addAll(((While) stmt).body);
    } else if (stmt instanceof IfElse) {
      IfElse ie = (IfElse) stmt;
      stmts.addAll(ie.trueBranch);
      stmts.addAll(ie.falseBranch);
    }
    return stmts;
  }

  private boolean assigns(Stmt stmt, Expr expr) {
    for (Stmt s : collect(stmt)) {
      if (assigns(s, expr)) {
        return true;
      }
    }
    if (stmt instanceof Assign) {
      Assign assign = (Assign) stmt;
      return assign.lhs == expr;
    }

    return false;
  }

  private boolean modifies(Stmt stmt, Variable expr) {
    for (Stmt s : collect(stmt)) {
      if (modifies(s, expr)) {
        return true;
      }
    }
    if (stmt instanceof Assign) {
      Assign assign = (Assign) stmt;
      Expr lhs = assign.lhs;
      if (lhs instanceof RecordAccess || lhs instanceof Access) {
        if (find(lhs, expr)) {
          return true;
        }
      }
    } else if (stmt instanceof Invoke) {
      for (Expr arg : ((Invoke) stmt).arguments) {
        if (find(arg, expr)) {
          return true;
        }
      }
    }

    return false;
  }

  private boolean find(Expr expr, Variable thing) {
    if (expr instanceof Variable) {
      if (((Variable) expr).var.equals(thing.var)) {
        return true;
      }
    }

    if (expr instanceof BinOp) {
      BinOp bop = (BinOp) expr;
      return find(bop.lhs, thing) || find(bop.rhs, thing);
    } else if (expr instanceof Access) {
      Access acc = (Access) expr;
      return find(acc.src, thing) || find(acc.index, thing);
    } else if (expr instanceof UnOp) {
      return find(((UnOp) expr).mhs, thing);
    } else if (expr instanceof NaryOp) {
      return findIn(((NaryOp) expr).arguments, thing);
    } else if (expr instanceof Comprehension) {
      Comprehension com = (Comprehension) expr;
      return find(com.condition, thing) || find(com.value, thing)
          || findInPairs(com.sources, thing);
    } else if (expr instanceof RecordAccess) {
      return find(((RecordAccess) expr).lhs, thing);
    } else if (expr instanceof DictionaryGen) {
      return findInPairs(((DictionaryGen) expr).pairs, thing);
    } else if (expr instanceof RecordGen) {
      return findIn(((RecordGen) expr).fields.values(), thing);
    } else if (expr instanceof TupleGen) {
      return findIn(((TupleGen) expr).fields, thing);
    } else if (expr instanceof Invoke) {
      Invoke inv = (Invoke) expr;
      return find(inv.receiver, thing) || findIn(inv.arguments, thing);
    }

    return false;
  }

  private boolean findIn(Iterable<Expr> exprs, Variable thing) {
    for (Expr e : exprs) {
      if (find(e, thing)) {
        return true;
      }
    }
    return false;
  }

  private boolean findInPairs(Iterable<? extends Pair<?, Expr>> exprs,
      Variable thing) {
    for (Pair<?, Expr> e : exprs) {
      Object first = e.first();
      if (first instanceof Expr) {
        if (find((Expr) first, thing)) {
          return true;
        }
      }
      if (find(e.second(), thing)) {
        return true;
      }
    }
    return false;
  }

  public JsStmt doAssert(Module wfile, Assert stmt) {
    return new JsLine(JsHelpers.assertion(doExpr(wfile, stmt.expr)));
  }

  public JsStmt doReturn(Module wfile, Return stmt) {
    if (stmt.expr != null) {
      return new JsReturn(JsHelpers.clone(doExpr(wfile, stmt.expr)));
    }
    return new JsReturn();
  }

  public JsStmt doWhile(Module wfile, FunDecl function, While stmt) {
    return new JsWhile(doExpr(wfile, stmt.condition), collectBody(wfile,
        function, stmt.body));
  }

  public JsStmt doFor(Module wfile, FunDecl function, For stmt) {
    return new JsFor(stmt.variable, doExpr(wfile, stmt.source), collectBody(
        wfile, function, stmt.body));
  }

  public JsStmt doIfElse(Module wfile, FunDecl function, IfElse stmt) {
    return new JsIfElse(doExpr(wfile, stmt.condition), collectBody(wfile,
        function, stmt.trueBranch), collectBody(wfile, function,
        stmt.falseBranch));
  }

  private List<JsStmt> collectBody(Module wfile, FunDecl function,
      List<Stmt> statements) {
    List<JsStmt> body = statements.isEmpty() ? null : new ArrayList<JsStmt>();

    for (Stmt statement : statements) {
      body.add(doStmt(wfile, function, statement));
    }

    return body;
  }

  public JsStmt doExtern(Module wfile, ExternJS stmt) {
    return new JsRaw(stmt.javascript);
  }

  public JsStmt doSkip(Module wfile, Skip stmt) {
    return null;
  }

  public JsStmt doDebug(Module wfile, Debug stmt) {
    return new JsLine(JsHelpers.debug(doExpr(wfile, stmt.expr)));
  }

  public JsExpr doExpr(Module wfile, Expr expr) {
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

  public List<JsExpr> doExprs(Module wfile, List<? extends Expr> exprs) {
    List<JsExpr> js = new ArrayList<JsExpr>();
    for (Expr expr : exprs) {
      js.add(doExpr(wfile, expr));
    }
    return js;
  }

  public JsExpr doVariable(Module wfile, Variable expr) {
    return new JsVariable(expr.var);
  }

  public JsExpr doNamedConstant(Module wfile, NamedConstant expr) {
    return new JsVariable(expr.var);
  }

  public JsExpr doConstant(Module wfile, Constant expr) {
    Object value = expr.value;
    String literal = value.toString();
    if (value instanceof String) {
      literal = JsRegex.stringify(literal);
    }
    return new JsLiteral(literal);
  }

  public JsExpr doTypeConst(Module wfile, TypeConst expr) {
    throw new SyntaxError("Unrecognised expression " + expr, wfile.filename, 0,
        0);
  }

  public JsExpr doBinOp(Module wfile, BinOp expr) {
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

  public JsExpr doListAccess(Module wfile, Access expr) {
    return new JsAccess(doExpr(wfile, expr.src), new JsInvoke(new JsVariable(
        "str$StA"), doExpr(wfile, expr.index)));
  }

  public JsExpr doUnOp(Module wfile, UnOp expr) {
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

  public JsExpr doNaryOp(Module wfile, NaryOp expr) {
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

  public JsExpr doComprehension(Module wfile, Comprehension expr) {
    throw new SyntaxError("Comprehensions not yet supported.", wfile.filename,
        0, 0);
  }

  public JsExpr doRecordAccess(Module wfile, RecordAccess expr) {
    return new JsAccess(doExpr(wfile, expr.lhs), expr.name);
  }

  public JsExpr doDictionaryGen(Module wfile, DictionaryGen expr) {
    List<JsExpr> keys = new ArrayList<JsExpr>(), values = new ArrayList<JsExpr>();

    for (Pair<Expr, Expr> pair : expr.pairs) {
      keys.add(doExpr(wfile, pair.first()));
      values.add(doExpr(wfile, pair.second()));
    }

    return JsHelpers.newMap(keys, values);
  }

  public JsExpr doRecordGen(Module wfile, RecordGen expr) {
    Map<String, JsExpr> fields = new HashMap<String, JsExpr>();
    for (String name : expr.fields.keySet()) {
      fields.put(name, doExpr(wfile, expr.fields.get(name)));
    }
    return new JsObject(fields);
  }

  public JsExpr doTupleGen(Module wfile, TupleGen expr) {
    return JsHelpers.newTuple(doExprs(wfile, expr.fields));
  }

  public JsExpr doInvoke(Module wfile, Invoke expr) {
    Attribute.FunType attr = expr.attribute(Attribute.FunType.class);
    String mangled = expr.name + "$" + Type.type2str(attr.type);

    JsExpr function = expr.receiver == null ? new JsVariable(mangled)
        : new JsAccess(doExpr(wfile, expr.receiver), mangled);

    List<JsExpr> args = doExprs(wfile, expr.arguments);
    for (int i = 0; i < args.size(); ++i) {
      args.set(i, JsHelpers.clone(args.get(i)));
    }

    return new JsInvoke(function, args);
  }

}
