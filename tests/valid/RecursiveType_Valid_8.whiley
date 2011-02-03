// The current parser state
define state as {[char] input, int pos}

// A simple, recursive expression tree
define expr as {int num} | {int op, expr lhs, expr rhs} | {[char] err}

// Top-level parse method
expr parse([char] input):
    r = parseAddSubExpr({input:input,pos:0})
    return r.e

{expr e, state st} parseAddSubExpr(state st):    
    return {e:{num:1},st:st}

void main([[char]] args):
    e = parse("Hello")
    println(str(e))
