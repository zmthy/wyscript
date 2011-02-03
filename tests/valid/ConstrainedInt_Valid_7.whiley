// this is a comment!
define nat as int
define num as {1,2,3,4}

[char] f(num x):
    y = x
    return str(y)

[char] g(int x, nat z):
    return f(z)

void main([[char]] args):
    println(g(1,3))
