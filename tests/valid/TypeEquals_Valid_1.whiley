define bop as {int x, int y}
define expr as int|bop

[char] f(expr e):
    if e ~= int:
        return "GOT INT"
    else:
        return "GOT BOB"

void main([[char]] args):
    e = 1
    println(f(e))
    e = {x:1,y:2}
    println(f(e))
 
