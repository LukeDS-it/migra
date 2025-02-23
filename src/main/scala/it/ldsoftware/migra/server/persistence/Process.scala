package it.ldsoftware.migra.server.persistence

import org.apache.pekko.actor.typed.{ActorRef, ActorSystem, Behavior}
import org.apache.pekko.cluster.sharding.typed.scaladsl.{ClusterSharding, Entity, EntityTypeKey}
import org.apache.pekko.persistence.typed.PersistenceId
import org.apache.pekko.persistence.typed.scaladsl.EventSourcedBehavior
import it.ldsoftware.migra.server.persistence.serialization.CborSerializable

object Process {

  val Tag: String = "process"
  val Key: EntityTypeKey[Command] = EntityTypeKey[Command]("Process")

  sealed trait Command extends CborSerializable

  sealed trait Event extends CborSerializable

  sealed trait Response extends CborSerializable

  sealed trait State extends CborSerializable

  def apply(id: String): Behavior[Command] =
    EventSourcedBehavior
      .withEnforcedReplies[Command, Event, State](
        persistenceId = PersistenceId.ofUniqueId(id),
        emptyState = ???,
        commandHandler = (state, command) => ???,
        eventHandler = (state, event) => state
      )
      .withTagger(_ => Set(Tag))

  def init(system: ActorSystem[?]): ActorRef[?] =
    ClusterSharding(system).init(Entity(Key)(context => Process(context.entityId)))
}
