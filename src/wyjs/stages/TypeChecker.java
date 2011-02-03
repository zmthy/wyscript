package wyjs.stages;

import static wyjs.util.SyntaxError.syntaxError;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import wyjs.lang.Expr;
import wyjs.lang.Expr.*;
import wyjs.lang.ModuleID;
import wyjs.lang.NameID;
import wyjs.lang.PkgID;
import wyjs.lang.Stmt;
import wyjs.lang.Stmt.*;
import wyjs.lang.Type;
import wyjs.lang.UnresolvedType;
import wyjs.lang.WhileyFile;
import wyjs.lang.WhileyFile.*;
import wyjs.util.Attribute;
import wyjs.util.Pair;
import wyjs.util.ResolveError;
import wyjs.util.SyntacticElement;
import wyjs.util.SyntaxError;

public class TypeChecker {

  private HashSet<ModuleID> modules;
  private HashMap<NameID, WhileyFile> filemap;
  private HashMap<NameID, List<Type.Fun>> functions;
  private HashMap<NameID, Type> types;
  private HashMap<NameID, Expr> constants;
  private HashMap<NameID, UnresolvedType> unresolved;
  private String filename;
  private FunDecl currentFunDecl;    
  
  public void check(List<WhileyFile> files) {
		modules = new HashSet<ModuleID>();
		filemap = new HashMap<NameID, WhileyFile>();
		functions = new HashMap<NameID, List<Type.Fun>>();
		types = new HashMap<NameID, Type>();
		constants = new HashMap<NameID, Expr>();
		unresolved = new HashMap<NameID, UnresolvedType>();

		// now, init data
		for (WhileyFile f : files) {
			modules.add(f.module);
		}

		// Stage 1 ... resolve and check types of all named types + constants
		generateConstants(files);
		generateTypes(files);

		// Stage 2 ... resolve and check types for all functions
		for (WhileyFile f : files) {
			filename = f.filename;
			for (WhileyFile.Decl d : f.declarations) {
				if (d instanceof FunDecl) {
					partResolve(f.module, (FunDecl) d);
				}
			}
		}

		// Stage 3 ... propagate types through all expressions
		for (WhileyFile f : files) {
			resolve(f);
		}
	}

  // =======================================================================
  // Stage 1 --- Generate and expand all constants and types
  // =======================================================================

  /**
   * The following method visits every define constant statement in every whiley
   * file being compiled, and determines its true and value.
   * 
   * @param files
   */
  protected void generateConstants(List<WhileyFile> files) {
    HashMap<NameID, Expr> exprs = new HashMap<NameID, Expr>();

    // first construct list.
    for (WhileyFile f : files) {
      for (Decl d : f.declarations) {
        if (d instanceof ConstDecl) {
          ConstDecl cd = (ConstDecl) d;
          NameID key = new NameID(f.module, cd.name());
          exprs.put(key, cd.constant);
          filemap.put(key, f);
        }
      }
    }

    for (NameID k : exprs.keySet()) {
      try {
        Expr v = expandConstant(k, exprs, new HashSet<NameID>());
        constants.put(k, v);
        /**
         * FIXME: this needs to be updated at some point Type t = v.type(); if
         * (t instanceof Type.Set) { Type.Set st = (Type.Set) t; types.put(k,
         * st.element); }
         */
      } catch (ResolveError rex) {
        syntaxError(rex.getMessage(), filemap.get(k).filename, exprs.get(k),
            rex);
      }
    }
  }

  /**
   * The expand constant method is responsible for turning a named constant
   * expression into a value. This is done by traversing the constant's
   * expression and recursively expanding any named constants it contains.
   * Simplification of constants is also performed where possible.
   * 
   * @param key --- name of constant we are expanding.
   * @param exprs --- mapping of all names to their( declared) expressions
   * @param visited --- set of all constants seen during this traversal (used to
   *          detect cycles).
   * @return
   * @throws ResolveError
   */
  protected Expr expandConstant(NameID key, HashMap<NameID, Expr> exprs,
      HashSet<NameID> visited) throws ResolveError {
    Expr e = exprs.get(key);
    Expr value = constants.get(key);
    if (value != null) {
      return value;
    } else if (visited.contains(key)) {
      // this indicates a cyclic definition.
      syntaxError("cyclic constant definition encountered",
          filemap.get(key).filename, exprs.get(key));
    } else {
      visited.add(key); // mark this node as visited
    }

    // At this point, we need to replace every unresolved variable with a
    // constant definition.
    Expr v = expandConstantHelper(e, filemap.get(key).filename, exprs, visited);
    constants.put(key, v);
    return v;
  }

