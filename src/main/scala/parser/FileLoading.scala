package parser

import cats.implicits.toTraverseOps
import model.{Call, Error}
import parser.CallLogProcess.parseCall
import scala.io.Source
import scala.util.{Failure, Success, Try}

object FileLoading {

  def processCallLog(fileName: String): Either[Error, List[Call]] = {
    lazy val source = Source.fromResource(fileName).mkString
    Try(source) match {
      case Success(_) => {
        val callLogsToList: List[String] = source.split("\\n").map(_.trim).toList
        val parseCalls = callLogsToList.map(parseCall).sequence
        Source.fromResource(fileName).close()
        parseCalls
      }
      case Failure(_) => Left(Error("Error while processing file: File doesn't exist"))
    }
  }

}
