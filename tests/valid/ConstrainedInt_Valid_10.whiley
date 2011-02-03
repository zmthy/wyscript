// This example was inspired by comments from Stuart Marshall.

define anat as int
define bnat as int

bnat atob(anat x):
    return x

anat btoa(bnat x):
    return x

void main([string] args):
    x = 1
    println(str(atob(x)))
    println(str(btoa(x)))
    
    
