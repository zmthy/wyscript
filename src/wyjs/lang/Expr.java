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

package wyjs.lang;

import java.util.*;

import wyjs.util.*;

public interface Expr extends SyntacticElement {

  public interface LVal extends Expr {}

  public static class Variable extends SyntacticElement.Impl implements Expr,
      LVal {

    public String var;

    public Variable(String var, Attribute... attributes) {
      super(attributes);
      this.var = var;
    }

    public String toString() {
      return var;
    }
  }

  public static class NamedConstant extends Variable {

    public ModuleID mid;

    public NamedConstant(String var, ModuleID mid, Attribute... attributes) {
      super(var, attributes);
      this.mid = mid;
    }

    public String toString() {
      return mid + ":" + var;
    }
  }

  public static class Constant extends SyntacticElement.Impl implements Expr {

    public Object value;

    public Constant(Object val, Attribute... attributes) {
      super(attributes);
      this.value = val;
    }

    public String toString() {
      return value.toString();
    }
  }

  public static class TypeConst extends SyntacticElement.Impl implements Expr {

    public UnresolvedType type;

    public TypeConst(UnresolvedType val, Attribute... attributes) {
      super(attributes);
      this.type = val;
    }
  }

  public static class FunConst extends SyntacticElement.Impl implements Expr {

	    public String name;
	    public final List<UnresolvedType> paramTypes;

	    public FunConst(String name, List<UnresolvedType> paramTypes, Attribute... attributes) {
	      super(attributes);
	      this.name = name;
	      this.paramTypes = paramTypes;
	    }
	  }
  
  public static class BinOp extends SyntacticElement.Impl implements Expr {

    public BOp op;
    public Expr lhs;
    public Expr rhs;

    public BinOp(BOp op, Expr lhs, Expr rhs, Attribute... attributes) {
      super(attributes);
      this.op = op;
      this.lhs = lhs;
      this.rhs = rhs;
    }

    public BinOp(BOp op, Expr lhs, Expr rhs, Collection<Attribute> attributes) {
      super(attributes);
      this.op = op;
      this.lhs = lhs;
      this.rhs = rhs;
    }

    public String toString() {
      return "(" + op + " " + lhs + " " + rhs + ")";
    }
  }

  // A list access is very similar to a BinOp, except that it can be assigned.
  public static class Access extends SyntacticElement.Impl implements Expr,
      LVal {
	public LOp op;
    public Expr src;
    public Expr index;

    public Access(LOp op, Expr src, Expr index, Attribute... attributes) {
      super(attributes);
      this.op = op;
      this.src = src;
      this.index = index;
    }

    public Access(LOp op, Expr src, Expr index, Collection<Attribute> attributes) {
      super(attributes);
      this.op = op;
      this.src = src;
      this.index = index;
    }

    public String toString() {
      return src + "[" + index + "]";
    }
  }

  public enum LOp {
	    LISTACCESS,DICTIONARYACCESS,
  }
  
  public enum UOp {
    NOT, NEG, LENGTHOF,
  }

  public static class UnOp extends SyntacticElement.Impl implements Expr {

    public UOp op;
    public Expr mhs;

    public UnOp(UOp op, Expr mhs, Attribute... attributes) {
      super(attributes);
      this.op = op;
      this.mhs = mhs;
    }

    public String toString() {
      return op + mhs.toString();
    }
  }

  public static class NaryOp extends SyntacticElement.Impl implements Expr {

    public NOp op;
    public final ArrayList<Expr> arguments;

    public NaryOp(NOp nop, Collection<Expr> arguments, Attribute... attributes) {
      super(attributes);
      this.op = nop;
      this.arguments = new ArrayList<Expr>(arguments);
    }

    public NaryOp(NOp nop, Attribute attribute, Expr... arguments) {
      super(attribute);
      this.op = nop;
      this.arguments = new ArrayList<Expr>();
      for (Expr a : arguments) {
        this.arguments.add(a);
      }
    }
  }

