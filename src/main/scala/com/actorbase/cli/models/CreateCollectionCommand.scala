package com.actorbase.cli.models

class CreateCollectionCommand(cr: CommandReceiver) extends Command {
  override def execute() : String = cr.createCollection
}
