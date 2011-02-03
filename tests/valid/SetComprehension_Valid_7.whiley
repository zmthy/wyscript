// this is a comment!
string f({int} xs):
    return str(xs)

void main([string] args):
    ys = {1,2,3}
    zs = {z | z in ys, z > 1}
    println(f(zs))
