string f(bool b):
    if(b):
        return "TRUE"
    else:
        return "FALSE"

void main([string] args):
    out->println(f(true))
    out->println(f(false))
