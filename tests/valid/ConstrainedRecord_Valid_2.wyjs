define RET as 169
define NOP as 0

define unitCode as { NOP, RET }
define UNIT as {unitCode op}

byte f(UNIT x):
    return x.op

void main([[char]] args):
    bytes = f({op:NOP})
    println(str(bytes))

