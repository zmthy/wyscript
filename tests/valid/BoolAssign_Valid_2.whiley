void main([string] args):
    x = true
    y = false
    println(str(x))
    println(str(y))
    println("AND")
    x = x && y
    println(str(x))
    println("NOT")
    println(str(!x))
