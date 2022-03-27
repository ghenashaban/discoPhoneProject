package parser

import model._

object CallLogProcess {

  def parseCall(string: String): Either[Error, Call] = string match {
      case value if value.isEmpty => Left(Error("File is empty"))
      case _ => {
        val splitCallLog: List[String] = string.split (" ").map (_.trim).toList
        splitCallLog match {
        case a :: b :: c :: Nil if a.nonEmpty && b.nonEmpty && c.nonEmpty =>
        Right (Call (CustomersId (a), PhoneNumberCalled (b), CallDuration (c) ) )
        case _ => Left (Error ("Error while parsing log: File has wrong format") )
        }
    }
    }

//  def parseCall2(call: String): Either[Error, Call] = call match {
//    case value if value.isEmpty => Left(Error("File is empty"))
//    case _ => {
//      val callArray: Array[String] = call.split(" ").map(_.trim)
//      Try(Call(CustomersId(callArray(0)), PhoneNumberCalled(callArray(1)), CallDuration(callArray(2)))) match {
//        case Success(call) => Right(call)
//        case Failure(_) => Left(Error("Error while parsing log: File has wrong format"))
//      }
//    }
//  }

}
