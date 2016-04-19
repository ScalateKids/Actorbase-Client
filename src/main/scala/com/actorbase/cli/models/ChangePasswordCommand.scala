package com.actorbase.cli.models

class ChangePasswordCommand(cr: CommandReceiver) extends Command {
  override def execute() : String = cr.changePassword
}

