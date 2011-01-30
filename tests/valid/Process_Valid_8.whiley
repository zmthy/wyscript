define MyProc as process { bool flag }

void MyProc::run(System sys):
    if flag:
        sys->out->println("TRUE")
    else:
        sys->out->println("FALSE")

void main([string] args):
    mproc = spawn { flag:false }     
    mproc->run(this)
