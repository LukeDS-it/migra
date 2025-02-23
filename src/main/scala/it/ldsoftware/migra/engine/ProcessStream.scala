package it.ldsoftware.migra.engine

import org.apache.pekko.NotUsed
import org.apache.pekko.stream.scaladsl.{Broadcast, Flow, GraphDSL, Merge, Sink, Source}
import org.apache.pekko.stream.{ClosedShape, Graph}

import scala.concurrent.Future

class ProcessStream(extractors: List[Extractor], consumers: List[Consumer], parLevel: Int) {

  lazy val input: Source[ExtractionResult, NotUsed] =
    Source
      .future(extractors.head.extract())
      .flatMapConcat(i => Source(i))

  private lazy val identity: Flow[ExtractionResult, ExtractionResult, NotUsed] = Flow[ExtractionResult]

  private lazy val pipe: List[Flow[ExtractionResult, ExtractionResult, NotUsed]] =
    extractors.tail.map { e =>
      e.throttling
        .map(d => Flow[ExtractionResult].throttle(1, d))
        .getOrElse(Flow[ExtractionResult])
        .mapAsync(parLevel)(rd => e.piped(rd).extract())
        .flatMapConcat(i => Source(i))
    }

  lazy val output: List[Flow[ExtractionResult, ConsumerResult, NotUsed]] =
    consumers.map { c =>
      c.throttling
        .map(d => Flow[ExtractionResult].throttle(1, d))
        .getOrElse(Flow[ExtractionResult])
        .mapAsync(parLevel)(rd => c.consume(rd))
    }

  def executionGraph(sink: Sink[ConsumerResult, Future[ProcessStats]]): Graph[ClosedShape, Future[ProcessStats]] =
    GraphDSL.createGraph(sink) { implicit builder => sink =>
      import GraphDSL.Implicits.*

      val piped = pipe.foldLeft(input ~> identity) { case (acc, next) =>
        acc ~> next
      }

      val mux = builder.add(Broadcast[ExtractionResult](output.size))
      val demux = builder.add(Merge[ConsumerResult](output.size))

      piped ~> mux

      output.foreach(o => mux ~> o ~> demux)

      demux ~> sink

      ClosedShape
    }
}
