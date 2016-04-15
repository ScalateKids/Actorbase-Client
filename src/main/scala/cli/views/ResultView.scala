package cli.views

import cli.models._

trait Observer {
  def update(o: Observable): Unit
}

class View extends Observer {
  override def update(o: Observable): Unit = println(o.getState)
}
