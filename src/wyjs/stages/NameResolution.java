// This file is part of the Whiley-to-Java Compiler (wyjc).
//
// The Whiley-to-Java Compiler is free software; you can redistribute 
// it and/or modify it under the terms of the GNU General Public 
// License as published by the Free Software Foundation; either 
// version 3 of the License, or (at your option) any later version.
//
// The Whiley-to-Java Compiler is distributed in the hope that it 
// will be useful, but WITHOUT ANY WARRANTY; without even the 
// implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR 
// PURPOSE.  See the GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public 
// License along with the Whiley-to-Java Compiler. If not, see 
// <http://www.gnu.org/licenses/>
//
// Copyright 2010, David James Pearce. 

package wyjs.stages;

import java.util.*;

import static wyjs.util.SyntaxError.*;
import wyjs.util.*;
import wyjs.lang.*;
import wyjs.lang.WhileyFile.*;
import wyjs.lang.Stmt;
import wyjs.lang.Stmt.*;
import wyjs.lang.Expr.*;

public class NameResolution {	
	private WhileyFile srcfile;
	
	public void resolve(List<WhileyFile> wyfiles) {
		for(WhileyFile wf : wyfiles) {
			resolve(wf);
		}
	}
	
	public void resolve(WhileyFile wf) {
		ArrayList<PkgID> imports = new ArrayList<PkgID>();
		
		srcfile = wf;		
		
		imports.add(srcfile.module.pkg().append(srcfile.module.module()));
		imports.add(srcfile.module.pkg().append("*"));
		imports.add(new PkgID(new String[]{"whiley","lang"}).append("*"));
						
		for(Decl d : wf.declarations) {			
			try {
				if(d instanceof ImportDecl) {
					ImportDecl impd = (ImportDecl) d;
					imports.add(0,new PkgID(impd.pkg));
				} else if(d instanceof FunDecl) {
					resolve((FunDecl)d,imports);
				} else if(d instanceof TypeDecl) {
					resolve((TypeDecl)d,imports);					
				} else if(d instanceof ConstDecl) {
					resolve((ConstDecl)d,imports);					
				}
			} catch(ResolveError ex) {
				syntaxError(ex.getMessage(),srcfile.filename,d);
			}
		}				
	}
	
	protected void resolve(ConstDecl td, ArrayList<PkgID> imports) {
		resolve(td.constant,new HashSet<String>(), imports);		
	}
	
	protected void resolve(TypeDecl td, ArrayList<PkgID> imports) throws ResolveError {
		try {
			resolve(td.type, imports);				
		} catch (ResolveError e) {												
			// Ok, we've hit a resolution error.
			syntaxError(e.getMessage(), srcfile.filename,  td);			
		}
	}	
	
	protected void resolve(FunDecl fd, ArrayList<PkgID> imports) {
		HashSet<String> environment = new HashSet<String>();
		
		// method parameter types
		for (WhileyFile.Parameter p : fd.parameters) {
			try {
				resolve(p.type, imports);
				environment.add(p.name());
			} catch (ResolveError e) {												
				// Ok, we've hit a resolution error.
				syntaxError(e.getMessage(), srcfile.filename, p, e);
			}
		}
		
		// method return type
		try {
			resolve(fd.ret, imports);
		} catch (ResolveError e) {
			// Ok, we've hit a resolution error.
			syntaxError(e.getMessage(), srcfile.filename, fd.ret);
		}
		
		List<Stmt> stmts = fd.statements;
		for (int i=0;i!=stmts.size();++i) {
			resolve(stmts.get(i), environment, imports);							
		}
	}
	
	public void resolve(Stmt s, HashSet<String> environment, ArrayList<PkgID> imports) {
		try {
			if(s instanceof Assign) {
				resolve((Assign)s, environment, imports);
			} else if(s instanceof Assert) {
				resolve((Assert)s, environment, imports);
			} else if(s instanceof Return) {
				resolve((Return)s, environment, imports);
			} else if(s instanceof Debug) {
				resolve((Debug)s, environment, imports);
			} else if(s instanceof Skip) {
				// do nothing
			} else if(s instanceof IfElse) {
				resolve((IfElse)s, environment, imports);
			} else if(s instanceof While) {
				resolve((While)s, environment, imports);
			} else if(s instanceof For) {
				resolve((For)s, environment, imports);
			} else if(s instanceof Invoke) {
				resolve((Invoke)s, environment, imports);
			} else {
				syntaxError("unknown statement encountered: "
						+ s.getClass().getName(), srcfile.filename, s);				
			}
		} catch (ResolveError e) {
			// Ok, we've hit a resolution error.
			syntaxError(e.getMessage(), srcfile.filename, s);			
		}
	}	

