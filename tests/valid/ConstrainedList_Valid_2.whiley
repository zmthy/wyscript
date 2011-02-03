int g(int x):
    if(x <= 0 || x >= 125):
        return 1
    else:
        return x

[byte] f(int x):
    return [g(x)]

void main([[char]] args):
    bytes = f(0)
    println(str(bytes))

