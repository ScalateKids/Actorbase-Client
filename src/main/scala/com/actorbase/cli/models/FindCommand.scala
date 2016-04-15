package com.actorbase.cli.models

class FindCommand(cr: CommandReceiver) extends Command {
  override def execute() : String = cr.find
}
