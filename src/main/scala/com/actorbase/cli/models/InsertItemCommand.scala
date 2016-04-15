package com.actorbase.cli.models

class InsertItemCommand(cr: CommandReceiver) extends Command {
  override def execute() : String = cr.insert
}