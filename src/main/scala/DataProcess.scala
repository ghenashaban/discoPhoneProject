import cats.implicits._
import scala.io.Source
import java.time.{Duration, LocalTime}

object DataProcess extends App {

  def dataProcess(fileName: String): Either[Error, List[Call]] = {
    Source.getClass.getResource(s"/$fileName") match {
      case null => Left(Error("file not found"))
      case _ if Source.fromResource(fileName).nonEmpty => {
        val callLogsToList: List[String] = Source.fromResource(fileName).mkString.split("\\n").map(_.trim).toList
        val parseCalls: Either[Error, List[Call]] = callLogsToList.map(parseCall).sequence
        printOutput(parseCalls)
        parseCalls
      }
      case _ => Left(Error("Empty File"))
    }
  }

  def printOutput(parseCalls: Either[Error, List[Call]]) = parseCalls match {
    case Right(_) =>
      val talk: List[Talk] = toTalk(customerIdGroupedByTotalCost(parseCalls))
      for (element <- talk) {
        println(element.value)
      }
    case Left(_) => println("something went wrong")
  }


  def parseCall(string: String): Either[Error, Call] = {
    val splitLine: List[String] = string.split(" ").map(_.trim).toList
    splitLine match {
      case a :: b :: c :: Nil if a.nonEmpty && b.nonEmpty && c.nonEmpty =>
        Right(Call(CustomersId(a), PhoneNumberCalled(b), CallDuration(c)))
      case _ => Left(Error("Error in ParseCall"))
    }
  }

  def customerIdGroupedByTotalCost(callLogsList: Either[Error, List[Call]]): List[(CustomersId, Cost)] = {
    callLogsList match {
      case Right(calls) => {
        val filteredCalls: List[Call] = promotionApplied(calls)
        val joinCallWithCost: List[(CustomersId, Cost)] = joinCustomerWithCost(filteredCalls, listOfCosts(filteredCalls, List()))
        val customerGroupedByCosts: Map[CustomersId, List[Cost]] = joinCallWithCost.groupBy(_._1).map {
          case (key, value) => key -> value.map(value => Cost(value._2.value))
        }
        calculateTotalCost(customerGroupedByCosts)
      }
    }
  }

  def calculateTotalCost(customerGroupedByCosts: Map[CustomersId, List[Cost]]): List[(CustomersId, Cost)] = {
    val customerGroupedByTotalCost: List[(CustomersId, Cost)] = customerGroupedByCosts.toList.map {
      case (key, value) => key -> Cost(Math.round(value.map(_.value).sum / 0.01)*0.01)
    }
    customerGroupedByTotalCost
  }

  def promotionApplied(list: List[Call]): List[Call] = {
    val longDuration = list.map(value => value.callDuration.value).max
    val filteredCalls = list.filter(_.callDuration.value != longDuration)
    filteredCalls
  }

  def listOfCosts(listOfCalls: List[Call], listOfCalculatedCost: List[Cost]): List[Cost] = listOfCalls match {
    case a :: aq => {
      val callCost: Cost = calculateCost(a)
      val total: List[Cost] = List(callCost) ++ listOfCosts(aq, listOfCalculatedCost)
      total
    }
    case Nil => listOfCalculatedCost
  }

  def calculateCost(listOfCallDuration: Call): Cost = {
    val durationInLong: Long = Duration.between(LocalTime.MIN, LocalTime.parse(listOfCallDuration.callDuration.value)).getSeconds
    val cost = if (durationInLong >= 180) Cost(0.05 * durationInLong) else Cost(0.03 * durationInLong)
    cost

  }

  def joinCustomerWithCost(filteredCalls: List[Call], listOfCallCostPerCustomer: List[Cost]): List[(CustomersId, Cost)] = {
    val customerIdJoinedWithCost = filteredCalls.map(_.customersId) zip listOfCallCostPerCustomer
    customerIdJoinedWithCost
  }


  def toTalk(listOfCustomerWithCost: List[(CustomersId, Cost)], talks: List[Talk] = List()): List[Talk] = listOfCustomerWithCost match {
    case a :: aq => {
      val talk: Talk = Talk(s"${a._1} has a bill of £${a._2.value}")
      val result = toTalk(aq, List(talk) ++ talks)
      result
    }
    case Nil => talks
  }

  dataProcess("calls.log")

}

