package it.ldsoftware.starling.engine

import akka.NotUsed
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, Merge, Sink, Source}
import akka.stream.{ClosedShape, Graph}
import it.ldsoftware.starling.workers.consumers.Consumer
import it.ldsoftware.starling.workers.extractors.Extractor
import it.ldsoftware.starling.workers.model.{ConsumerResult, ExtractionResult}

import scala.concurrent.Future

class DataProcessor(extractors: List[Extractor], consumers: List[Consumer], parLevel: Int) {

  val input: Source[ExtractionResult, NotUsed] =
    Source
      .future(extractors.head.extract())
      .flatMapConcat(i => Source(i))

  val identity: Flow[ExtractionResult, ExtractionResult, NotUsed] = Flow[ExtractionResult]

  val pipe: List[Flow[ExtractionResult, ExtractionResult, NotUsed]] =
    extractors.tail
      .map { e =>
        Flow[ExtractionResult]
          .mapAsync(parLevel)(rd => e.piped(rd).extract())
          .flatMapConcat(i => Source(i))
      }

  val output: List[Flow[ExtractionResult, ConsumerResult, NotUsed]] =
    consumers map { c =>
      Flow[ExtractionResult]
        .mapAsync(parLevel)(rd => c.consume(rd))
    }

  def executionGraph(sink: Sink[ConsumerResult, Future[ConsumerResult]]): Graph[ClosedShape, Future[ConsumerResult]] =
    GraphDSL.create(sink) { implicit builder => sink =>
      import GraphDSL.Implicits._

      val piped = pipe.foldLeft(input ~> identity) {
        case (acc, next) => acc ~> next
      }

      val mux = builder.add(Broadcast[ExtractionResult](output.size))
      val demux = builder.add(Merge[ConsumerResult](output.size))

      piped ~> mux

      output.foreach(o => mux ~> o ~> demux)

      demux ~> sink

      ClosedShape
    }
}
