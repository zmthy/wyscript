define Expr as real | { Expr lhs, int data } | [Expr]
define SubExpr as real | { SubExpr lhs, int data }

string toString(Expr e):
    if e ~= SubExpr:
        if e ~= real:
            return str(e)
        else:
            return str(e.data) + "->" + toString(e.lhs)
    else:
        return str(-1)

void main([string] args):
    se1 = 0.1234
    se2 = {lhs: se1, data: 1}
    se3 = {lhs: se2, data: 45}
    e1 = [se1]
    e2 = [e1,se1,se2]
    println(toString(se1))
    println(toString(se2))
    println(toString(se3))
    println(toString(e1))
    println(toString(e2))
