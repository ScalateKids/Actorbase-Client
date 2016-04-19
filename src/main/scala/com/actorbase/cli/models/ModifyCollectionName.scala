package com.actorbase.cli.models

class ModifyCollectionNameCommand(cr: CommandReceiver) extends Command {
  override def execute() : String = cr.modifyCollectionName
}

