package cli.models

class InsertItemCommand(op: CommandReceiver) extends Command {
  override def execute() : String = op.insert
}