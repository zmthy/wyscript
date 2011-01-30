{int->int} f(int x):
    return {1->x, 3->2}

int get(int i, {int->int} map):
    return map[i]

void main([string] args):
    m1 = f(1)
    m2 = f(2)
    m3 = f(3)
    
    m1[2] = 4
    m2[1] = 23498
    
    out->println(str(get(1,m1)))
    out->println(str(get(2,m1)))
    out->println(str(get(1,m2)))
    out->println(str(get(1,m3)))
    out->println(str(get(3,m3)))
