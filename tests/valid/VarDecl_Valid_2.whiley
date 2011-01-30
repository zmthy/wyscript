string g(int z):
    return str(z)

string f(int x):
    y = x + 1
    return g(y)

void main([string] args):
    out->println(f(1))
