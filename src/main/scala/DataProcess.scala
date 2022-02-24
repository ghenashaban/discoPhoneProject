import cats.implicits._
import scala.io.Source
import java.time.{Duration, LocalTime}

object DataProcess extends App {

  def dataProcess(fileName: String): Either[Error, List[Call]] = {
    Source.getClass.getResource(s"/$fileName") match {
      case null => Left(Error("file not found"))
      case _ if Source.fromResource(fileName).nonEmpty => {
        val callsLogsToList: List[String] = Source.fromResource(fileName).mkString.split("\\n").map(_.trim).toList
        val parseCalls: Either[Error, List[Call]] = callsLogsToList.map(parseCall).sequence
        callDurationsGroupedByCustomerId(parseCalls)
        parseCalls
      }
      case _ => Left(Error("Empty File"))
    }
  }

  def parseCall(string: String): Either[Error, Call] = {
    val listOfCallLogs: List[String] = string.split(" ").map(_.trim).toList
    listOfCallLogs match {
      case a :: b :: c :: Nil if a.nonEmpty && b.nonEmpty && c.nonEmpty =>
        Right(Call(CustomersId(a), PhoneNumberCalled(b), CallDuration(c)))
      case _ => Left(Error("Error in ParseCall"))
    }
  }

  def callDurationsGroupedByCustomerId(callLogsList: Either[Error, List[Call]]): Either[Error, Map[CustomersId, List[LocalTime]]] = {
    callLogsList match {
      case Right(calls) => {
        val test: Map[CustomersId, List[CallDuration]] = calls.groupBy(_.customersId).map {
          case (key, value) => key -> value.map(_.callDuration)
        }
        val callDurationMappedByIdToLocalTime: Map[CustomersId, List[LocalTime]] = test.map { case (key, value) => key -> value.map(value => LocalTime.parse(value.value)) }
        filterCallsForPromotion(callDurationMappedByIdToLocalTime)
        Right(callDurationMappedByIdToLocalTime)
      }
      case Left(Error(_)) => Left(Error("Error in callDurationsGroupedByCustomerId"))
    }
  }

  def filterCallsForPromotion(callMappedById: Map[CustomersId, List[LocalTime]]): List[List[CallDurationInSeconds]] = {
    val localTimeCallDurationList: List[List[LocalTime]] = callMappedById.values.toList
    val callDurationToSecondsList: List[List[CallDurationInSeconds]] = localTimeCallDurationList.map(value => value.map(value => CallDurationInSeconds(Duration.between(LocalTime.MIN, value).getSeconds)))
    val filteredCalls: List[List[Long]] = callDurationToSecondsList.map(value => value.map(value => value.value)).map(value => value.filter(_ != value.max))
    val filteredCallsDurationInSeconds: List[List[CallDurationInSeconds]] = filteredCalls.map(value => value.map(value => CallDurationInSeconds(value)))
    joinCustomerWithCost(callMappedById.keys.toList, listOfCallCostsPerCustomer(filteredCallsDurationInSeconds, List()))
    filteredCallsDurationInSeconds
  }

  def listOfCallCostsPerCustomer(listOfCallDuration: List[List[CallDurationInSeconds]], listOfCalculatedCost: List[Cost]): List[Cost] = listOfCallDuration match {
    case a :: aq => {
      val callCost: Cost = calculateCost(a)
      val total: List[Cost] = List(callCost) ++ listOfCallCostsPerCustomer(aq, listOfCalculatedCost)
      total
    }
    case Nil => listOfCalculatedCost
  }

  def calculateCost(listOfCallDuration: List[CallDurationInSeconds], totalCost: Cost = Cost(0)): Cost = listOfCallDuration match {
    case a :: aq => {
      val cost = if (a.value >= 180) Cost(0.05 * a.value) else Cost(0.03 * a.value)
      calculateCost(aq, Cost(totalCost.value + cost.value))
    }
    case Nil => totalCost
  }

  def joinCustomerWithCost(customerIdMappedWithDuration: List[CustomersId], listOfCallCostPerCustomer: List[Cost]): List[(CustomersId, Cost)] = {
    val customerIdJoinedWithCost: List[(CustomersId, Cost)] = customerIdMappedWithDuration zip listOfCallCostPerCustomer
    toString(customerIdJoinedWithCost)
    customerIdJoinedWithCost
  }

  def toString(listOfCustomerWithCost: List[(CustomersId, Cost)]): Talk = listOfCustomerWithCost match {
    case a :: aq => {
      toString(aq)
      println(Talk(s"${a._1} has a bill of £${a._2.value}").value)
      Talk(s"${a._1} has a bill of £${a._2.value}")
    }
    case Nil => Talk("")
  }

  dataProcess("calls.log")
}

