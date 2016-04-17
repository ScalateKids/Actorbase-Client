package com.actorbase.cli.models

class LogoutCommand(cr: CommandReceiver) extends Command {
  override def execute() : String = cr.logout
}