  public enum NOp {
    SETGEN, LISTGEN, SUBLIST
  }

  public static class Comprehension extends SyntacticElement.Impl implements
      Expr {

    public COp cop;
    public Expr value;
    public final ArrayList<Pair<String, Expr>> sources;
    public Expr condition;

    public Comprehension(COp cop, Expr value,
        Collection<Pair<String, Expr>> sources, Expr condition,
        Attribute... attributes) {
      super(attributes);
      this.cop = cop;
      this.value = value;
      this.condition = condition;
      this.sources = new ArrayList<Pair<String, Expr>>(sources);
    }
  }

  public enum COp {
    SETCOMP, LISTCOMP, NONE, // implies value == null
    SOME, // implies value == null
  }

  public static class RecordAccess extends SyntacticElement.Impl implements
      LVal {

    public Expr lhs;
    public String name;

    public RecordAccess(Expr lhs, String name, Attribute... attributes) {
      super(attributes);
      this.lhs = lhs;
      this.name = name;
    }

    public String toString() {
      return lhs + "." + name;
    }
  }

  public static class DictionaryGen extends SyntacticElement.Impl implements
      Expr {

    public final ArrayList<Pair<Expr, Expr>> pairs;

    public DictionaryGen(Collection<Pair<Expr, Expr>> pairs,
        Attribute... attributes) {
      super(attributes);
      this.pairs = new ArrayList<Pair<Expr, Expr>>(pairs);
    }
  }

  public static class RecordGen extends SyntacticElement.Impl implements Expr {

    public final HashMap<String, Expr> fields;

    public RecordGen(Map<String, Expr> fields, Attribute... attributes) {
      super(attributes);
      this.fields = new HashMap<String, Expr>(fields);
    }
  }

  public static class TupleGen extends SyntacticElement.Impl implements LVal {

    public final ArrayList<Expr> fields;

    public TupleGen(Collection<Expr> fields, Attribute... attributes) {
      super(attributes);
      this.fields = new ArrayList<Expr>(fields);
    }
  }

  public static class Invoke extends SyntacticElement.Impl implements Expr,
      Stmt {

    public String name;
    public Expr receiver;
    public final ArrayList<Expr> arguments;
    public boolean indirect = false;

    public Invoke(String name, Expr receiver, List<Expr> arguments,
        Attribute... attributes) {
      super(attributes);
      this.name = name;
      this.receiver = receiver;
      this.arguments = new ArrayList<Expr>(arguments);
    }
  }

  public enum BOp {
    AND {

      public String toString() {
        return "&&";
      }
    },
    OR {

      public String toString() {
        return "||";
      }
    },
    ADD {

      public String toString() {
        return "+";
      }
    },
    SUB {

      public String toString() {
        return "-";
      }
    },
    MUL {

      public String toString() {
        return "*";
      }
    },
    DIV {

      public String toString() {
        return "/";
      }
    },
    UNION {

      public String toString() {
        return "+";
      }
    },
    INTERSECTION {

      public String toString() {
        return "&";
      }
    },
    EQ {

      public String toString() {
        return "==";
      }
    },
    NEQ {

      public String toString() {
        return "!=";
      }
    },
    LT {

      public String toString() {
        return "<";
      }
    },
    LTEQ {

      public String toString() {
        return "<=";
      }
    },
    GT {

      public String toString() {
        return ">";
      }
    },
    GTEQ {

      public String toString() {
        return ">=";
      }
    },
    SUBSET {

      public String toString() {
        return "<";
      }
    },
    SUBSETEQ {

      public String toString() {
        return "<=";
      }
    },
    ELEMENTOF {

      public String toString() {
        return "in";
      }
    },
    LISTRANGE {

      public String toString() {
        return "..";
      }
    },
    TYPEEQ {

      public String toString() {
        return "~==";
      }
    },
    TYPEIMPLIES {

      public String toString() {
        return "~=>";
      }
    }
  };
}
