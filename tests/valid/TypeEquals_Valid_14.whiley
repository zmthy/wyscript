define expr as {int op, expr lhs} | {string err}

int f(expr e):
    if e ~= {string err}:
        return |e.err|
    else:
        return -1
    
void main([string] args):
    x = f({err:"Hello World"})
    println(str(x))
    x = f({op:1,lhs:{err:"Gotcha"}})
    println(str(x))
