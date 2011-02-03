// this is a comment!
define IntReal as int|real

[char] f(IntReal y):
    return str(y)

void main([[char]] args):
    x = 123
    println(f(x))
    x = 1.234
    println(f(x))

