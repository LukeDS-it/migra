package it.ldsoftware.starling.engine

import akka.actor.ActorSystem
import akka.http.scaladsl.{Http, HttpExt}
import akka.stream.Materializer

import scala.concurrent.ExecutionContext

/**
  * The process context is the context in which a process is executed.
  * It contains all the complementary services that any process could need to run, such
  * as the actor system on which the application is running, the materializer and execution
  * context based on that system and so on.
  *
  * @param system the main actor system that is used to run the process
  */
case class ProcessContext(system: ActorSystem) {

  lazy val materializer: Materializer = Materializer(system)

  lazy val executionContext: ExecutionContext = system.dispatcher

  lazy val http: HttpExt =  Http(system)


}
