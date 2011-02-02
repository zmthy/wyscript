define codeOp as { 1, 2, 3, 4 }
define code as {codeOp op, [int] payload}

string f(codeOp x):
    y = {op:x,payload:[]}
    return str(y)

void main([string] args):
    println(f(1))
