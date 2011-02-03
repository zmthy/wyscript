define IntList as {int op, [real] rest}|{int op, int mode}

[char] f(IntList y):
    return str(y)

[char] g({int op, int mode} z):
    return str(z)

void main([[char]] args):
    x = {op:1, rest:[1.23]}
    println(f(x))
    x = {op:1.23, mode: 0}
    x.op = 123 // OK
    println(g(x))
