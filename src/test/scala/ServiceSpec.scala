import model._
import Common._
import org.scalatest.flatspec.AnyFlatSpec
import service.CalculateBill._

class ServiceSpec extends AnyFlatSpec {

  "List of Calls" should "return Customer grouped by total cost with promotion applied" in {
    val actualResult = getCustomersTotalBill(
      List(customerACall1, customerACall2, customerBCall1, customerBCall2), true)
    val expectedResult = Map(CustomersId("B") -> Cost(0.0), CustomersId("A") -> Cost(6.15))
    assertResult(expectedResult)(actualResult)
  }

  "List of Calls" should "return Customer grouped by total cost with no promotion" in {
    val actualResult = getCustomersTotalBill(
      List(customerACall1, customerACall2, customerBCall1, customerBCall2))
    val expectedResult = Map(CustomersId("B") -> Cost(10.75), CustomersId("A") -> Cost(21.78))
    assertResult(expectedResult)(actualResult)
  }

  "Given a Call log" should "return a CallLogWithCost" in {
    val actualResult = calculateCallCost(customerACall1)
    val expectedResult = CallWithCost(customerACall1, Cost(6.15))
    assertResult(expectedResult)(actualResult)
  }

  "Given a list of CallWithCost" should "return CallWithCost mapped by customerId" in {
    val listOfCallCost: List[CallWithCost] = List(CallWithCost(customerACall1, Cost(6.15)), CallWithCost(customerBCall1, Cost(4.0)),
      CallWithCost(customerACall2, Cost(14.49)), CallWithCost(customerBCall2, Cost(6.75)))
    val actualResult = groupCustomerWithCalls(listOfCallCost)
    val expectedResult = Map(
      CustomersId("B") ->
        List(CallWithCost(Call(CustomersId("B"), PhoneNumberCalled("555-333-212"), CallDuration("00:01:20")), Cost(4.0)),
             CallWithCost(Call(CustomersId("B"), PhoneNumberCalled("555-333-212"), CallDuration("00:02:15")), Cost(6.75))),
      CustomersId("A") ->
        List(CallWithCost(Call(CustomersId("A"), PhoneNumberCalled("555-333-212"), CallDuration("00:02:03")), Cost(6.15)),
             CallWithCost(Call(CustomersId("A"), PhoneNumberCalled("555-433-242"), CallDuration("00:06:03")), Cost(14.49))))
    assertResult(expectedResult)(actualResult)
  }

  "Given a customerId with a list of costs" should "return customerId with total bill ignoring filtering" in {
    val input = Map(CustomersId("A") -> List(Cost(6.15), Cost(14.49)), CustomersId("B") -> List(Cost(4.0), Cost(6.75)))
    val actualResult = getTotalCost(input)
    val expectedResult =  Map(CustomersId("A") -> Cost(20.64), CustomersId("B") -> Cost(10.75))
    assertResult(expectedResult)(actualResult)
  }
}
