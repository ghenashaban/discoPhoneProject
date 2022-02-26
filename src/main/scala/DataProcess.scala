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
       printAll(parseCalls)
        parseCalls
      }
      case _ => Left(Error("Empty File"))
    }
  }

  def printAll(parseCalls :Either[Error, List[Call]] ) = {
    val talk = toTalk(customerIdGroupedByTotalCost(parseCalls))
    for (element <- talk.map(_.map(_.value)).getOrElse(None)) {
      println(element)
    }
  }

  def parseCall(string: String): Either[Error, Call] = {
    val splitLine: List[String] = string.split(" ").map(_.trim).toList
    splitLine match {
      case a :: b :: c :: Nil if a.nonEmpty && b.nonEmpty && c.nonEmpty =>
        Right(Call(CustomersId(a), PhoneNumberCalled(b), CallDuration(c)))
      case _ => Left(Error("Error in ParseCall"))
    }
  }

  def customerIdGroupedByTotalCost(callLogsList: Either[Error, List[Call]]): Either[Error, List[(CustomersId, Cost)]] = {
    callLogsList match {
      case Right(calls) => {
        val test: Map[CustomersId, List[CallDuration]] = calls.groupBy(_.customersId).map {
          case (key, value) => key -> value.map(_.callDuration)
        }
        val callDurationMappedByIdToLocalTime: Map[CustomersId, List[LocalTime]] = test.map { case (key, value) => key -> value.map(value => LocalTime.parse(value.value)) }
        println(callDurationMappedByIdToLocalTime)
        Right(filterCallsForPromotion(callDurationMappedByIdToLocalTime))
      }
      case Left(Error(_)) => Left(Error("Error in customerIdGroupedByTotalCost"))
    }
  }

  def filterCallsForPromotion(callMappedById: Map[CustomersId, List[LocalTime]]): List[(CustomersId, Cost)] = {
    val localTimeCallDurationList: List[List[LocalTime]] = callMappedById.values.toList
    val callDurationToSecondsList: List[List[Long]] = localTimeCallDurationList.map(value => value.map(value => Duration.between(LocalTime.MIN, value).getSeconds))
    println("call duration" + callDurationToSecondsList)
//    val filteredCalls: List[List[Long]] = callDurationToSecondsList.map(value => value.map(value => value.value)).map(value => value.filter(_ != value.max))
    val filteredCalls: List[List[Long]] = callDurationToSecondsList.map(value => value.filter(_ != value.max))
   println(filteredCalls)
    val filteredCallsDurationInSeconds: List[List[CallDurationInSeconds]] = filteredCalls.map(value => value.map(value => CallDurationInSeconds(value)))
    println(filteredCallsDurationInSeconds)
    joinCustomerWithCost(callMappedById.keys.toList, listOfCallCostsPerCustomer(filteredCalls, List()))
  }

  def listOfCallCostsPerCustomer(listOfCallDuration: List[List[Long]], listOfCalculatedCost: List[Cost]): List[Cost] = listOfCallDuration match {
    case a :: aq => {
      val callCost: Cost = calculateCost(a)
      val total: List[Cost] = List(callCost) ++ listOfCallCostsPerCustomer(aq, listOfCalculatedCost)
      total
    }
    case Nil => listOfCalculatedCost
  }

  def calculateCost(listOfCallDuration: List[Long], totalCost: Cost = Cost(0)): Cost = listOfCallDuration match {
    case a :: aq => {
      val cost = if (a >= 180) Cost(0.05 * a) else Cost(0.03 * a)
      calculateCost(aq, Cost(totalCost.value + cost.value))
    }
    case Nil => totalCost
  }

  def joinCustomerWithCost(customerIdMappedWithDuration: List[CustomersId], listOfCallCostPerCustomer: List[Cost]): List[(CustomersId, Cost)] = {
    val customerIdJoinedWithCost: List[(CustomersId, Cost)] = customerIdMappedWithDuration zip listOfCallCostPerCustomer
    customerIdJoinedWithCost
  }

  def toTalk(listOfCustomerWithCost: Either[Error, List[(CustomersId, Cost)]], talks: List[Talk] = List()): Either[Error, List[Talk]] = listOfCustomerWithCost match {
    case Right(a :: aq) => {
      val talk: Talk = Talk(s"${a._1} has a bill of £${a._2.value}")
      val result = toTalk(Right(aq), List(talk) ++ talks)
      result
    }
    case Right(Nil) => Right(talks)
    case _ => Left(Error("Some thing went wrong"))
  }

  dataProcess("simple-calls.log")
}

