define wmccf7tup as {int x, int y}

wmccf7tup f(System x, int x):
    return {x:1,y:x->get()}

int get():
    return 1

void main([string] args):
    print str(f(this,1))
