package com.actorbase.cli.views

class ActorbasePrompt extends PromptProvider{

  val os = System.getProperty("os.name")

  override def getPrompt: String = {
    "actorbasecli@" + os.toLowerCase + "$ "
  }
}
