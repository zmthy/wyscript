define expr as {int}|bool

[char] f(expr e):
    if e ~= {int}:
        return "GOT {INT}"
    else if e ~= bool:
        return "GOT BOOL"
    else:
        return "GOT SOMETHING ELSE?"

void main([[char]] args):
    e = true
    println(f(e))
    e = {1,2,3,4}
    println(f(e))
 
