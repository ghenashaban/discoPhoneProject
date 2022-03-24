package model

final case class CallWithCost(customersId: CustomersId, phoneNumberCalled: PhoneNumberCalled, callDuration: CallDuration, cost: Cost)

