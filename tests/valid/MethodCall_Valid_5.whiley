[int] f(System x):
    return [1,2,3,x->get()]

int get():
    return 1

void main([string] args):
    println(str(this->f(this)))
