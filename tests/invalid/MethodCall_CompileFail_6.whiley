define wmccf6tup as {int x, int y}

wmccf6tup f(System x, int y):
    return {x:1, y:x->get()}

int get():
    return 1

void main([string] args):
    print str(f(this,1))
