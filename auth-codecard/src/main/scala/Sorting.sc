def decode(items : List[(Int,Int)]) = {
  for{
    j <- items
    k <- 1 to j._1
  }yield(j._2)
}

decode(List((4, 1), (1, 2), (2, 3), (4, 7)))

def fib(prev: Int, nextPrev: Int): Int = {
  println(nextPrev)
  val next = prev + nextPrev
  if(next > 15) System.exit(0)
  fib(next, prev)
}

fib(2 ,1)