define imsg as int|{string msg}

string getMessage(imsg m):
    if m ~= {string msg}:
        return m.msg
    else:
        return str(m)

void main([string] args):
    println(getMessage({msg:"HELLO WORLD"}))
    println(getMessage(1))
