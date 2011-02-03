define expr as [int]|int

[char] f(expr e):
    if e ~= [int]:
        return "GOT [INT]"
    else if e ~= int:
        return "GOT INT"
    else:
        return "GOT SOMETHING ELSE?"

void main([[char]] args):
    e = 1
    println(f(e))
    e = [1,2,3,4]
    println(f(e))
 
