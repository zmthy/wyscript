define intlist as [int]

int f(intlist xs):
    a = 0
    i = 0
    while i < |xs|:
        a = a + xs[i]
        i = i + 1
    return a