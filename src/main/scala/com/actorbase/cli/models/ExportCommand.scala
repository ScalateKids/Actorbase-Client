package com.actorbase.cli.models

class ExportCommand(op: CommandReceiver) extends Command {
  override def execute() : String = op.export
}