  /**
   * The following is a helper method for expandConstant. It takes a given
   * expression (rather than the name of a constant) and expands to a value
   * (where possible). If the expression contains, for example, method or
   * function declarations then this will certainly fail (producing a syntax
   * error).
   * 
   * @param key --- name of constant we are expanding.
   * @param exprs --- mapping of all names to their( declared) expressions
   * @param visited --- set of all constants seen during this traversal (used to
   *          detect cycles).
   */
  protected Expr expandConstantHelper(Expr expr, String filename,
      HashMap<NameID, Expr> exprs, HashSet<NameID> visited) throws ResolveError {
    if (expr instanceof Constant) {
      Constant c = (Constant) expr;
      return c;
    } else if (expr instanceof Variable) {
      // Note, this must be a constant definition of some sort
      Variable v = (Variable) expr;
      // FIXME: for when we put namespacing back in.
      ModuleID mid = v.attribute(Attribute.Module.class).module;
      NameID name = new NameID(mid, v.var);
      return expandConstant(name, exprs, visited);
    }

    syntaxError("invalid expression in constant definition", filename, expr);
    return null;
  }

  /**
   * The following method visits every define type statement in every whiley
   * file being compiled, and determines its true type.
   * 
   * @param files
   */
  protected void generateTypes(List<WhileyFile> files) {
    HashMap<NameID, SyntacticElement> srcs = new HashMap<NameID, SyntacticElement>();

    // The declOrder list is basically a hack. It ensures that types are
    // visited in the order that they are declared. This helps give some
    // sense to the way recursive types are handled, but a more general
    // solution could easily be found.
    ArrayList<NameID> declOrder = new ArrayList<NameID>();

    // second construct list.
    for (WhileyFile f : files) {
      for (Decl d : f.declarations) {
        if (d instanceof TypeDecl) {
          TypeDecl td = (TypeDecl) d;
          NameID key = new NameID(f.module, td.name());
          declOrder.add(key);
          unresolved.put(key, td.type);
          srcs.put(key, d);
          filemap.put(key, f);
        }
      }
    }

    // third expand all types
    for (NameID key : declOrder) {
      try {
        HashMap<NameID, Type> cache = new HashMap<NameID, Type>();
        Type t = expandType(key, cache);
        if (Type.isExistential(t)) {
          t = Type.T_NAMED(key, t);
        }
        types.put(key, t);
      } catch (ResolveError ex) {
        syntaxError(ex.getMessage(), filemap.get(key).filename, srcs.get(key),
            ex);
      }
    }
  }

  /**
   * This is a deeply complex method!
   * 
   * @param key
   * @param cache
   * @return A triple of the form <T,B,C>, where T is the type, B is the
   *         constraint block and C indicates whether or not this is in fact a
   *         constrained type. The latter is useful since it means we can throw
   *         away unnecessary constraint blocks when the type in question is not
   *         actually constrained.
   * @throws ResolveError
   */
  protected Type expandType(NameID key, HashMap<NameID, Type> cache)
      throws ResolveError {

    Type cached = cache.get(key);
    Type t = types.get(key);

    if (cached != null) {
      return cached;
    } else if (t != null) {
      return t;
    }

    // following is needed to terminate any recursion
    cache.put(key, Type.T_RECURSIVE(key, null));

    // now, expand the type fully
    t = expandType(unresolved.get(key), filemap.get(key).filename, cache);

    // Now, we need to test whether the current type is open and recursive
    // on this name. In such case, we must close it in order to complete the
    // recursive type.
    boolean isOpenRecursive = Type.isOpenRecursive(key, t);
    if (isOpenRecursive) {
      t = Type.T_RECURSIVE(key, t);
    }

    // finally, store it in the cache
    cache.put(key, t);

    // Done
    return t;
  }

