int f(int x):
    if(x < 10):
        return 1
    else if(x > 10):
        return 2
    return 0

void main([string] args):
    println(str(f(1)))
    println(str(f(10)))
    println(str(f(11)))
    println(str(f(1212)))
    println(str(f(-1212)))
