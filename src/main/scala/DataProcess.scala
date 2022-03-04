import cats.implicits._

import java.io.FileNotFoundException
import java.time.{Duration, LocalTime}
import scala.io.Source
import scala.util.{Failure, Success, Try}

object DataProcess extends App {

  ///////////////////- General -////////////////////
  // This file is quite big, I think it would have benefited from splitting it up into 3 classes:
  // 1- A CallLogReader class to read the file from the resources and convert it into a list of strings
  // 2- A CallProcessing class to apply the business logic i.e promotions + helper classes
  // 3- A small and simple application class to instantiate the 2 classes and run the programme

  // The methods could have been ordered by usage, plus a lot of the helper method can be private

  ///////////////////- dataProcess -////////////////////
  //I would give this a more specific name like processCallLog
  //You can use fromResource instead of getResource (fromResource will locate the file URI)
  //You can use .getLines instead of splitting on each new line

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

  def processCallLog(fileName: String): List[Talk] = {
    val src = Source.fromResource(fileName)
    val callLogsToList = Try(src.getLines) match {
      case Success(lines) => lines.toList
      case Failure(exception) => throw new FileNotFoundException(s"Cannot find file $fileName \n $exception")
    }
    src.close()

    if (callLogsToList.isEmpty) {
      throw new Exception(s"$fileName is empty")
    } else {
      val parsedCalls = callLogsToList.map(parseCall2)
      printOutput2(parsedCalls)
    }
  }

  ///////////////////- printOutput -////////////////////
  //I don't think this needs the error passing in
  //I think returning something from this could be valuable
  def printOutput(parseCalls: Either[Error, List[Call]]) = parseCalls match {
    case Right(_) =>
      val talk: List[Talk] = toTalk(customerIdGroupedByTotalCost(parseCalls))
      for (element <- talk) {
        println(element.value)
      }
    case Left(_) => println("something went wrong")
  }

  def printOutput2(parsedCalls: List[Call]): List[Talk] = {
    val groupByCost = customerIdGroupedByTotalCost2(parsedCalls)
    val talkPrice = toTalk(groupByCost)
    print(talkPrice)
    talkPrice
  }

  ///////////////////- parseCall -////////////////////
  //This works well
  def parseCall(string: String): Either[Error, Call] = {
    val splitLine: List[String] = string.split(" ").map(_.trim).toList
    splitLine match {
      case a :: b :: c :: Nil if a.nonEmpty && b.nonEmpty && c.nonEmpty =>
        Right(Call(CustomersId(a), PhoneNumberCalled(b), CallDuration(c)))
      case _ => Left(Error("Error in ParseCall"))
    }
  }

  //If you used an array here you could split the call by the index
  def parseCall2(call: String): Call = {
    val callArray: Array[String] = call.split(" ").map(_.trim)

    Try(Call(CustomersId(callArray(0)), PhoneNumberCalled(callArray(1)), CallDuration(callArray(2)))) match {
      case Success(call) => call
      case Failure(er) =>
        throw new ArrayIndexOutOfBoundsException(s"Cannot convert string ${callArray.mkString("Array(", ", ", ")")}")
    }
  }

///////////////////- customerIdGroupedByTotalCost -////////////////////
//I wouldn't pass down the error to this function
  def customerIdGroupedByTotalCost(callLogsList: Either[Error, List[Call]]): List[(CustomersId, Cost)] = {
    callLogsList match {
      case Right(calls) => {
        val filteredCalls: List[Call] = promotionApplied(calls)
        //for joinCustomerWithTotalCost you are passing in the
        val joinCallWithCost: List[(CustomersId, Cost)] = joinCustomerWithTotalCost(filteredCalls, listOfCosts(filteredCalls, List()))
        joinCallWithCost
      }
    }
  }

def customerIdGroupedByTotalCost2(callLogsList:List[Call]): List[(CustomersId, Cost)] = {
      val filteredCalls: List[Call] = promotionApplied(callLogsList)
      val joinCallWithCost: List[(CustomersId, Cost)] = joinCustomerWithTotalCost2(filteredCalls)
      joinCallWithCost
}

  ///////////////////- promotionApplied -////////////////////
//This works well
  def promotionApplied(list: List[Call]): List[Call] = {
    val longDuration = list.map(value => value.callDuration.value).max
    val filteredCalls = list.filter(_.callDuration.value != longDuration)
    filteredCalls
  }

  ///////////////////- listOfCosts -////////////////////
  //I think that this could be simplified as you are passing an empty
  def listOfCosts(listOfCalls: List[Call], listOfCalculatedCost: List[Cost]): List[Cost] = listOfCalls match {
    case a :: aq => {
      val callCost: Cost = calculateCost(a)
      val total: List[Cost] = List(callCost) ++ listOfCosts(aq, listOfCalculatedCost)
      total
    }
    case Nil => listOfCalculatedCost
  }

  def listOfCosts2(listOfCalls: List[Call]): List[Cost] = {
    listOfCalls.map(call => calculateCost(call))
  }

  def calculateCost(listOfCallDuration: Call): Cost = {
    val durationInLong: Long = Duration.between(LocalTime.MIN, LocalTime.parse(listOfCallDuration.callDuration.value)).getSeconds
    val cost = if (durationInLong >= 180) Cost(0.05 * durationInLong) else Cost(0.03 * durationInLong)
    cost

  }

  def calculateTotalCost(customerGroupedByCosts: Map[CustomersId, List[Cost]]): List[(CustomersId, Cost)] = {
    val customerGroupedByTotalCost: List[(CustomersId, Cost)] = customerGroupedByCosts.toList.map {
      case (key, value) => key -> Cost(Math.round(value.map(_.value).sum / 0.01) * 0.01)
    }
    customerGroupedByTotalCost
  }

  ///////////////////- joinCustomerWithTotalCost -////////////////////

  def joinCustomerWithTotalCost(filteredCalls: List[Call], listOfCallCostPerCustomer: List[Cost]): List[(CustomersId, Cost)] = {
    val customerIdJoinedWithCost = filteredCalls.map(_.customersId) zip listOfCallCostPerCustomer
    val customerGroupedByCosts: Map[CustomersId, List[Cost]] = customerIdJoinedWithCost.groupBy(_._1).map {
      case (key, value) => key -> value.map(value => Cost(value._2.value))
    }
    calculateTotalCost(customerGroupedByCosts)
  }

  def toTalk(listOfCustomerWithCost: List[(CustomersId, Cost)], talks: List[Talk] = List()): List[Talk] = listOfCustomerWithCost match {
    case a :: aq => {
      val talk: Talk = Talk(s"${a._1} has a bill of £${a._2.value}")
      val result = toTalk(aq, List(talk) ++ talks)
      result
    }
    case Nil => talks
  }

  processCallLog("calls.log")

}

