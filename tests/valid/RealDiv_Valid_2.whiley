real g(int x):
     return x / 3

[char] f(int x, int y):
    return str(g(x))

void main([[char]] args):
     println(f(1,2))

