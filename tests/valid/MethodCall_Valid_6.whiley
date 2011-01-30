define wmcr6tup as {int x, int y}

wmcr6tup f(System x, int y):
    return {x:y,y:x->get()}

int get():
    return 1

void main([string] args):
    out->println(str(this->f(this,1)))
