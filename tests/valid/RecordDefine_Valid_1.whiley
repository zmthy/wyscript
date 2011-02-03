define point as {int x, int y}

point f(point x):
    return x

void main([[char]] args):
    p = f({x:1,y:1})
    println(str(p))
