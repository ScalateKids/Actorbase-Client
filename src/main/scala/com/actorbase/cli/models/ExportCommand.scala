package com.actorbase.cli.models

class ExportCommand(cr: CommandReceiver) extends Command {
  override def execute() : String = cr.export
}