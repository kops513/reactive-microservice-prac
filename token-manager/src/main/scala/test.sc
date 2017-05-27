import scala.collection.immutable.Stack
var stack = Stack.empty[Int]
stack = stack.push(1)
stack = stack.push(2)
stack = stack.pop
def solution(h: Array[Int]): Int = {
  var count = 0
  var stack = Stack.empty[Int]
  for(t <- h){
    if(stack.isEmpty){
      stack = stack.push(t)
    }
    else{
      var n = stack.top
      if(t > n) {
        stack = stack.push(t)
      }
      else{
        if(t != n){
          do{
            count = count + 1
            stack = stack.pop
            if(!stack.isEmpty)
              n = stack.top

          }while(!stack.isEmpty && t < n)

          if(t != n)
          stack = stack.push(t)
        }
      }
    }
  }
  count + stack.size
}

solution(Array(8,8,5,7,9,8,7,4,8))
solution(Array())
solution(Array(1))
solution(Array(1,1))
solution(Array(1,1,2))
solution(Array(2,1,1))


