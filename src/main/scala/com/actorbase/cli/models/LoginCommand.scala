package com.actorbase.cli.models

class LoginCommand(cr: CommandReceiver) extends Command {
  override def execute() : String = cr.login
}
