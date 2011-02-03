string f(int x):
    return "F(INT)"

string f(real y):
    return "F(REAL)"

string f([int] xs):
    return "F([int])"

string f({int} xs):
    return "F({int})"


void main([string] args):
    println(f(1.0))
    println(f(1))
    println(f([1,2,3]))
    println(f({1,2,3}))
