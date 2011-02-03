string f({int} xs):
    return str(xs)

string g({int} ys):
    return f(ys ∩ {1,2,3})

void main([string] args):
    println(g({1,2,3,4}))
    println(g({2}))
    println(g({}))
