define expr as [int]|int
define tup as {expr lhs, int p}

[char] f(tup t):
    if t.lhs ~= [int] && |t.lhs| > 0 && t.lhs[0] == 0:
        return "MATCH" + str(t.lhs)
    else:
        return "NO MATCH"

void main([[char]] args):
    println(f({lhs:[0],p:0}))
    println(f({lhs:[1],p:0}))
    println(f({lhs:[],p:0}))
