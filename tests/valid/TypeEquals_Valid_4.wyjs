define expr as {int}|bool

{int} g({int} input):
    return input + {-1}

[char] f(expr e):
    if e ~= {int}:
        t = g(e)
        return "GOT: " + str(t)
    else:
        return "GOT SOMETHING ELSE?"

void main([[char]] args):
    e = {1,2,3,4}
    println(f(e))
 
