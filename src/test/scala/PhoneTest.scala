//import DataProcess._
//import org.scalatest.flatspec.AnyFlatSpec
//
//class PhoneProjectTest extends AnyFlatSpec {
//
//  val customerACall1 = model.Call(model.CustomersId("A"), model.PhoneNumberCalled("555-333-212"), model.CallDuration("00:02:03"))
//  val customerACall2 = model.Call(model.CustomersId("A"), model.PhoneNumberCalled("555-433-242"), model.CallDuration("00:06:41"))
//  val customerBCall1 = model.Call(model.CustomersId("B"), model.PhoneNumberCalled("555-333-212"), model.CallDuration("00:01:20"))
//  val customerBCall2 = model.Call(model.CustomersId("B"), model.PhoneNumberCalled("555-333-212"), model.CallDuration("00:02:15"))
//
//  "model.Call log" should "return a list of Calls" in {
//    val actualResult = dataProcess("simple-calls.log")
//    val expectedResult = Right(List(customerACall1, customerACall2, customerBCall1, customerBCall2))
//    assertResult(expectedResult)(actualResult)
//  }
//
//  "model.Call logs parsed correctly where a string of call" should "return a model.Call item" in {
//    val stringCallLog = "A 555-333-212 00:02:03"
//    val actualResult = parseCall(stringCallLog)
//    val expectedResult = Right(customerACall1)
//    assertResult(expectedResult)(actualResult)
//  }
//
//  "List of Calls" should "return Customer grouped by call duration" in {
//    val actualResult = customerIdGroupedByTotalCost(Right(List(customerACall1, customerACall2, customerBCall1)))
//    val expectedResult = List(
//      model.CustomersId("B") -> model.Cost(2.4),
//      model.CustomersId("A") -> model.Cost(3.69)
//    )
//    assertResult(expectedResult)(actualResult)
//  }
//
//  "Customer with a list of call durations" should "return a list of filtered call durations in seconds" in {
//    val actualResult = promotionApplied(List(customerACall1, customerACall2, customerBCall1, customerBCall2))
//    val expectedResult = List(customerACall1, customerBCall1, customerBCall2)
//    assertResult(expectedResult)(actualResult)
//  }
//
//  "Given a model.Call Item" should "return cost for call" in {
//    val actualResult = calculateCost(customerACall1)
//    val expectedResult = model.Cost(3.69)
//    assertResult(expectedResult)(actualResult)
//  }
//
//  "Given filtered calls" should "return list of costs per call" in {
//    val filteredCalls = List(customerACall1, customerBCall1, customerBCall2)
//    val actualResult = listOfCosts(filteredCalls, List())
//    val expectedResult = List(model.Cost(3.69), model.Cost(2.4), model.Cost(4.05))
//    assertResult(expectedResult)(actualResult)
//  }
//
//  "Given CustomerId mapped to all call durations" should "return customerId with total cost" in {
//    val actualResult = calculateTotalCost(Map(model.CustomersId("B") -> List(model.Cost(2.4), model.Cost(4.05))))
//    val expectedResult = List((model.CustomersId("B"), model.Cost(6.45)))
//    assertResult(expectedResult)(actualResult)
//  }
//
//  "Given List of filtered calls and a list of total costs" should "join customerId with total cost" in {
//    val filteredCalls = promotionApplied(List(customerACall1, customerACall2, customerBCall1, customerBCall2))
//    val listOfCallCostPerCustomerFiltered = List(model.Cost(3.69), model.Cost(2.4))
//    val actualResult = joinCustomerWithTotalCost(filteredCalls, listOfCallCostPerCustomerFiltered)
//    val expectedResult = List((model.CustomersId("B"), model.Cost(2.4)), (model.CustomersId("A"), model.Cost(3.69)))
//    assertResult(expectedResult)(actualResult)
//  }
//
//  "Given a customerId with total cost" should "return a string" in {
//    val actualResult = DataProcess.toTalk(List((model.CustomersId("A"), model.Cost(3.69)), (model.CustomersId("B"), model.Cost(6.45))))
//    val expectedResult = List(Talk("model.CustomersId(B) has a bill of £6.45"), Talk("model.CustomersId(A) has a bill of £3.69"))
//    assertResult(expectedResult)(actualResult)
//  }
//}
