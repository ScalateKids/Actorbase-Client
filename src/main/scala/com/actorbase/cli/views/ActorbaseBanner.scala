package com.actorbase.cli.views

class ActorbaseBanner() {
  def getBanner() : String = {
    val banner = """    ___   ________________  ____  ____  ___   _____ ______
                   #   /   | / ____/_  __/ __ \/ __ \/ __ )/   | / ___// ____/
                   #  / /| |/ /     / / / / / / /_/ / __  / /| | \__ \/ __/
                   # / ___ / /___  / / / /_/ / _, _/ /_/ / ___ |___/ / /___
                   #/_/  |_\____/ /_/  \____/_/ |_/_____/_/  |_/____/_____/
                   #""".stripMargin('#')
    banner.toString() + "\n"
  }
}
