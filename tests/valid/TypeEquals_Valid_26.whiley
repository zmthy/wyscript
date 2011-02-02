define rlist as real | [int]

int f(rlist l):
    if l ~= real:
        return 0
    else:
        return |l|

void main([string] args):
    println(str(f(123)))
    println(str(f(1.23)))
    println(str(f([1,2,3]))) 

