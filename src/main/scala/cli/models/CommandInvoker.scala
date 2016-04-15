package cli.models

class CommandInvoker extends Observable {

  private var history : List[Command] = Nil

  def storeAndExecute(cmd : Command) : String = {
    this.history :+= cmd
    setState(cmd.execute())
    notifyAllObservers()
    getState
  }
}