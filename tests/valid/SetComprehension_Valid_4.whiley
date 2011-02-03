{int} f([int] xs):
    return { x | x in xs, x > 1 }

void main([[char]] args):
    println(str(f([1,2,3])))
    println(str(f([1,2,3,3])))
    println(str(f([-1,1,2,-1,3,3])))
