define utr12nat as int
define intList as utr12nat|[int]
define tupper as {int op, intList il}

int f(tupper y):
    return y.op

void main([[char]] args):
    x = {op:1,il:1}
    println(str(x))
    f(x)
