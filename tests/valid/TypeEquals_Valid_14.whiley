define expr as {int op, expr lhs} | {[char] err}

int f(expr e):
    if e ~= {[char] err}:
        return |e.err|
    else:
        return -1
    
void main([[char]] args):
    x = f({err:"Hello World"})
    println(str(x))
    x = f({op:1,lhs:{err:"Gotcha"}})
    println(str(x))
