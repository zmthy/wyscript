void main([[char]] args):
    x = {f1:2,f2:3}
    y = {f1:1,f2:3}
    println(str(x))
    println(str(y)   )
    assert x != y
    x.f1 = 1
    println(str(x))
    println(str(y)  )
    assert x == y
