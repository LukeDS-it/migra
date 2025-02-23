package it.ldsoftware.migra.server.persistence

import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.cluster.sharding.typed.scaladsl.{ClusterSharding, Entity, EntityTypeKey}
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.EventSourcedBehavior
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
