[char] f({int} xs, {int} ys, {int} zs):
    if zs == xs ∪ ys:
        return str(xs)
    else:
        return "FAILED"

[char] g({int} ys):
    return f(ys,ys,ys)

[char] h({int} ys, {int} zs):
    return f(ys,zs,ys ∪ zs)

void main([[char]] args):
    println(g({}))
    println(g({2}))
    println(g({1,2,3}))
    println(h({},{}))
    println(h({1},{2}))
    println(h({1,2,3},{3,4,5}))
