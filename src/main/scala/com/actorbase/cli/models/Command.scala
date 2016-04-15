package com.actorbase.cli.models

trait Command {
  def execute() : String
}