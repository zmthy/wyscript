int f({int} xs):
    return |xs|

void main([string] args):
    xs = {{1},{1,2,3}}
    zs = { {x:x,y:ys} | x∈xs,ys∈x }
    f(zs)
    print str(xs)
    print str(zs)
