real f(real x):
    return -x

void main([string] args):
    out->println(str(f(1.2)))
    out->println(str(f(0.00001)))
    out->println(str(f(5632)))
