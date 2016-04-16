package com.actorbase.cli.models

class HelpCommand(cr: CommandReceiver) extends Command {
  override def execute() : String = cr.help
}
