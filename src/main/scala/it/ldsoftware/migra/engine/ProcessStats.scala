package it.ldsoftware.migra.engine

case class ProcessStats(consumed: Int, notConsumed: Int) {
  def withSuccess: ProcessStats = copy(consumed + 1, notConsumed)
  def withFailure: ProcessStats = copy(consumed, notConsumed + 1)
}
