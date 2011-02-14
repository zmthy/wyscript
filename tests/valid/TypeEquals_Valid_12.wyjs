define imsg as int|{int op}|{string msg}

string getMessage(imsg m):
    if m ~= {string msg}:
        return m.msg
    else if m ~= {int op}:
        return str(m.op)
    else:
        return str(m)

void main([string] args):
    println(getMessage({msg:"HELLO WORLD"}))
    println(getMessage(1))
    println(getMessage({op:123}))
