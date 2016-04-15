package cli.views

import cli.models.Observable

trait Observer {
  def update(o: Observable): Unit
}
