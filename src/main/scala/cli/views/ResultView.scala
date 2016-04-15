package cli.views

import cli.models._

class ResultView extends Observer {
  override def update(o: Observable): Unit = println(o.getState)
}
