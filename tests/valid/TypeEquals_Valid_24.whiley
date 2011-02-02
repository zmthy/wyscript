define src as int|[int]|[[int]]

string f(src e):
    if e ~= [*]:
        return "[*]"
    else:
        return "int"

void main([string] args):
    println(f([1,2,3]))
    println(f([[1],[2]]))
    println(f(1))
