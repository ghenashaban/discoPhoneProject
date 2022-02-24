import DataProcess._
import org.scalatest.flatspec.AnyFlatSpec
import java.time.LocalTime
import scala.collection.immutable.HashMap

class PhoneProjectTest extends AnyFlatSpec {

  "Simplified call logs" should "return a list of Call" in {
    val actualResult = dataProcess("simple-calls.log")
    val expectedResult = Right(List(
      Call(CustomersId("A"), PhoneNumberCalled("555-333-212"), CallDuration("00:02:03")),
      Call(CustomersId("A"), PhoneNumberCalled("555-433-242"), CallDuration("00:06:41")),
      Call(CustomersId("B"), PhoneNumberCalled("555-333-212"), CallDuration("00:01:20"))))
    assertResult(expectedResult)(actualResult)
  }

  "Call logs parsed correctly where a string of call" should "return a Call case class" in {
    val stringCallLog = "A 555-333-212 00:02:03"
    val actualResult = parseCall(stringCallLog)
    val expectedResult = Right(Call(CustomersId("A"), PhoneNumberCalled("555-333-212"), CallDuration("00:02:03")))
    assertResult(expectedResult)(actualResult)
  }

  "calls grouped by customerId" should "???" in {
    val actualResult = callDurationsGroupedByCustomerId(Right(List(
      Call(CustomersId("A"), PhoneNumberCalled("555-333-212"), CallDuration("00:02:03")),
      Call(CustomersId("A"), PhoneNumberCalled("555-433-242"), CallDuration("00:06:41")),
      Call(CustomersId("B"), PhoneNumberCalled("555-333-212"), CallDuration("00:01:20"))))
    )
    val expectedResult = Right(HashMap(
      CustomersId("A") -> List(LocalTime.parse("00:02:03"), LocalTime.parse("00:06:41")),
      CustomersId("B") -> List(LocalTime.parse("00:01:20"))))
    assertResult(expectedResult)(actualResult)
  }

  "Filter out the call with longest Duration" should "return a list of filtered call durations in seconds" in {
    val actualResult = filterCallsForPromotion(Map(CustomersId("A") ->
      List(LocalTime.parse("00:02:03"),
        LocalTime.parse("00:06:41"),
        LocalTime.parse("00:01:20"))))
    val expectedResult = List(List(CallDurationInSeconds(123), CallDurationInSeconds(80)))
    assertResult(expectedResult)(actualResult)
  }

  "Given a list of durations in seconds per customer" should "return total costs per customer after filter applied" in {
    val listOfCallDurationinSecondsFiltered: List[List[CallDurationInSeconds]] = List(List(CallDurationInSeconds(123)), List(CallDurationInSeconds(80)))
    val actualResult = listOfCallCostsPerCustomer(listOfCallDurationinSecondsFiltered, List())
    val expectedResult: List[Cost] = List(Cost(3.69), Cost(2.4))
    assertResult(expectedResult)(actualResult)
  }

  "CustomerA list of duration of calls in seconds" should "return total cost" in {
    val actualResult = calculateCost(List(CallDurationInSeconds(123), CallDurationInSeconds(63)))
    val expectedResult = Cost(5.58)
    assertResult(expectedResult)(actualResult)
  }

  "Given List of customerId and a list of total cost per customer" should "join customerId with cost" in {
    val customerIdMappedWithDuration = List(CustomersId("A"), CustomersId("B"))
    val listOfCallCostPerCustomerFiltered = List(Cost(3.69), Cost(2.4))
    val actualResult = joinCustomerWithCost(customerIdMappedWithDuration, listOfCallCostPerCustomerFiltered)
    val expectedResult = List((CustomersId("A"), Cost(3.69)), (CustomersId("B"), Cost(2.4)))
    assertResult(expectedResult)(actualResult)
  }

  "Given a customerId with a cost" should "return a string" in {
    val actualResult = DataProcess.toString(List(
      (CustomersId("B"), Cost(28.64)))).value
    val expectedResult = "CustomersId(B) has a bill of £28.64"
    assertResult(expectedResult)(actualResult)
  }
}
