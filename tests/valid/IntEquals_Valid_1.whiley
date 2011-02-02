string f(int x, real y):
    if x == y:
        return "EQUAL"
    else:
        return "NOT EQUAL"

void main([string] args):
    println(f(1,4.0))
    println(f(1,4.2))
    println(f(0,0))