  protected Type expandType(UnresolvedType t, String filename,
      HashMap<NameID, Type> cache) {
    if (t instanceof UnresolvedType.List) {
      UnresolvedType.List lt = (UnresolvedType.List) t;
      return Type.T_LIST(expandType(lt.element, filename, cache));
    } else if (t instanceof UnresolvedType.Set) {
      UnresolvedType.Set st = (UnresolvedType.Set) t;
      return Type.T_SET(expandType(st.element, filename, cache));
    } else if (t instanceof UnresolvedType.Dictionary) {
      UnresolvedType.Dictionary st = (UnresolvedType.Dictionary) t;
      return Type.T_DICTIONARY(expandType(st.key, filename, cache),
          expandType(st.value, filename, cache));
    } else if (t instanceof UnresolvedType.Record) {
      UnresolvedType.Record tt = (UnresolvedType.Record) t;
      HashMap<String, Type> types = new HashMap<String, Type>();
      for (Map.Entry<String, UnresolvedType> e : tt.types.entrySet()) {
        Type p = expandType(e.getValue(), filename, cache);
        types.put(e.getKey(), p);
      }
      return Type.T_RECORD(types);
    } else if (t instanceof UnresolvedType.Union) {
      UnresolvedType.Union ut = (UnresolvedType.Union) t;
      HashSet<Type.NonUnion> bounds = new HashSet<Type.NonUnion>();
      for (int i = 0; i != ut.bounds.size(); ++i) {
        UnresolvedType b = ut.bounds.get(i);

        Type bt = expandType(b, filename, cache);
        if (bt instanceof Type.NonUnion) {
          bounds.add((Type.NonUnion) bt);
        } else {
          bounds.addAll(((Type.Union) bt).bounds);
        }
      }

      // Type type;
      if (bounds.size() == 1) {
        return bounds.iterator().next();
      } else {
        return Type.leastUpperBound(bounds);
      }
    } else if (t instanceof UnresolvedType.Named) {
      UnresolvedType.Named dt = (UnresolvedType.Named) t;
      // FIXME: for when we put namespacing back in
      ModuleID module = dt.attribute(Attribute.Module.class).module;
      NameID name = new NameID(module, dt.name);

      try {
        return expandType(name, cache);
      } catch (ResolveError rex) {
        syntaxError(rex.getMessage(), filename, t, rex);
        return null;
      }
    } else {
      // for base cases
      return resolve(t);
    }
  }

  // =======================================================================
  // Stage 2 --- Determine actual types for all functions
  // =======================================================================

  protected void partResolve(ModuleID module, FunDecl fd) {

    ArrayList<Type> parameters = new ArrayList<Type>();
    for (WhileyFile.Parameter p : fd.parameters) {
      parameters.add(resolve(p.type));
    }

    // method return type
    Type ret = resolve(fd.ret);

    Type.Fun ft = Type.T_FUN(null, ret, parameters);
    NameID name = new NameID(module, fd.name);
    List<Type.Fun> types = functions.get(name);
    if (types == null) {
      types = new ArrayList<Type.Fun>();
      functions.put(name, types);
    }
    types.add(ft);
    fd.attributes().add(new TypeAttr(ft));
  }

  // =======================================================================
  // Stage 3 --- Propagate types through expressions
  // =======================================================================

  public void resolve(WhileyFile wf) {
    this.filename = wf.filename;
    for (WhileyFile.Decl d : wf.declarations) {
      try {
        if (d instanceof FunDecl) {
          resolve((FunDecl) d, wf);
        }
      } catch (SyntaxError se) {
        throw se;
      } catch (Throwable ex) {
        syntaxError("internal failure", wf.filename, d, ex);
      }
    }
  }

  protected void resolve(FunDecl fd, WhileyFile wf) {
    Environment environment = new Environment();
    currentFunDecl = fd;
    
    // First, initialise typing environment
    Type.Fun tf = (Type.Fun) fd.attribute(TypeAttr.class).type;

    for (int i = 0; i != fd.parameters.size(); ++i) {
      String name = fd.parameters.get(i).name();
      Type type = tf.params.get(i);
      environment.put(name, type);
    }

    // Second, propagate types through all expressions
    for (Stmt s : fd.statements) {
      environment = resolve(s, environment);
    }

    currentFunDecl = null;
  }

