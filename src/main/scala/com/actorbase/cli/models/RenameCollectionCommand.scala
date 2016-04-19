package com.actorbase.cli.models

class RenameCollectionCommand(cr: CommandReceiver) extends Command {
  override def execute() : String = cr.renameCollection
}

