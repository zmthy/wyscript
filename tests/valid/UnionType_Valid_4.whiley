define ur4nat as int
define tur4nat as int
define wur4nat as ur4nat|tur4nat

[char] f(wur4nat x):
    return str(x)

void main([[char]] args):
    println(f(1))  

