define ilist as int | [int]
define rlist as real | [int]

[char] f(rlist e):
    if e ~= int:
        return "int"
    else if e ~= [int]:
        return "[int]"
    else:
        return "real"

[char] g(ilist e):
    return f(e)


void main([[char]] args):
    println(f(1))
    println(f([1]))
    println(f([]))
