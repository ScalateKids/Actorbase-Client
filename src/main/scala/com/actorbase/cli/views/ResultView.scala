package com.actorbase.cli.views

import cli.models._
import com.actorbase.cli.models.Observable

class ResultView extends Observer {
  override def update(o: Observable): Unit = println(o.getState)
}
