define Expr as real | Var | BinOp
define BinOp as { Expr lhs, Expr rhs } 
define Var as { string id }

define SyntaxError as { string err }
define SExpr as SyntaxError | Expr

SExpr build(int i):    
    if i > 10:
        return { id: "var" }
    else if i > 0:
        return i
    else:
        return { lhs:sbuild(i+10), rhs:sbuild(i+1) } 

SExpr sbuild(int i):
    if i > 20:
        return { err: "error" }
    else:
        return build(i)

real evaluate(Expr e):
    if e ~= real:
        return e
    if e ~= {[int] id}:
        return |e.id|
    else:
        return evaluate(e.lhs) + evaluate(e.rhs)

// Main method
public void main([string] args):
    i = -5
    while i < 10:
        e = sbuild(i)
        if e ~= {[int] err}:
            println("syntax error: " + e.err)
        else:
            e = evaluate(e)
            println(str(e))
        i = i + 1
