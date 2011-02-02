define pos as real
define neg as int
define expr as pos|neg|[int]

string f(expr e):
    if e ~= pos && e > 0:
        return "POSITIVE: " + str(e)
    else if e ~= neg:
        return "NEGATIVE: " + str(e)
    else:
        return "OTHER"

void main([string] args):
    println(f(-1))
    println(f(1))
    println(f(1234))
    println(f([1,2,3]))
 
