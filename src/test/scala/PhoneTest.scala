import DataProcess._
import org.scalatest.flatspec.AnyFlatSpec

class PhoneProjectTest extends AnyFlatSpec {

  val customerAfirstCall = Call(CustomersId("A"), PhoneNumberCalled("555-333-212"), CallDuration("00:02:03"))
  val customerAsecondCall = Call(CustomersId("A"), PhoneNumberCalled("555-433-242"), CallDuration("00:06:41"))
  val customerBfirstCall = Call(CustomersId("B"), PhoneNumberCalled("555-333-212"), CallDuration("00:01:20"))
  val customerBsecondCall = Call(CustomersId("B"), PhoneNumberCalled("555-333-212"), CallDuration("00:02:15"))

  "Call log" should "return a list of Calls" in {
    val actualResult = dataProcess("simple-calls.log")
    val expectedResult = Right(List(customerAfirstCall, customerAsecondCall, customerBfirstCall, customerBsecondCall))
    assertResult(expectedResult)(actualResult)
  }

  "Call logs parsed correctly where a string of call" should "return a Call item" in {
    val stringCallLog = "A 555-333-212 00:02:03"
    val actualResult = parseCall(stringCallLog)
    val expectedResult = Right(customerAfirstCall)
    assertResult(expectedResult)(actualResult)
  }

  "List of Calls" should "return Customer grouped by call duration" in {
    val actualResult = customerIdGroupedByTotalCost(Right(List(customerAfirstCall, customerAsecondCall, customerBfirstCall)))
    val expectedResult = List(
      CustomersId("B") -> Cost(2.4),
      CustomersId("A") -> Cost(3.69)
    )
    assertResult(expectedResult)(actualResult)
  }

  "Customer with a list of call durations" should "return a list of filtered call durations in seconds" in {
    val actualResult = promotionApplied(List(customerAfirstCall, customerAsecondCall, customerBfirstCall, customerBsecondCall))
    val expectedResult = List(customerAfirstCall, customerBfirstCall, customerBsecondCall)
    assertResult(expectedResult)(actualResult)
  }

  "Given a Call Item" should "return cost for call" in {
    val actualResult = calculateCost(customerAfirstCall)
    val expectedResult = Cost(3.69)
    assertResult(expectedResult)(actualResult)
  }

  "Given filtered calls" should "return list of costs per call" in {
    val filteredCalls = List(customerAfirstCall, customerBfirstCall, customerBsecondCall)
    val actualResult = listOfCosts(filteredCalls, List())
    val expectedResult = List(Cost(3.69), Cost(2.4), Cost(4.05))
    assertResult(expectedResult)(actualResult)
  }

  "Given CustomerId mapped to all call durations" should "return customerId with total cost" in {
    val actualResult = calculateTotalCost(Map(CustomersId("B") -> List(Cost(2.4), Cost(4.05))))
    val expectedResult = List((CustomersId("B"), Cost(6.45)))
    assertResult(expectedResult)(actualResult)
  }

  "Given List of filtered calls and a list of total costs" should "join customerId with total cost" in {
    val filteredCalls = promotionApplied(List(customerAfirstCall, customerAsecondCall, customerBfirstCall, customerBsecondCall))
    val listOfCallCostPerCustomerFiltered = List(Cost(3.69), Cost(2.4))
    val actualResult = joinCustomerWithCost(filteredCalls, listOfCallCostPerCustomerFiltered)
    val expectedResult = List((CustomersId("A"), Cost(3.69)), (CustomersId("B"), Cost(2.4)))
    assertResult(expectedResult)(actualResult)
  }

  "Given a customerId with total cost" should "return a string" in {
    val actualResult = DataProcess.toTalk(List((CustomersId("A"), Cost(3.69)), (CustomersId("B"), Cost(6.45))))
    val expectedResult = List(Talk("CustomersId(B) has a bill of £6.45"), Talk("CustomersId(A) has a bill of £3.69"))
    assertResult(expectedResult)(actualResult)
  }
}
