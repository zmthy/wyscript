define dummy as process {int x}

void dummy::f(int x):
    print str(x)

void main([string] args):
    this->f(1)
