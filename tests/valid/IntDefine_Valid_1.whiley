// this is a comment!
define ir1nat as int
define pir1nat as ir1nat

[char] f(int x):
    if x > 2:
        y = x
        return str(y)
    return ""

void main([[char]] args):
    println(f(1))
    println(f(2))
    println(f(3))
