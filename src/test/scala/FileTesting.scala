import DataProcess._
import org.scalatest.flatspec.AnyFlatSpec

class FileTesting extends AnyFlatSpec {

  "File Does not exist" should "return File not found error" in {
    val actualResult = dataProcess("unknownFileName.log")
    val expectedResult = Left(Error("file not found"))
    assertResult(expectedResult)(actualResult)
  }

  "File is empty" should "return File empty error" in {
    val actualResult = dataProcess("empty-calls.log")
    val expectedResult = Left(Error("Empty File"))
    assertResult(expectedResult)(actualResult)
  }

  "Missing data in call logs" should "return a parse call error" in {
    val actualResult = dataProcess("missing-data.log")
    val expectedResult = (Left(Error("Error in ParseCall")))
    assertResult(expectedResult)(actualResult)
  }
}