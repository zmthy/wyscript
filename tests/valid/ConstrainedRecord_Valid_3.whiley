define btup as {byte op, byte index}

[byte] f(btup b):        
    return [b.op,b.index]

void main([string] args):
    out->println(str(f({op:1,index:2})))
