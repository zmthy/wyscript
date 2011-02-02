define bop as {int x, int y}
define expr as int|bop

int f(expr e):
    if e ~= bop:
        return e.x + e.y
    else if e ~= int:
        return e // type difference
    else:
        return -1 // unreachable

void main([string] args):
    x = f(1)
    println(str(x))
    x = f({x:4,y:10})   
    println(str(x))
