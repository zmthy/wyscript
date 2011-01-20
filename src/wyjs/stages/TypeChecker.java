package wyjs.stages;

import java.util.*;
import java.math.BigInteger;

import static wyjs.util.SyntaxError.*;
import wyjs.util.*;
import wyjs.lang.*;
import wyjs.lang.WhileyFile.*;
import wyjs.lang.Stmt;
import wyjs.lang.Stmt.*;
import wyjs.lang.Expr.*;

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
	 * The following method visits every define constant statement in every
	 * whiley file being compiled, and determines its true and value.
	 * 
	 * @param files
	 */
	protected void generateConstants(List<WhileyFile> files) {
		HashMap<NameID, Expr> exprs = new HashMap();

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
				 * FIXME: this needs to be updated at some point
				Type t = v.type();
				if (t instanceof Type.Set) {
					Type.Set st = (Type.Set) t;					
					types.put(k, st.element);
				}
				*/
			} catch (ResolveError rex) {
				syntaxError(rex.getMessage(), filemap.get(k).filename, exprs
						.get(k), rex);
			}
		}
	}

	/**
	 * The expand constant method is responsible for turning a named constant
	 * expression into a value. This is done by traversing the constant's
	 * expression and recursively expanding any named constants it contains.
	 * Simplification of constants is also performed where possible.
	 * 
	 * @param key
	 *            --- name of constant we are expanding.
	 * @param exprs
	 *            --- mapping of all names to their( declared) expressions
	 * @param visited
	 *            --- set of all constants seen during this traversal (used to
	 *            detect cycles).
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
			syntaxError("cyclic constant definition encountered", filemap
					.get(key).filename, exprs.get(key));
		} else {
			visited.add(key); // mark this node as visited
		}

		// At this point, we need to replace every unresolved variable with a
		// constant definition.
		Expr v = expandConstantHelper(e, filemap.get(key).filename, exprs,
				visited);
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
	 * @param key
	 *            --- name of constant we are expanding.
	 * @param exprs
	 *            --- mapping of all names to their( declared) expressions
	 * @param visited
	 *            --- set of all constants seen during this traversal (used to
	 *            detect cycles).
	 */
	protected Expr expandConstantHelper(Expr expr, String filename,
			HashMap<NameID, Expr> exprs, HashSet<NameID> visited)
			throws ResolveError {
		if (expr instanceof Constant) {
			Constant c = (Constant) expr;
			return c;
		} else if (expr instanceof Variable) {
			// Note, this must be a constant definition of some sort
			Variable v = (Variable) expr;
			// FIXME: for when we put namespacing back in.
			NameID name = new NameID(new ModuleID(new PkgID(""), filename),
					v.var);
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
		HashMap<NameID, SyntacticElement> srcs = new HashMap();
		
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
				syntaxError(ex.getMessage(), filemap.get(key).filename, srcs
						.get(key), ex);
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
	 *         constrained type. The latter is useful since it means we can
	 *         throw away unnecessary constraint blocks when the type in
	 *         question is not actually constrained.
	 * @throws ResolveError
	 */
	protected Type expandType(NameID key,
			HashMap<NameID, Type> cache) throws ResolveError {
		
		Type cached = cache.get(key);
		Type t = types.get(key);
		
		if (cached != null) {			
			return cached;
		} else if(t != null) {
			return t;
		} 

		// following is needed to terminate any recursion
		cache.put(key, Type.T_RECURSIVE(key, null));

		// now, expand the type fully		
		t = expandType(unresolved.get(key), filemap.get(key).filename,
				cache);

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
			for(int i=0;i!=ut.bounds.size();++i) {
				UnresolvedType b = ut.bounds.get(i);
				
				Type bt = expandType(b, filename, cache);				
				if (bt instanceof Type.NonUnion) {
					bounds.add((Type.NonUnion) bt);
				} else {
					bounds.addAll(((Type.Union) bt).bounds);
				}				
			}
			
			Type type;
			if (bounds.size() == 1) {
				return bounds.iterator().next();
			} else {				
				return Type.leastUpperBound(bounds);
			}			
		} else if (t instanceof UnresolvedType.Named) {
			UnresolvedType.Named dt = (UnresolvedType.Named) t;
			// FIXME: for when we put namespacing back in
			ModuleID module = new ModuleID(new PkgID(""),"");
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

		// method receiver type (if applicable)
		Type.ProcessName rec = null;
		
		Type.Fun ft = Type.T_FUN(rec, ret, parameters);
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
					resolve((FunDecl) d);													
				}
			} catch (SyntaxError se) {
				throw se;
			} catch (Throwable ex) {
				syntaxError("internal failure", wf.filename, d, ex);
			}
		}					
	}
	
	protected void resolve(FunDecl fd) {				
		HashMap<String,Type> environment = new HashMap<String,Type>();
		currentFunDecl = fd;
		
		// First, initialise typing environment		
		Type.Fun tf = (Type.Fun) fd.attribute(TypeAttr.class).type;
		
		for (int i=0;i!=fd.parameters.size();++i) {
			String name = fd.parameters.get(i).name();
			Type type = tf.params.get(i);
			environment.put(name,type);			
		}

		
		// Second, propagate types through all expressions 				
		for (Stmt s : fd.statements) {
			resolve(s, environment);
		}

		currentFunDecl = null;		
	}
	
	public void resolve(Stmt stmt, HashMap<String,Type> environment) {
		try {
			if (stmt instanceof Assign) {
				resolve((Assign) stmt, environment);
			} else if (stmt instanceof Assert) {
				resolve((Assert) stmt, environment);
			} else if (stmt instanceof Return) {
				resolve((Return) stmt, environment);
			} else if (stmt instanceof Debug) {
				resolve((Debug) stmt, environment);
			} else if (stmt instanceof IfElse) {
				resolve((IfElse) stmt, environment);
			} else if (stmt instanceof While) {
				resolve((While) stmt, environment);
			} else if (stmt instanceof For) {
				resolve((For) stmt, environment);
			} else if (stmt instanceof Skip) {
				resolve((Skip) stmt, environment);
			} else {
				syntaxError("unknown statement encountered: "
						+ stmt.getClass().getName(), filename, stmt);
			}
		} catch (SyntaxError sex) {
			throw sex;
		} catch (Exception ex) {			
			syntaxError("internal failure", filename, stmt, ex);
		}		
	}
	
	protected void resolve(Assign s, HashMap<String,Type> environment) {
		
		Type rhs_t = resolve(s.rhs,environment);
		
		if(s.lhs instanceof Variable) {
			Variable v = (Variable) s.lhs;
			environment.put(v.var, rhs_t);
		} else {
			Type lhs_t = resolve(s.lhs,environment);
			checkSubtype(lhs_t,rhs_t,s);
		}
	}

	protected void resolve(Assert s, HashMap<String,Type> environment) {
		Type t = resolve(s.expr,environment);
		checkSubtype(Type.T_BOOL,t,s);
	}

	protected void resolve(Return s, HashMap<String,Type> environment) {

		if (s.expr != null) {
			Type t = resolve(s.expr,environment);
			TypeAttr ta = currentFunDecl.attribute(TypeAttr.class);
			Type.Fun ft = (Type.Fun) ta.type;
			checkSubtype(ft.ret,t,s); 			
		} 
	}

	protected void resolve(Skip s, HashMap<String,Type> environment) {
		// TODO: remove skip statement?
	}

	protected void resolve(Debug s, HashMap<String,Type> environment) {
		resolve(s.expr, environment);
		// TO DO ... check type is a string?
	}

	protected Type resolve(Expr e, HashMap<String,Type> environment) {
		try {
			if (e instanceof Constant) {
				return resolve((Constant) e, environment);
			} else if (e instanceof Variable) {
				return resolve((Variable) e, environment);
			} else if (e instanceof BinOp) {
				return resolve((BinOp) e, environment);
			}else {
				syntaxError("unknown expression encountered: "
						+ e.getClass().getName(), filename, e);
			}
		} catch (SyntaxError se) {
			throw se;
		} catch (Exception ex) {
			syntaxError("internal failure", filename, e, ex);
		}
		return null;
	}

	protected Type resolve(Constant c, HashMap<String,Type> environment) {
		Object v = c.value;
		if(v instanceof Boolean) {
			return Type.T_BOOL;
		} else if(v instanceof Integer) {
			return Type.T_INT;
		} else if(v instanceof Double) {
			return Type.T_REAL;
		} 
		syntaxError("unknown constant encountered: "
				+ c.getClass().getName(), filename, c);
		return null;
	}

	protected Type resolve(Variable v, HashMap<String,Type> environment) throws ResolveError {
		Type v_t = environment.get(v.var);
		if(v_t != null) {
			return v_t;
		}
		syntaxError("variable not defined", filename, v);
		return null;
	}
	
	protected Type resolve(BinOp bop, HashMap<String,Type> environment) throws ResolveError {
		Type lhs_t = resolve(bop.lhs,environment);
		Type rhs_t = resolve(bop.rhs,environment);
		Type lub = Type.leastUpperBound(lhs_t,rhs_t);
		
		// FIXME: really need to add conversions somehow
		
		switch(bop.op) {
			case ADD:
			case SUB:
			case DIV:
			case MUL:		
			{
				Type tmp = Type.leastUpperBound(Type.T_REAL,lub);
				checkSubtype(tmp,Type.T_REAL,bop);
				return lub;
			}
		}
				
		syntaxError("unknown binary expression encountered: "
				+ bop.getClass().getName(), filename, bop);
		return null;
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
			return Type.T_DICTIONARY(resolve(st.key),resolve(st.value));					
		} else if (t instanceof UnresolvedType.Tuple) {
			// At the moment, a tuple is compiled down to a wyil record.
			UnresolvedType.Tuple tt = (UnresolvedType.Tuple) t;
			HashMap<String,Type> types = new HashMap<String,Type>();			
			int idx=0;
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
			// FIXME: for when we put namespacing back in
			ModuleID mid = new ModuleID(new PkgID(""),filename);
			if (modules.contains(mid)) {
				return types.get(new NameID(mid, dt.name));								
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

			Type type;
			if (bounds.size() == 1) {
				return bounds.iterator().next();
			} else {
				return Type.leastUpperBound(bounds);
			}			
		} 
		
		syntaxError("unknown type encountered", filename, t);
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
		if (!Type.isSubtype(t1, t2)) {
			syntaxError("expecting type " + t1 + ", got type " + t2, filename,
					elem);
		}
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
}
