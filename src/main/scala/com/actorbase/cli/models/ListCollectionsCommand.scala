package com.actorbase.cli.models

class ListCollectionsCommand(cr: CommandReceiver) extends Command {
  override def execute() : String = cr.listCollections
}

