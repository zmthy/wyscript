// this is a comment!
define IntList as int|[int]

void f(int y):
    println(str(y))

void g([int] z):
    println(str(z))

void main([[char]] args):
    x = 123
    this->f(x)
    x = [1,2,3]
    this->g(x)
