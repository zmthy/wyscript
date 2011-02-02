define pos as int
define neg as int

define intlist as pos|neg|[int]

int f(intlist x):
    if x ~= int:
        return x
    return 1 

void main([string] args):
    x = f([1,2,3])
    println(str(x))
    x = f(123)
    println(str(x))

