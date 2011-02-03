int f(int x, int y):
    a = x == y
    if(a):
        return 1
    else:
        return x + y

int g(int x, int y):
    a = x >= y
    if(!a):
        return x + y
    else:
        return 1


void main([[char]] args):
    println(str(f(1,1)))
    println(str(f(0,0)))
    println(str(f(4,345)))
    println(str(g(1,1)))
    println(str(g(0,0)))
    println(str(g(4,345)))
