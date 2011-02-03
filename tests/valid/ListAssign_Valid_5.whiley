[int] f():
    return [1,2]

void main([[char]] args):
     a1 = f()
     a2 = f()
     a2[0] = 0
     
     println(str(a1[0]))
     println(str(a1[1]))
     println(str(a2[0]))
     println(str(a2[1]))
