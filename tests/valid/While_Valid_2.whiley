define nat as int

nat sum([nat] ls):
    i=0
    sum = 0
    while i < |ls|:
        sum = sum + ls[i]
        i = i + 1
    return sum

void main([string] args):
    out->println(str(sum([])))
    out->println(str(sum([1,2,3])))
    out->println(str(sum([12387,98123,12398,12309,0])))
