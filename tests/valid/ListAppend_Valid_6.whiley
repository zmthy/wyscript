define plistv6 as [int]

int f(plistv6 xs):
    return |xs|

int g(plistv6 left, plistv6 right):
    return f(left + right)

void main([[char]] args):
    r = g([1,2,3],[6,7,8])
    println(str(r))
