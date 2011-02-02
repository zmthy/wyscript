int f(System x):
    return x->get()

int get():
    return 123

void main([string] args):
    println(str(this->f(this)))
