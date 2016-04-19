package com.actorbase.cli.models

class RemoveItemCommand(cr: CommandReceiver) extends Command {
  override def execute() : String = cr.removeItem
}

