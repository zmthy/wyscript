[int] f(System x, int x):
    return [1,2,3,x->get()]

int get():
    return 1

void main([string] args):
    print str(f(this,1))
