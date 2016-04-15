package cli.models

import cli.models._
import cli.views._

trait Observable {

  private var observers : List[Observer] = Nil
  private var state : String = ""

  def setState(s: String) : Unit = state = s

  def getState : String = state

  def attach(observer: Observer) : Unit = observers :+= observer

  def notifyAllObservers() : Unit = {
    for(observer <- observers)
      observer.update(this)
  }

}

class CommandLauncher extends Observable {

  private var history : List[Command] = Nil

  def storeAndExecute(cmd : Command) : String = {
    this.history :+= cmd
    setState(cmd.execute())
    notifyAllObservers
    getState
  }
}

trait Command {
  def execute() : String
}

class Operations(params: Map[Any, Any]) {
  def insert() : String = {
    var result : String = "[INSERT]\n"
    for ((k, v) <- params) {
      result += s"$k -> $v\n"
    }
    result
  }
  def export() : String = {
    var result : String = "[EXPORT]\n"
    for ((k, v) <- params) {
      result += s"$k -> $v\n"
    }
    result
  }
}

class InsertCommand(op: Operations) extends Command {
  override def execute() : String = op.insert
}

class ExportCommand(op: Operations) extends Command {
  override def execute() : String = op.export
}
