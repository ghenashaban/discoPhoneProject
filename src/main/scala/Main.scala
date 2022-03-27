import parser.FileLoading.processCallLog
import service.CalculateBill.getCustomersTotalBill

object Main extends App {

  val processLogFile = processCallLog("calls.log")

  processLogFile match {
    case Right(calls) =>
      val customerWithTotalCost = getCustomersTotalBill(calls, promotionApplied = true)
      customerWithTotalCost.foreach { case (key, value) =>
        println(s"Customer ${key.value} has a bill of £${value.value}")
      }
    case Left(error) => println(error.message)
  }

}