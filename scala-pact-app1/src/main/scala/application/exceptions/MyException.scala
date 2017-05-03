package application.exceptions

import akka.http.scaladsl.model.Uri

sealed abstract class MyException(message: String) extends Exception(message)

case object DuplicatedEntryException extends MyException("An entry with the same id already exists in the database")

case class DuplicatedEntryException(uri: Uri) extends MyException("An entry with the same id already exists in the database")

case object LocationNotFoundException extends MyException("Couldn't find the Location Header")