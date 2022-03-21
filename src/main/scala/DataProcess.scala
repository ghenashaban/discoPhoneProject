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
        Source.fromResource(fileName).close()
        parseCalls
      }
      case _ => Left(Error("Empty File"))
    }
  }

  def printOutput(parseCalls: Either[Error, List[Call]]) = parseCalls match {
    case Right(_) =>
      val elements = toString(promotionApplied(parseCalls))
      for (element <- elements) {
        println(element)
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

  // create a function than does the mapping and pass only RIGHT

  def customerIdMapped = ???

  def promotionApplied(list: Either[Error, List[Call]]): Map[CustomersId, Cost] = list match {
    case Right(call) => {
      val customerIdMappedByCalls = call.groupBy(_.customersId).splitAt(0).map(_.map {
        case (key, value) => key -> value.groupBy(_.phoneNumberCalled).map {
          case (key, value) => key -> value.map(_.callDuration.value)
        } })._2
      val filteredCalls = getFilteredCalls(customerIdMappedByCalls)
      calculateCost(filteredCalls)
    }
  }

  def getFilteredCalls(calls: Map[CustomersId, Map[PhoneNumberCalled, List[String]]]): Map[CustomersId, Map[PhoneNumberCalled, Long]] = {
    val totalDurationPerPhoneNumberCalled = calls map {
      case (key, value) => key -> CalculateTotalDurationPerPhoneNumberCalled(value) }
    totalDurationPerPhoneNumberCalled.map { case (key, value) => key -> filterDuration(value) }
  }

  private def CalculateTotalDurationPerPhoneNumberCalled(call: Map[PhoneNumberCalled, List[String]]): Map[PhoneNumberCalled, Long] = {
    call.map {
      case (key, value) => key -> value.map(value => Duration.between(LocalTime.MIN, LocalTime.parse(value)).getSeconds).sum
    }
  }


  private def filterDuration(value: Map[PhoneNumberCalled, Long]): Map[PhoneNumberCalled, Long] = {
    val values = value.map { case (_, value) => value }.max
    value.filter(_._2 != values)
  }

  def calculateCost(filteredCalls: Map[CustomersId, Map[PhoneNumberCalled, Long]]): Map[CustomersId, Cost] = {
    val getDuration = filteredCalls.map { case (key, value) => key -> value.values }
    val cost = getDuration.map {
      case (key, value) => key -> value.map {
      value => if (value >= 180) Cost(0.05 * value) else Cost(0.03 * value)
    }
    }
    val total = cost.map {
      case (key, value) => key -> Cost(Math.round(value.map(value => value.value).sum / 0.01) * 0.01)
    }
    total
  }

  def toString(value: Map[CustomersId, Cost]): List[String] = {
    value.map { case (key, value) =>
      Talk(s"Customer ${key.value} has a bill of £${value.value}")
    }.map(_.value).toList
  }

  dataProcess("calls.log")

}

