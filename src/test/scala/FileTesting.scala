import parser.CallLogProcess._
import org.scalatest.flatspec.AnyFlatSpec
import parser.FileLoading.processCallLog

class FileTesting extends AnyFlatSpec {

  "File does not exist" should "return File doesn't exist error" in {
    val actualResult = processCallLog("unknownFileName.log")
    val expectedResult = Left(model.Error("Error while processing file: File doesn't exist"))
    assertResult(expectedResult)(actualResult)
  }

  "Empty file" should "return File is empty error" in {
    val actualResult = processCallLog("empty-calls.log")
    val expectedResult = Left(model.Error("File is empty"))
    assertResult(expectedResult)(actualResult)
  }

  "Missing data in call logs" should "return File has wrong format error" in {
    val actualResult = processCallLog("missing-data.log")
    val expectedResult = (Left(model.Error("Error while parsing log: File has wrong format")))
    assertResult(expectedResult)(actualResult)
  }
}