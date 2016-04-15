package com.actorbase.cli.models

class LoginCommand(op: CommandReceiver) extends Command {
  override def execute() : String = op.login
}
