package fresco

import java.util.UUID

import akka.actor.{Actor, ActorSystem, Props}
import akka.routing.RoundRobinPool
import fresco.filesystem.Repository
import fresco.logging.Logging

/**
  * Created by suhail on 2016-09-08.
  *
  */

case object Create

class RepositoryCreation extends Actor with Logging {
  override def receive: Receive = {
    case Create => {
      logger.debug(s"created")
      val repoLoc = "/Users/suhail/tmp/fresco"
      Repository(repoLoc)
      Thread.sleep(2000)
    }
    case _ => {

    }
  }
}

object Launcher extends App with Logging {
//  val system = ActorSystem("system")
//  val creator = system.actorOf(RoundRobinPool(100).props(Props[RepositoryCreation]))
//  creator ! Create
//  system.terminate
//  logger.debug("application shutdown")
  (1 to 10).foreach(i => {
    val s: String = UUID.randomUUID().toString();
    println(s"id: $s")
  })

}
