define imsg as int|{[char] msg}

[char] getMessage(imsg m):
    if m ~= {[char] msg}:
        return m.msg
    else:
        return str(m)

void main([[char]] args):
    println(getMessage({msg:"HELLO WORLD"}))
    println(getMessage(1))