  public Environment resolve(Stmt stmt, Environment environment) {
    if (environment == null) {
      syntaxError("unreachable code", filename, stmt);
    }
    try {
      if (stmt instanceof Assign) {
        return resolve((Assign) stmt, environment);
      } else if (stmt instanceof Assert) {
        return resolve((Assert) stmt, environment);
      } else if (stmt instanceof Return) {
        return resolve((Return) stmt, environment);
      } else if (stmt instanceof Debug) {
        return resolve((Debug) stmt, environment);
      } else if (stmt instanceof IfElse) {
        return resolve((IfElse) stmt, environment);
      } else if (stmt instanceof While) {
        return resolve((While) stmt, environment);
      } else if (stmt instanceof For) {
        return resolve((For) stmt, environment);
      } else if (stmt instanceof Skip) {
        return resolve((Skip) stmt, environment);
      } else if (stmt instanceof Invoke) {
    	  resolve((Invoke) stmt, environment);
    	  return environment;
      }
    } catch (SyntaxError se) {
      throw se;
    } catch (Exception ex) {
      syntaxError("internal failure", filename, stmt, ex);
    }

    syntaxError("unknown statement encountered (" + stmt + ")", filename, stmt);

    return null;
  }

  protected Environment resolve(Assign s, Environment environment) {

    Type rhs_t = resolve(s.rhs, environment);

    if (s.lhs instanceof Variable) {
      Variable v = (Variable) s.lhs;
      environment = new Environment(environment);
      environment.put(v.var, rhs_t);
    } else {
      Type lhs_t = resolve(s.lhs, environment);
      checkSubtype(lhs_t, rhs_t, s);
    }

    return environment;
  }

  protected Environment resolve(Assert s, Environment environment) {
    Type t = resolve(s.expr, environment);
    checkSubtype(Type.T_BOOL, t, s);
    return environment;
  }

  protected Environment resolve(Return s, Environment environment) {

    if (s.expr != null) {
      Type t = resolve(s.expr, environment);
      TypeAttr ta = currentFunDecl.attribute(TypeAttr.class);
      Type.Fun ft = (Type.Fun) ta.type;
      checkSubtype(ft.ret, t, s.expr);
    }

    return null;
  }

  protected Environment resolve(Skip s, Environment environment) {
    // TODO: remove skip statement?
    return environment;
  }

  protected Environment resolve(Debug s, Environment environment) {
    resolve(s.expr, environment);
    // TO DO ... check type is a string?
    return environment;
  }

  protected Environment resolve(IfElse s, Environment environment) {
    // TODO: update to allow retyping by type tests
    Type t = resolve(s.condition, environment);
    checkSubtype(Type.T_BOOL, t, s.condition);

    Environment trueEnv = environment;
    Environment falseEnv = environment;

    for (Stmt stmt : s.trueBranch) {
      trueEnv = resolve(stmt, trueEnv);
    }

    for (Stmt stmt : s.falseBranch) {
      falseEnv = resolve(stmt, falseEnv);
    }

    return join(trueEnv, falseEnv);
  }

  protected Environment resolve(For s, Environment environment) {
    // check variable not already defined
    if (environment.containsKey(s.variable)) {
      syntaxError(s.variable + " is already defined", filename, s);
    }

    // setup initial environment and check source is a collection
    Environment bodyEnv = new Environment(environment);
    Type src_t = resolve(s.source, environment);
    checkSubtype(Type.T_SET(Type.T_ANY), src_t, s.source);
    // FIXME: following is broken
    Type.SetList sl = (Type.SetList) src_t;
    bodyEnv.put(s.variable, sl.element());

    // Now, iterate until a fixed point is reached
    Environment oldEnv;
    Environment startEnv = bodyEnv;
    do {
      oldEnv = bodyEnv;
      for (Stmt stmt : s.body) {
        bodyEnv = resolve(stmt, bodyEnv);
      }
      bodyEnv = join(bodyEnv, startEnv);
    } while (!oldEnv.equals(bodyEnv));

    return join(bodyEnv, environment);
  }

