package service

import config.config._
import model.{Call, CallDuration, CallWithCost, Cost, CustomersId}
import java.time.{Duration, LocalTime}

object CalculateBill {

  val promotion = new ApplyPromotion

  def getCustomersTotalBill(parseCalls: List[Call], promotionApplied: Boolean = false): Map[CustomersId, Cost] = {
    val logsGroupedByCustomerId = groupCustomerWithCalls(parseCalls.map(calculateCallCost))

    if (promotionApplied) {
      val filteredCallsByPromotion = logsGroupedByCustomerId.map {
        case (key, value) => key -> promotion.applyPromotion(value)
      }
      getTotalCost(filteredCallsByPromotion)
    } else {
      val customerWithListOfCosts = logsGroupedByCustomerId.map {
        case (key, value) => key -> value.map(_.cost)
      }
      getTotalCost(customerWithListOfCosts)
    }
  }

  // was private but removed for testing
   def calculateCallCost(call: Call): CallWithCost = {
     val callDurationInSeconds: Long = Duration.between(LocalTime.MIN, LocalTime.parse(call.callDuration.value)).getSeconds
     val callInSeconds = call.copy(call.customersId, call.phoneNumberCalled, CallDuration(callDurationInSeconds.toString))
    lazy val callDuration = callInSeconds.callDuration.value.toDouble
    val cost: Cost =
      if (callDuration >= threeMinutesInSeconds) {
        Cost((callDuration - threeMinutesInSeconds) * callsOver3minCost +
          threeMinutesInSeconds * callsUnder3minCost)
      } else {
        Cost(callsUnder3minCost * callDuration)
      }
    CallWithCost(call, cost)
  }

  // was private but removed for testing
   def groupCustomerWithCalls(list: List[CallWithCost]): Map[CustomersId, List[CallWithCost]] = {
    list.groupBy(_.call.customersId)
  }

  def getTotalCost(customerIdWithCosts: Map[CustomersId, List[Cost]]): Map[CustomersId, Cost] = {
    customerIdWithCosts.map {
      case (key, value) => key -> Cost("%.2f".format(value.map(_.value).sum).toDouble)
    }
  }
}
