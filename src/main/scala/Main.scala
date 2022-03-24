import parser.DataProcess._
import service.CustomerBill.getCustomersTotalBill

object Main extends App {

  val processLogFile = dataProcess("calls.log")

  processLogFile match {
    case Right(calls) =>
      val customerWithTotalCost = getCustomersTotalBill(calls)
      customerWithTotalCost.foreach { case (key, value) =>
        println(s"Customer ${key.value} has a bill of £${value.value}")
      }
    case Left(error) => println(error.message)
  }
}