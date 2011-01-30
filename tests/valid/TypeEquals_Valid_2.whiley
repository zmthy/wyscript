define expr as [int]|int

string f(expr e):
    if e ~= [int]:
        return "GOT [INT]"
    else if e ~= int:
        return "GOT INT"
    else:
        return "GOT SOMETHING ELSE?"

void main([string] args):
    e = 1
    out->println(f(e))
    e = [1,2,3,4]
    out->println(f(e))
 
