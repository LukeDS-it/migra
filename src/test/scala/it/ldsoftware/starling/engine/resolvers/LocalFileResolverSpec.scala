package it.ldsoftware.starling.engine.resolvers

import org.scalamock.scalatest.MockFactory
import org.scalatest.GivenWhenThen
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.io.File

class LocalFileResolverSpec extends AnyWordSpec with GivenWhenThen with Matchers with MockFactory {

  "the local file resolver" should {
    "read file from a relative path" in {
      val rootReference = new File("./src/test/resources/relative-dir/reference-file.txt")
      val target = "relative-file.txt"
      new LocalFileResolver(rootReference).retrieveFile(target) shouldBe "relative"
    }

    "read file from an absolute path" in {
      val absoluteFile = new File("./src/test/resources/absolute-file.txt").getAbsolutePath
      val rootReference = new File("./src/test/resources/relative-dir/reference-file.txt")
      new LocalFileResolver(rootReference).retrieveFile(absoluteFile) shouldBe "absolute"
    }
  }


}
