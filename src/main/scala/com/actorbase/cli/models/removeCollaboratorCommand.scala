package com.actorbase.cli.models

class RemoveCollaboratorCommand(cr: CommandReceiver) extends Command {
  override def execute() : String = cr.removeCollaborator
}