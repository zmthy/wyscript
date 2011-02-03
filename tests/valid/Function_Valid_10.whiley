define msg as {int op, int s}

[char] f(msg m):
    return (str(m))

[char] f([int] ls):
    return (str(ls))

[char] f([real] ls):
    return (str(ls))

void main([[char]] args):
    println(f([1,2,3]))
    println(f([1.2,2.2,3.3]))
