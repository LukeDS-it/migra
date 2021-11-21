package it.ldsoftware.starling.engine

import scala.concurrent.Future

trait TokenProvider {

  def token: Future[String]

}
