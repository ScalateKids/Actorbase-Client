package com.actorbase.cli.models

class AddContributorCommand(cr: CommandReceiver) extends Command {
  override def execute() : String = cr.addContributor
}