define ilist as real | [int]

string f(real e):
    if e ~= int:
        return "int"
    else:
        return "realreal"

void main([string] args):
    println(f(1))
