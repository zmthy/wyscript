define tenup as int
define msg1 as {tenup op, [int] data}
define msg2 as {int index}

define msgType as msg1 | msg2

[char] f(msgType m):
    return str(m)

void main([[char]] args):
    x = {op:11,data:[]}
    println(f(x))