  protected Environment resolve(While s, Environment environment) {
    // setup initial environment and check source is a collection
    Type src_t = resolve(s.condition, environment);
    checkSubtype(Type.T_BOOL, src_t, s.condition);

    // Now, iterate until a fixed point is reached
    Environment oldEnv;
    Environment startEnv = environment;
    do {
      oldEnv = environment;
      for (Stmt stmt : s.body) {
        environment = resolve(stmt, environment);
      }
      environment = join(environment, startEnv);
    } while (!oldEnv.equals(environment));

    return environment;
  }

  protected Type resolve(Expr e, Environment environment) {
    try {
      if (e instanceof Constant) {
        return resolve((Constant) e, environment);
      } else if (e instanceof Variable) {
        return resolve((Variable) e, environment);
      } else if (e instanceof UnOp) {
        return resolve((UnOp) e, environment);
      } else if (e instanceof Invoke) {
        return resolve((Invoke) e, environment);
      } else if (e instanceof BinOp) {
        return resolve((BinOp) e, environment);
      } else if (e instanceof NaryOp) {
        return resolve((NaryOp) e, environment);
      } else if (e instanceof RecordGen) {
        return resolve((RecordGen) e, environment);
      } else if (e instanceof RecordAccess) {
        return resolve((RecordAccess) e, environment);
      } else if (e instanceof DictionaryGen) {
        return resolve((DictionaryGen) e, environment);
      } else if (e instanceof Access) {
        return resolve((Access) e, environment);
      } else if (e instanceof TupleGen) {
        return resolve((TupleGen) e, environment);
      } else {
        syntaxError("unknown expression encountered", filename, e);
      }
    } catch (SyntaxError se) {
      throw se;
    } catch (Exception ex) {
      syntaxError("internal failure", filename, e, ex);
    }
    return null;
  }

  protected Type resolve(Constant c, Environment environment) {
    Object v = c.value;
    if (v instanceof Boolean) {
      return Type.T_BOOL;
    } else if (v instanceof Character) {
      return Type.T_CHAR;
    }else if (v instanceof Integer) {
      return Type.T_INT;
    } else if (v instanceof Double) {
      return Type.T_REAL;
    }
    syntaxError("unknown constant encountered", filename, c);
    return null;
  }

  protected Type resolve(Variable v, Environment environment)
      throws ResolveError {
    Type v_t = environment.get(v.var);
    if (v_t != null) { return v_t; }
    // Not a variable, but could be a constant
    Attribute.Module mattr = v.attribute(Attribute.Module.class);
    if(mattr != null) {
    	Expr constant = constants.get(new NameID(mattr.module,v.var));    	
    	return resolve(constant,environment);
    }
    syntaxError("variable not defined", filename, v);
    return null;
  }

  protected Type resolve(Invoke ivk, Environment environment) {

		ArrayList<Type> types = new ArrayList<Type>();

		for (Expr arg : ivk.arguments) {
			Type arg_t = resolve(arg, environment);
			types.add(arg_t);
		}

		try {
			// FIXME: when putting name spacing back in, we'll need to fix this.
			ModuleID mid = ivk.attribute(Attribute.Module.class).module;
			NameID nid = new NameID(mid, ivk.name);
			Type.Fun funtype = bindFunction(nid, types, ivk);
			// now, udpate the invoke
			ivk.attributes().add(new Attribute.FunType(funtype));
			return funtype.ret;
		} catch (ResolveError ex) {
			syntaxError(ex.getMessage(), filename, ivk);
			return null; // unreachable
		}
	}

