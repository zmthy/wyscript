define test as {int x} | {int y}
define src as test | int

[char] f(src e):
    if e ~= test:
        return "{int x} | {int y}"
    else:
        return "int"

void main([[char]] args):
    println(f({x: 1}))
    println(f({y: 2}))
    println(f(1))
