[char] f(int|null x):
    if x ~= null:
        return "GOT NULL"
    else:
        return "GOT INT"

void main([[char]] args):
    x = null
    println(f(x))
    println(f(1))
