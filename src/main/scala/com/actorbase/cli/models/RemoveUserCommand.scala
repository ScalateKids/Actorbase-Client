package com.actorbase.cli.models

class RemoveUserCommand(cr: CommandReceiver) extends Command {
  override def execute() : String = cr.removeUser
}