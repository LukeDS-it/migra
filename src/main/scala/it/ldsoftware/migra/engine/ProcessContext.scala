package it.ldsoftware.migra.engine

import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.http.scaladsl.{Http, HttpExt}
import org.apache.pekko.stream.Materializer
import it.ldsoftware.migra.configuration.AppConfig

import java.io.{BufferedWriter, FileWriter}
import scala.collection.mutable
import scala.concurrent.ExecutionContext

/** The process context is the context in which a process is executed. It contains all the complementary services that
  * any process could need to run, such as the actor system on which the application is running, the materializer and
  * execution context based on that system and so on.
  *
  * @param system
  *   the main actor system that is used to run the process
  */
case class ProcessContext(
    system: ActorSystem,
    appConfig: AppConfig,
    fileResolver: FileResolver,
    tokenCaches: mutable.Map[String, TokenProvider] = mutable.Map()
) {

  lazy val materializer: Materializer = Materializer(system)

  lazy val executionContext: ExecutionContext = system.dispatcher

  lazy val http: HttpExt = Http(system)

  def getTokenCache(name: String): TokenProvider = tokenCaches(name)

  def addTokenCache(name: String, tokenProvider: TokenProvider): Unit = tokenCaches.put(name, tokenProvider)

  def retrieveFile(name: String): String = fileResolver.retrieveFile(name)

  def openFile(name: String): BufferedWriter = new BufferedWriter(new FileWriter(fileResolver.getFilePath(name)))

}
