package it.ldsoftware.migra.server.services

import akka.cluster.sharding.typed.scaladsl.ClusterSharding

class ProcessService(sharding: ClusterSharding) {

  def getProcesses() = ???

  def createProcess() = ???

  def editProcess() = ???

  def deleteProcess() = ???

  def startProcess() = ???

}
