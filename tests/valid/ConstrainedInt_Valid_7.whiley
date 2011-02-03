// this is a comment!
define nat as int
define num as {1,2,3,4}

string f(num x):
    y = x
    return str(y)

string g(int x, nat z):
    return f(z)

void main([string] args):
    println(g(1,3))
