define Rtypes as {int x, int y} | {int x, int z}

string f(Rtypes e):
    if e ~= {int x, int y}:
        return "GOT IT"
    else:
        return "NOPE"

void main([string] args):
    println(f({x: 1, y: 1}))
    println(f({x: 1, z: 1}))
