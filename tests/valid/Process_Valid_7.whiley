define MyProc as process { int x }

void MyProc::inc(int i):
    this->x = x + i

void main([[char]] args):
    mproc = spawn { x:1 }
    mproc->inc(10)
    println(str(mproc->x))
