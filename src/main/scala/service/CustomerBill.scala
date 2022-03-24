package service

import config.config._
import model.{Call, CallDuration, CallWithCost, Cost, CustomersId, Error, PhoneNumberCalled}
import java.time.{Duration, LocalTime}
import cats.implicits._

object CustomerBill {

  def calculateCost(call: Call): CallWithCost = {
    val callDuration: Double = call.callDuration.value.toDouble
    val cost: Cost = if (callDuration >= threeMinutesInSeconds) {
      Cost((callDuration - threeMinutesInSeconds) * callsOver3minCost +
        threeMinutesInSeconds * callsUnder3minCost)
    } else Cost(callsUnder3minCost * callDuration)
    CallWithCost(call.customersId, call.phoneNumberCalled, call.callDuration, cost)

  }
  def getCustomersTotalBill(parseCalls: List[Call]): Map[CustomersId, Cost] =  {
      val groupCustomer = groupCustomerWithCalls(parseCalls.map(getCallCost))
      getFilteredCalls(groupCustomer)
  }

  def getCallCost(call: Call): CallWithCost = {
    val makeSeconds = Duration.between(LocalTime.MIN, LocalTime.parse(call.callDuration.value)).getSeconds
    val callInSeconds = call.copy(call.customersId, call.phoneNumberCalled, CallDuration(makeSeconds.toString))
    calculateCost(callInSeconds)
  }

  def groupCustomerWithCalls(list: List[CallWithCost]): Map[CustomersId, Map[PhoneNumberCalled, Cost]] = {
    list.groupBy(_.customersId).splitAt(0).map(_.map {
      case (key, value) => key -> value.groupBy(_.phoneNumberCalled).map {
        case (key, value) => key -> Cost(value.map(_.cost.value).sum)
      }
    })._2
  }

  def getFilteredCalls(grouped: Map[CustomersId, Map[PhoneNumberCalled, Cost]]): Map[CustomersId, Cost] = {
    val customerWithListOfCosts: Map[CustomersId, List[Double]] = grouped.map { case (key, value) => key -> value.map {
      case (_, value) => value.value
    }.toList.sorted.reverse.tail
    }
    getTotalCost(customerWithListOfCosts)

  }
  def getTotalCost(filteredCalls: Map[CustomersId, List[Double]]): Map[CustomersId, Cost] = {
    filteredCalls.map {
      case (key, value) => key -> Cost("%.2f".format(value.sum).toDouble)
    }
  }
}
