[char] f([[int]] x):
    if(|x[0]| > 2):
        return str(x[0][1])
    else:
        return ""


void main([[char]] args):
     arr = [[1,2,3],[1]]
     println(f(arr))
