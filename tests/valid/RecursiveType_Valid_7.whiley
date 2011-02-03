// A simple, recursive expression tree
define expr as {int num} | {int op, expr lhs, expr rhs} | {[char] err}

expr parseTerm():
    return parseIdentifier() 

expr parseIdentifier():
    return {err:"err"}

void main([[char]] args):
    e = parseTerm()
    println(str(e))
