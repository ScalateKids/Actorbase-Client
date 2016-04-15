package com.actorbase.cli.views

import com.actorbase.cli.models.Observable

trait Observer {
  def update(o: Observable): Unit
}
