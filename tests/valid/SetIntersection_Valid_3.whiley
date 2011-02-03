[char] f({int} xs):
    return str(xs)

[char] g({int} ys):
    return f(ys ∩ {1,2})

void main([[char]] args):
    println(g({}))
    println(g({2,3,4,5,6}))
    println(g({2,6}))
