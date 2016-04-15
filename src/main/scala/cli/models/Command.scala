package cli.models

trait Command {
  def execute() : String
}