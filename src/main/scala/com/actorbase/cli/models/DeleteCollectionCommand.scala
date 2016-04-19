package com.actorbase.cli.models

class DeleteCollectionCommand(cr: CommandReceiver) extends Command {
  override def execute() : String = cr.deleteCollection
}

