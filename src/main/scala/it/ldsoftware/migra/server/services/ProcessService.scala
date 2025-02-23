package it.ldsoftware.migra.server.services

import org.apache.pekko.cluster.sharding.typed.scaladsl.ClusterSharding

class ProcessService(sharding: ClusterSharding) {

  def getProcesses() = ???

  def createProcess() = ???

  def editProcess() = ???

  def deleteProcess() = ???

  def startProcess() = ???

}
