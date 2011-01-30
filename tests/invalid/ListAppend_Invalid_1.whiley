int f([string] r):
    return |r|
 
void main([string] args):
    r = args + [1]
    f(r)
    print str(r)
