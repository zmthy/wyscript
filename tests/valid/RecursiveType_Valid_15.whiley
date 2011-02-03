define Expr as real | Var | BinOp
define BinOp as { Expr lhs, Expr rhs } 
define Var as { [char] id }

define SyntaxError as { [char] err }
define SExpr as SyntaxError | Expr

Expr build(int i):    
    if i > 10:
        return { id: "var" }
    else if i > 0:
        return i
    else:
        return { lhs:build(i+10), rhs:build(i+1) } 

SExpr sbuild(int i):
    if i > 20:
        return { err: "error" }
    else:
        return build(i)

// Main method
public void main([[char]] args):
    i = -5
    while i < 10:
        e = sbuild(i)
        if e ~= SyntaxError:
            println("syntax error: " + e.err)
        else:
            println(str(e))
        i = i + 1