	/**
	 * Bind function is responsible for determining the true type of a method or
	 * function being invoked. To do this, it must find the function/method with
	 * the most precise type that matches the argument types. *
	 * 
	 * @param nid
	 * @param receiver
	 * @param paramTypes
	 * @param elem
	 * @return
	 * @throws ResolveError
	 */
	protected Type.Fun bindFunction(NameID nid, List<Type> paramTypes,
			SyntacticElement elem) throws ResolveError {
		Type receiver = null; // dummy
		Type.Fun target = Type.T_FUN(null, Type.T_ANY, paramTypes);
		Type.Fun candidate = null;

		List<Type.Fun> targets = lookupMethod(nid);

		for (Type.Fun ft : targets) {
			Type funrec = ft.receiver;
			if (receiver == funrec
					|| (receiver != null && funrec != null && Type.isSubtype(
							funrec, receiver))) {
				// receivers match up OK ...
				if (ft.params.size() == paramTypes.size()
						&& Type.isSubtype(ft, target)
						&& (candidate == null || Type.isSubtype(candidate, ft))) {
					// This declaration is a candidate. Now, we need to see if
					// our
					// candidate type signature is as precise as possible.
					if (candidate == null) {
						candidate = ft;
					} else if (Type.isSubtype(candidate, ft)) {
						candidate = ft;
					}
				}
			}
		}

		// Check whether we actually found something. If not, print a useful
		// error message.
		if (candidate == null) {
			String msg = "no match for " + nid.name()
					+ parameterString(paramTypes);
			boolean firstTime = true;
			int count = 0;
			for (Type.Fun ft : targets) {
				if (firstTime) {
					msg += "\n\tfound: " + nid.name()
							+ parameterString(ft.params);
				} else {
					msg += "\n\tand: " + nid.name()
							+ parameterString(ft.params);
				}
				if (++count < targets.size()) {
					msg += ",";
				}
			}

			syntaxError(msg + "\n", filename, elem);
		}

		return candidate;
	}

	private String parameterString(List<Type> paramTypes) {
		String paramStr = "(";
		boolean firstTime = true;
		for (Type t : paramTypes) {
			if (!firstTime) {
				paramStr += ",";
			}
			firstTime = false;
			paramStr += Type.toShortString(t);
		}
		return paramStr + ")";
	}

	protected List<Type.Fun> lookupMethod(NameID nid) throws ResolveError {
		List<Type.Fun> matches = functions.get(nid);
		if (matches == null) {
			return Collections.EMPTY_LIST;
		} else {
			return matches;
		}
	}
  
  
  protected Type resolve(UnOp uop, Environment environment) throws ResolveError {
    Type t = resolve(uop.mhs, environment);
    switch (uop.op) {
    case LENGTHOF:
      checkSubtype(Type.T_SET(Type.T_ANY), t, uop.mhs);
      Type.SetList sl = (Type.SetList) t;
      return sl.element();
    case NEG:
      checkSubtype(Type.T_REAL, t, uop.mhs);
      return t;
    case NOT:
      checkSubtype(Type.T_BOOL, t, uop.mhs);
      return t;
    }
    syntaxError("unknown unary expression encountered", filename, uop);
    return null;
  }

  protected Type resolve(BinOp bop, Environment environment)
      throws ResolveError {
    Type lhs_t = resolve(bop.lhs, environment);
    Type rhs_t = resolve(bop.rhs, environment);

    // FIXME: really need to add coercions somehow

    switch (bop.op) {
    case ADD: {
      if (Type.isSubtype(Type.T_SET(Type.T_ANY), lhs_t)
          || Type.isSubtype(Type.T_SET(Type.T_ANY), rhs_t)) {
        checkSubtype(Type.T_SET(Type.T_ANY), lhs_t, bop.lhs);
        checkSubtype(Type.T_SET(Type.T_ANY), rhs_t, bop.rhs);
        // need to update operation
        bop.op = BOp.UNION;
        return Type.leastUpperBound(lhs_t, rhs_t);
      }
    }
    case SUB:
    case DIV:
    case MUL: {
      checkSubtype(Type.T_REAL, lhs_t, bop.lhs);
      checkSubtype(Type.T_REAL, rhs_t, bop.rhs);
      return Type.leastUpperBound(lhs_t, rhs_t);
    }
    case LT:
    case LTEQ:
    case GT:
    case GTEQ: {
      checkSubtype(Type.T_REAL, lhs_t, bop.lhs);
      checkSubtype(Type.T_REAL, rhs_t, bop.rhs);
      return Type.T_BOOL;
    }
    case AND:
    case OR: {
      checkSubtype(Type.T_BOOL, lhs_t, bop.lhs);
      checkSubtype(Type.T_BOOL, rhs_t, bop.rhs);
      return Type.T_BOOL;
    }
    }

    syntaxError("unknown binary expression encountered", filename, bop);
    return null;
  }

