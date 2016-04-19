package com.actorbase.cli.models

class AddUserCommand(cr: CommandReceiver) extends Command {
  override def execute() : String = cr.addUser
}
