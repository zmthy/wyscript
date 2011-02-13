define MyProc as process { bool flag }

void MyProc::run(System sys):
    if flag:
        sys->println("TRUE")
    else:
        sys->println("FALSE")

void main([[char]] args):
    mproc = spawn { flag:false }     
    mproc->run(this)