	protected void resolve(Assign s, HashSet<String> environment,
			ArrayList<PkgID> imports) {
		if(s.lhs instanceof Variable) {
			Variable v = (Variable) s.lhs;
			environment.add(v.var);
		} else if(s.lhs instanceof TupleGen) {
			TupleGen tg = (TupleGen) s.lhs;
			for(Expr e : tg.fields) {
				if(e instanceof Variable) {
					Variable v = (Variable) e;
					environment.add(v.var);
				} else {
					syntaxError("variable expected",srcfile.filename,e);
				}
			}
		} else {
			resolve(s.lhs, environment, imports);
		}
		resolve(s.rhs, environment, imports);	
	}

	protected void resolve(Assert s, HashSet<String> environment,
			ArrayList<PkgID> imports) {
		resolve(s.expr, environment, imports);		
	}

	protected void resolve(Return s, HashSet<String> environment,
			ArrayList<PkgID> imports) {
		if(s.expr != null) {
			resolve(s.expr, environment, imports);
		}
	}
	
	protected void resolve(Debug s, HashSet<String> environment,
			ArrayList<PkgID> imports) {
		resolve(s.expr, environment, imports);		
	}

	protected void resolve(IfElse s, HashSet<String> environment,
			ArrayList<PkgID> imports) {
		resolve(s.condition, environment, imports);
		HashSet<String> tenv = new HashSet<String>(environment);
		for (Stmt st : s.trueBranch) {
			resolve(st, tenv, imports);
		}
		if (s.falseBranch != null) {
			HashSet<String> fenv = new HashSet<String>(environment);
			for (Stmt st : s.falseBranch) {
				resolve(st, environment, imports);
			}
			
			for (String p : tenv) {
				if (fenv.contains(p)) {
					environment.add(p);
				}
			}
		}
	}
	
	protected void resolve(While s, HashSet<String> environment,
			ArrayList<PkgID> imports) {
		resolve(s.condition, environment, imports);		
		environment = new HashSet<String>(environment);
		for (Stmt st : s.body) {
			resolve(st, environment, imports);
		}
	}
	
	protected void resolve(For s, HashSet<String> environment,
			ArrayList<PkgID> imports) {
		resolve(s.source, environment, imports);		
		
		if (environment.contains(s.variable)) {
			syntaxError("variable " + s.variable + " is alreaded defined",
					srcfile.filename, s);
		}
		environment = new HashSet<String>(environment);
		environment.add(s.variable);
		for (Stmt st : s.body) {
			resolve(st, environment, imports);
		}
	}
	protected void resolve(Expr e, HashSet<String> environment, ArrayList<PkgID> imports) {
		try {
			if (e instanceof Constant) {
				
			} else if (e instanceof Variable) {
				resolve((Variable)e, environment, imports);
			} else if (e instanceof NaryOp) {
				resolve((NaryOp)e, environment, imports);
			} else if (e instanceof Comprehension) {
				resolve((Comprehension) e, environment, imports);
			} else if (e instanceof BinOp) {
				resolve((BinOp)e, environment, imports);
			} else if (e instanceof UnOp) {
				resolve((UnOp)e, environment, imports);
			} else if (e instanceof Invoke) {
				resolve((Invoke)e, environment, imports);
			} else if (e instanceof Comprehension) {
				resolve((Comprehension) e, environment, imports);
			} else if (e instanceof RecordAccess) {
				resolve((RecordAccess) e, environment, imports);
			} else if (e instanceof RecordGen) {
				resolve((RecordGen) e, environment, imports);
			} else if (e instanceof TupleGen) {
				resolve((TupleGen) e, environment, imports);
			} else if (e instanceof DictionaryGen) {
				resolve((DictionaryGen) e, environment, imports);
			} else if (e instanceof Access) {
				resolve((Access) e, environment, imports);
			} else if(e instanceof TypeConst) {
				resolve((TypeConst) e, environment, imports);
			} else {				
				syntaxError("unknown expression encountered: "
							+ e.getClass().getName(), srcfile.filename, e);								
			}
		} catch(ResolveError re) {
			syntaxError(re.getMessage(),srcfile.filename,e,re);			
		} catch(SyntaxError se) {
			throw se;
		} catch(Exception ex) {
			syntaxError("internal failure", srcfile.filename, e, ex);			
		}	
	}
	
	protected void resolve(Invoke ivk, HashSet<String> environment,
			ArrayList<PkgID> imports) throws ResolveError {
					
		for(Expr e : ivk.arguments) {						
			resolve(e, environment, imports);
		}
		
		// FIXME: needed for proper namespacing
		//ModuleID mid = loader.resolve(ivk.name,imports);		
		ModuleID mid = srcfile.module;
		Expr target = ivk.receiver;
		
		if(target != null) {
			resolve(target,environment,imports);
		}
		
		// Ok, resolve the module for this invoke
		ivk.attributes().add(new Attribute.Module(mid));		
	}
	
