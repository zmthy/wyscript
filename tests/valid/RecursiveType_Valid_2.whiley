define binop as {int op, expr left, expr right}
define expr as int | binop

void main([string] args):
    e = 123
    println(str(e))
