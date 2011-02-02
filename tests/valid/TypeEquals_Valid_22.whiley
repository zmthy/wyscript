define iset as {int} | int

string f(iset e):
    if e ~= {int}:
        return "{int}"
    else:
        return "int"

void main([string] args):
    println(f({1,2,3}))
    println(f(1))
