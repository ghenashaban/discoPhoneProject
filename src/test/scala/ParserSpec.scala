import Common._
import org.scalatest.flatspec.AnyFlatSpec
import parser.CallLogProcess._
import parser.FileLoading.processCallLog

class ParserSpec extends AnyFlatSpec {

  "Given a existing file name with a correct format " should "return a list of Calls" in {
    val actualResult = processCallLog("simple-calls.log")
    val expectedResult = Right(List(customerACall1, customerACall2, customerBCall1, customerBCall2))
    assertResult(expectedResult)(actualResult)
  }

  "One string call log line" should "return a Call item" in {
    val stringCallLog = "A 555-333-212 00:02:03"
    val actualResult = parseCall(stringCallLog)
    val expectedResult = Right(customerACall1)
    assertResult(expectedResult)(actualResult)
  }
}
