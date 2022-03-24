package parser

import cats.implicits._
import model._

import scala.io.Source
import scala.util.{Failure, Success, Try}

object DataProcess {


  def dataProcess(fileName: String): Either[Error, List[Call]] = {
    val source = Source.fromResource(fileName).mkString
     if (source.isEmpty) Left(Error("File is empty")) else {
       Try(source) match {
         case Success(_) => {
           val callLogsToList = Source.fromResource(fileName).mkString.split("\\n").map(_.trim).toList
           val parseCalls = callLogsToList.map(parseCall).sequence
           Source.fromResource(fileName).close()
           parseCalls
         }
         case Failure(_) => Left(Error("Error while processing file: File doesn't exist"))
       }
     }
  }

  def parseCall(string: String): Either[Error, Call] = {
    val splitLine: List[String] = string.split(" ").map(_.trim).toList
    splitLine match {
      case a :: b :: c :: Nil if a.nonEmpty && b.nonEmpty && c.nonEmpty =>
        Right(Call(CustomersId(a), PhoneNumberCalled(b), CallDuration(c)))
      case _ => Left(Error("Error while parsing log: File has wrong format"))
    }
  }

}
