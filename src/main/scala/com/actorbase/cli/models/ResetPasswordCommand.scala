package com.actorbase.cli.models

class ResetPasswordCommand(cr: CommandReceiver) extends Command {
  override def execute() : String = cr.resetPassword
}