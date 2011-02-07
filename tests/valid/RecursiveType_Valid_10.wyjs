define Link as { LinkedList next, int data }
define LinkedList as null|Link

int sum(LinkedList l):
    if l ~= null:
        return 0
    else:
        return l.data + sum(l.next)

void main([[char]] args):
    l1 = { next: null, data: 1}
    l2 = { next: l1, data: 2}
    l3 = { next: l2, data: 3}
    println(str(sum(l1))) // 1
    println(str(sum(l2))) // 1 + 2 = 3
    println(str(sum(l3))) // 1 + 2 + 3 = 6

