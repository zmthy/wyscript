define SyntaxError as {[char] msg}

[char] f(int x):
    if x > 0:        
        nst = {input: "Hello World"}
    else:
        nst = syntaxError("problem")
    // check for error
    if nst ~= {[int] msg}:
        return "error"
    else:
        return nst.input

// Create a syntax error
SyntaxError syntaxError([char] errorMessage):
    return {msg: errorMessage}

void main([[char]] args):
    println(f(0))
    println(f(1))
