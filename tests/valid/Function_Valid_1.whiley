string f(real x):
    return "GOT REAL"

string f(int x):
    return "GOT INT"

void main([string] args):
    println(f(1))
    println(f(1.23))
