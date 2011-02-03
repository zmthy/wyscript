{int->int} f(int x):
    return {1->x, 3->2}

int get(int i, {int->int} map):
    return map[i]

void main([[char]] args):
    println(str(get(1,f(1))))
    println(str(get(1,f(2))))
    println(str(get(1,f(3))))
    println(str(get(3,f(3))))
