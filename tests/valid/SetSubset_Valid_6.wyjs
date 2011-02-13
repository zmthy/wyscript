[char] f({int} xs, {int} ys):
    if xs ⊂ ys:
        return "XS IS A SUBSET"
    else:
        return "FAILED"

[char] g({int} xs, {int} ys):
    return f(xs,ys)

void main([[char]] args):
    println(g({1,2,3},{1,2,3}))
    println(g({1,2},{1,2,3}))
    println(g({1},{1,2,3}))
