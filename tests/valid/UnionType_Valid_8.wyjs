define msg1 as {int op, [int] data}
define msg2 as {int op, [{int dum}] data}

define msgType as msg1 | msg2

[char] f(msgType m):
    return str(m)

void main([[char]] args):
    x = {op:1,data:[1,2,3]}
    println(f(x))
    list = x.data
    println(str(list))
    
