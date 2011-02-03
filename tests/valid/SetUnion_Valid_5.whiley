[char] f({int} xs):
    if |xs| > 0:
        return str(xs)
    else:
        return "FAILED"

[char] g({int} ys):
    return f(ys ∪ {1})

void main([[char]] args):
    println(g({}))
    println(g({2}))
