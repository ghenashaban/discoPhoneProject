
object Common {
  val customerACall1 = model.Call(model.CustomersId("A"), model.PhoneNumberCalled("555-333-212"), model.CallDuration("00:02:03"))
  val customerACall2 = model.Call(model.CustomersId("A"), model.PhoneNumberCalled("555-433-242"), model.CallDuration("00:06:41"))
  val customerBCall1 = model.Call(model.CustomersId("B"), model.PhoneNumberCalled("555-333-212"), model.CallDuration("00:01:20"))
  val customerBCall2 = model.Call(model.CustomersId("B"), model.PhoneNumberCalled("555-333-212"), model.CallDuration("00:02:15"))
}
