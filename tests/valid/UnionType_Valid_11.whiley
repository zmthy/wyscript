// this is a comment!
define IntList as int|[int]

void f(int y):
    out->println(str(y))

void g([int] z):
    out->println(str(z))

void main([string] args):
    x = 123
    this->f(x)
    x = [1,2,3]
    this->g(x)