  protected Type resolve(NaryOp nop, Environment environment) {
    if (nop.op == NOp.SUBLIST) {
      Expr src = nop.arguments.get(0);
      Expr start = nop.arguments.get(1);
      Expr end = nop.arguments.get(2);
      Type src_t = resolve(src, environment);
      Type start_t = resolve(start, environment);
      Type end_t = resolve(end, environment);
      checkSubtype(Type.T_LIST(Type.T_ANY), src_t, src);
      checkSubtype(Type.T_INT, start_t, start);
      checkSubtype(Type.T_INT, end_t, end);
      return src_t;
    } else {
      // Must be a set or list generator
      Type lub = Type.T_VOID;
      for (Expr e : nop.arguments) {
        Type t = resolve(e, environment);
        lub = Type.leastUpperBound(lub, t);
      }

      if (nop.op == NOp.SETGEN) {
        return Type.T_SET(lub);
      } else {
        return Type.T_LIST(lub);
      }
    }
  }

  protected Type resolve(RecordGen rg, Environment environment) {
    HashMap<String, Type> types = new HashMap<String, Type>();

    for (Map.Entry<String, Expr> f : rg.fields.entrySet()) {
      Type t = resolve(f.getValue(), environment);
      types.put(f.getKey(), t);
    }

    return Type.T_RECORD(types);
  }

  protected Type resolve(RecordAccess ra, Environment environment) {
    Type src = resolve(ra.lhs, environment);
    Type.Record ert = Type.effectiveRecordType(src);
    if (ert == null) {
      syntaxError("expected record type, got " + src, filename, ra.lhs);
    }
    Type t = ert.types.get(ra.name);
    if (t == null) {
      syntaxError("no such field in type: " + ert, filename, ra);
    }
    return t;
  }

  protected Type resolve(DictionaryGen rg, Environment environment) {
    Type keyType = Type.T_VOID;
    Type valueType = Type.T_VOID;

    for (Pair<Expr, Expr> p : rg.pairs) {
      Type kt = resolve(p.first(), environment);
      Type vt = resolve(p.second(), environment);
      keyType = Type.leastUpperBound(keyType, kt);
      valueType = Type.leastUpperBound(valueType, vt);
    }

    return Type.T_DICTIONARY(keyType, valueType);
  }

  protected Type resolve(Access ra, Environment environment) {
    Type src = resolve(ra.src, environment);
    Type idx = resolve(ra.index, environment);
    Type.Dictionary edt = Type.effectiveDictionaryType(src);

    if (edt == null) {
      syntaxError("expected dictionary or list type, got " + src, filename,
          ra.src);
    }

    checkSubtype(edt.key, idx, ra.index);

    return edt.value;
  }

  protected Type resolve(TupleGen rg, Environment environment) {
    HashMap<String, Type> types = new HashMap<String, Type>();
    // FIXME: add proper support for tuple types.
    int idx = 0;
    for (Expr e : rg.fields) {
      Type t = resolve(e, environment);
      types.put("$" + idx++, t);
    }
    return Type.T_RECORD(types);
  }

