package service

import config.config._
import model.{Call, CallWithCost, Cost, CustomersId}

import java.time.{Duration, LocalTime}

object CalculateBill {

  val promotion = new ApplyPromotion

  def getCustomersTotalBill(parseCalls: List[Call], promotionApplied: Boolean = false): Map[CustomersId, Cost] = {

    val logsGroupedByCustomerId = parseCalls.map(calculateCallCost).groupBy(_.call.customersId)

    val customerWithListOfCosts = if (promotionApplied) {
      logsGroupedByCustomerId.view.mapValues(promotion.applyPromotion).toMap
    } else {
      logsGroupedByCustomerId.view.mapValues(_.map(_.cost)).toMap
    }
    getTotalCost(customerWithListOfCosts)
  }

  // was private but removed for testing
  def calculateCallCost(call: Call): CallWithCost = {
    val durationInSeconds: Long = Duration.between(LocalTime.MIN, LocalTime.parse(call.callDuration.value)).getSeconds
    lazy val callDuration = durationInSeconds.toDouble
    val cost: Cost =
      if (callDuration >= threeMinutesInSeconds) {
        Cost((callDuration - threeMinutesInSeconds) * callsOver3minCost +
          threeMinutesInSeconds * callsUnder3minCost)
      } else {
        Cost(callsUnder3minCost * callDuration)
      }
    CallWithCost(call, cost)
  }

  def getTotalCost(customerIdWithCosts: Map[CustomersId, List[Cost]]): Map[CustomersId, Cost] = {
    customerIdWithCosts.view.mapValues(value => Cost("%.2f".format(value.map(_.value).sum).toDouble)).toMap
  }
}
