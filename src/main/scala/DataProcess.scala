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
      val talkee = heeey(bla(parseCalls))
      for (element <- talkee) {
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

  def bla(callLogsList: Either[Error, List[Call]]): Either[Error, Map[CustomersId, Cost]] = {
    callLogsList match {
      case Right(calls) => Right(promotionApplied(calls))
    }
  }

  def promotionApplied(list: List[Call]): Map[CustomersId, Cost]= {
    val teestA: Map[CustomersId, Map[PhoneNumberCalled, List[String]]] = list.groupBy(_.customersId).splitAt(0).map(_.map { case (key, value) => key -> value.groupBy(_.phoneNumberCalled).map { case (key, value) => key -> value.map(_.callDuration.value) } })._2
    val filtered: Map[CustomersId, Map[PhoneNumberCalled, Long]] = allCalls(teestA)
    println(filtered)
    calculateCostNew(filtered)

  }

  // new
  def allCalls(oldCalls: Map[CustomersId, Map[PhoneNumberCalled, List[String]]]) = {
    val sumed = oldCalls map { case (key, value) => key -> sumTheDuration(value) }
    sumed.map { case (key, value) => key -> filterTheDuration(value) }
  }

  // new
  def sumTheDuration(call: Map[PhoneNumberCalled, List[String]]) = {
    call.map { case (key, value) => key -> value.map(value => Duration.between(LocalTime.MIN, LocalTime.parse(value)).getSeconds).sum }
  }

  // new
  def filterTheDuration(value: Map[PhoneNumberCalled, Long]) = {
    val values = value.map { case (_, value) => value }.max
    value.filter(_._2 != values)
  }

  // new
  def calculateCostNew(bla: Map[CustomersId, Map[PhoneNumberCalled, Long]]) = {
    val durationInLong = bla.map { case (key, value) => key -> value.map(_._2) }
    val cost = durationInLong.map { case (key, value) => key -> value.map {
      value => if (value >= 180) Cost(0.05 * value) else Cost(0.03 * value)}}
    val total = cost.map { case (key, value) => key -> Cost(Math.round(value.map(value => value.value).sum / 0.01) * 0.01) }
    total
  }

  // new
  def heeey(value: Either[Error, Map[CustomersId, Cost]]): List[String] = value match {
    case Right(bla) => { bla.map {case (key, value) =>
        Talk(s"Customer ${key.value} has a bill of £${value.value}")
      }.map(_.value).toList
     }
  }

  dataProcess("calls.log")

}