  protected Type resolve(UnresolvedType t) {
    if (t instanceof UnresolvedType.Any) {
      return Type.T_ANY;
    } else if (t instanceof UnresolvedType.Existential) {
      return Type.T_EXISTENTIAL;
    } else if (t instanceof UnresolvedType.Void) {
      return Type.T_VOID;
    } else if (t instanceof UnresolvedType.Null) {
      return Type.T_NULL;
    } else if (t instanceof UnresolvedType.Bool) {
      return Type.T_BOOL;
    } else if (t instanceof UnresolvedType.Int) {
      return Type.T_INT;
    } else if (t instanceof UnresolvedType.Char) {
      return Type.T_CHAR;
    } else if (t instanceof UnresolvedType.Real) {
      return Type.T_REAL;
    } else if (t instanceof UnresolvedType.List) {
      UnresolvedType.List lt = (UnresolvedType.List) t;
      return Type.T_LIST(resolve(lt.element));
    } else if (t instanceof UnresolvedType.Set) {
      UnresolvedType.Set st = (UnresolvedType.Set) t;
      return Type.T_SET(resolve(st.element));
    } else if (t instanceof UnresolvedType.Dictionary) {
      UnresolvedType.Dictionary st = (UnresolvedType.Dictionary) t;
      return Type.T_DICTIONARY(resolve(st.key), resolve(st.value));
    } else if (t instanceof UnresolvedType.Tuple) {
      // At the moment, a tuple is compiled down to a wyil record.
      UnresolvedType.Tuple tt = (UnresolvedType.Tuple) t;
      Environment types = new Environment();
      int idx = 0;
      for (UnresolvedType e : tt.types) {
        String name = "$" + idx++;
        types.put(name, resolve(e));
      }
      return Type.T_RECORD(types);
    } else if (t instanceof UnresolvedType.Record) {
      UnresolvedType.Record tt = (UnresolvedType.Record) t;
      HashMap<String, Type> types = new HashMap<String, Type>();
      for (Map.Entry<String, UnresolvedType> e : tt.types.entrySet()) {
        types.put(e.getKey(), resolve(e.getValue()));
      }
      return Type.T_RECORD(types);
    } else if (t instanceof UnresolvedType.Named) {
      UnresolvedType.Named dt = (UnresolvedType.Named) t;       
      ModuleID mid = dt.attribute(Attribute.Module.class).module;            
      if (modules.contains(mid)) {
        Type n_t = types.get(new NameID(mid, dt.name));
        if(n_t != null) {
        	return n_t;
        } 
      }
    } else if (t instanceof UnresolvedType.Union) {
      UnresolvedType.Union ut = (UnresolvedType.Union) t;
      HashSet<Type.NonUnion> bounds = new HashSet<Type.NonUnion>();
      for (UnresolvedType b : ut.bounds) {
        Type bt = resolve(b);
        if (bt instanceof Type.NonUnion) {
          bounds.add((Type.NonUnion) bt);
        } else {
          bounds.addAll(((Type.Union) bt).bounds);
        }
      }

      // Type type;
      if (bounds.size() == 1) {
        return bounds.iterator().next();
      } else {
        return Type.leastUpperBound(bounds);
      }
    }
       
    syntaxError("unknown type encountered: " + t, filename, t);
    return null;
  }

  /**
   * Check whether t1 :> t2; that is, whether t2 is a subtype of t1.
   * 
   * @param t1
   * @param t2
   * @param elem
   */
  public void checkSubtype(Type t1, Type t2, SyntacticElement elem) {

    // FIXME: the following special case is used because Type.isSubtype does
    // not consider a set to be a subtype of a list. In the source language,
    // however, this does make sense --- but it requires us to insert
    // coercions.

    if (Type.isSubtype(Type.T_SET(Type.T_ANY), t1)
        && Type.isSubtype(Type.T_LIST(Type.T_ANY), t2)) {
      // FIXME: following is broken because of named types.
      Type.Set ts1 = (Type.Set) t1;
      Type.List ts2 = (Type.List) t2;
      t1 = ts1.element;
      t2 = ts2.element;
    }

    if (!Type.isSubtype(t1, t2)) {
      syntaxError("expecting type " + t1 + ", got type " + t2, filename, elem);
    }
  }

  public static Environment join(Environment e1, Environment e2) {
    if (e1 == null) {
      return e2;
    }
    if (e2 == null) {
      return e1;
    }

    Environment r = new Environment();
    for (Map.Entry<String, Type> k1 : e1.entrySet()) {
      String name = k1.getKey();
      Type t1 = k1.getValue();
      Type t2 = e2.get(name);
      if (t2 != null) {
        r.put(name, Type.leastUpperBound(t1, t2));
      }
    }

    return r;
  }

  /**
   * A TypeAttr provides a way of attaching a type to a syntacticElement
   * 
   * @author djp
   * 
   */
  public static class TypeAttr implements Attribute {

    public final Type type;

    public TypeAttr(Type t) {
      this.type = t;
    }
  }

  @SuppressWarnings("serial")
  public static class Environment extends HashMap<String, Type> {

    public Environment() {
      super();
    }

    public Environment(Map<String, Type> init) {
      super(init);
    }
  }
}
