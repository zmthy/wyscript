define imsg as int|{int op}|{[char] msg}

[char] getMessage(imsg m):
    if m ~= {[char] msg}:
        return m.msg
    else if m ~= {int op}:
        return str(m.op)
    else:
        return str(m)

void main([[char]] args):
    println(getMessage({msg:"HELLO WORLD"}))
    println(getMessage(1))
    println(getMessage({op:123}))
