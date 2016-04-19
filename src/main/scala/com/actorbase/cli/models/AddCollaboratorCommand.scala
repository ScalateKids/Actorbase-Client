package com.actorbase.cli.models

class AddCollaboratorCommand(cr: CommandReceiver) extends Command {
  override def execute() : String = cr.addCollaborator
}