	protected void resolve(Variable v, HashSet<String> environment,
			ArrayList<PkgID> imports) throws ResolveError {		
		
		if (!environment.contains(v.var)) {
			// This variable access may correspond with a constant definition
			// in some module. Therefore, we must determine which module this
			// is, and then store that information for future use.
									
			// FIXME: needed for proper namespacing
			//ModuleID mid = loader.resolve(v.var, imports);
			//v.attributes().add(new Attributes.Module(mid));
			
			for(Decl d : srcfile.declarations) {
				if(d instanceof ConstDecl) {
					ConstDecl cd = (ConstDecl) d;
					if(cd.name().equals(v.var)) {
						// The following indicates that this is a constant
						v.attributes().add(new Attribute.Module(srcfile.module));
						break;
					}
				}
			}
		} 
	}
	
	protected void resolve(UnOp v, HashSet<String> environment,
			ArrayList<PkgID> imports) throws ResolveError {
		resolve(v.mhs, environment, imports);		
	}
	
	protected void resolve(BinOp v, HashSet<String> environment, ArrayList<PkgID> imports) {
		resolve(v.lhs, environment, imports);
		resolve(v.rhs, environment, imports);		
	}
	
	protected void resolve(Access v, HashSet<String> environment,
			ArrayList<PkgID> imports) {
		resolve(v.src, environment, imports);
		resolve(v.index, environment, imports);
	}
	
	protected void resolve(NaryOp v, HashSet<String> environment,
			ArrayList<PkgID> imports) throws ResolveError {				
		for(Expr e : v.arguments) {
			resolve(e, environment, imports);
		}		
	}
	
	protected void resolve(Comprehension e, HashSet<String> environment, ArrayList<PkgID> imports) throws ResolveError {						
		HashSet<String> nenv = new HashSet<String>(environment);
		for(Pair<String,Expr> me : e.sources) {														
			resolve(me.second(),nenv,imports); 			
			nenv.add(me.first());
		}		
		if(e.value != null) {			
			resolve(e.value,nenv,imports);
		}
		if(e.condition != null) {
			resolve(e.condition,nenv,imports);
		}	
	}	
		
	protected void resolve(RecordGen sg, HashSet<String> environment,
			ArrayList<PkgID> imports) throws ResolveError {		
		for(Map.Entry<String,Expr> e : sg.fields.entrySet()) {
			resolve(e.getValue(),environment,imports);
		}			
	}

	protected void resolve(TupleGen sg, HashSet<String> environment,
			ArrayList<PkgID> imports) throws ResolveError {		
		for(Expr e : sg.fields) {
			resolve(e,environment,imports);
		}			
	}
	
	protected void resolve(DictionaryGen sg, HashSet<String> environment,
			ArrayList<PkgID> imports) throws ResolveError {		
		for(Pair<Expr,Expr> e : sg.pairs) {
			resolve(e.first(),environment,imports);
			resolve(e.second(),environment,imports);
		}			
	}
	
	protected void resolve(TypeConst tc, HashSet<String> environment,
			ArrayList<PkgID> imports) throws ResolveError {		
		resolve(tc.type,imports);			
	}
	
	
	protected void resolve(RecordAccess sg, HashSet<String> environment, ArrayList<PkgID> imports) throws ResolveError {
		resolve(sg.lhs,environment,imports);			
	}
	
	protected void resolve(UnresolvedType t, ArrayList<PkgID> imports) throws ResolveError {
		if(t instanceof UnresolvedType.List) {
			UnresolvedType.List lt = (UnresolvedType.List) t;
			resolve(lt.element,imports);
		} else if(t instanceof UnresolvedType.Set) {
			UnresolvedType.Set st = (UnresolvedType.Set) t;
			resolve(st.element,imports);
		} else if(t instanceof UnresolvedType.Dictionary) {
			UnresolvedType.Dictionary st = (UnresolvedType.Dictionary) t;
			resolve(st.key,imports);
			resolve(st.value,imports);
		} else if(t instanceof UnresolvedType.Record) {
			UnresolvedType.Record tt = (UnresolvedType.Record) t;
			for(Map.Entry<String,UnresolvedType> e : tt.types.entrySet()) {
				resolve(e.getValue(),imports);
			}
		} else if(t instanceof UnresolvedType.Tuple) {
			UnresolvedType.Tuple tt = (UnresolvedType.Tuple) t;
			for(UnresolvedType e : tt.types) {
				resolve(e,imports);
			}
		} else if(t instanceof UnresolvedType.Named) {
			// This case corresponds to a user-defined type. This will be
			// defined in some module (possibly ours), and we need to identify
			// what module that is here, and save it for future use.
			UnresolvedType.Named dt = (UnresolvedType.Named) t;						
			// FIXME: needed for namespacing
			//ModuleID mid = loader.resolve(dt.name, imports);
			ModuleID mid = srcfile.module;
			t.attributes().add(new Attribute.Module(mid));
		} else if(t instanceof UnresolvedType.Union) {
			UnresolvedType.Union ut = (UnresolvedType.Union) t;
			for(UnresolvedType b : ut.bounds) {
				resolve(b,imports);
			}
		} 
	}
}
