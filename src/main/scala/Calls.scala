case class Call(customersId: CustomersId, phoneNumberCalled: PhoneNumberCalled, callDuration: CallDuration)
case class CustomerCalls(customersId: CustomersId, listOfDuration: List[CallDuration])

final case class CustomersId(value: String)
final case class PhoneNumberCalled(value: String)
final case class CallDuration(value: String)
final case class CallDurationInSeconds(value: Long)
final case class Cost(value: Double)
final case class Talk(value: String)