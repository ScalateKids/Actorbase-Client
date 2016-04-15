package cli.views

object CommandLoop extends App{
  val ban = new ActorbaseBanner
  print(ban.getBanner())
  var loop = true
  while(loop){
    print(">> ")
    val x = scala.io.StdIn.readLine()
    loop = false
  }

}
