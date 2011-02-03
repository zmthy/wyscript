define iset as {int} | int

[char] f(iset e):
    if e ~= {int}:
        return "{int}"
    else:
        return "int"

void main([[char]] args):
    println(f({1,2,3}))
    println(f(1))
