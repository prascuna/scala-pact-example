package application.repositories

import java.sql.SQLIntegrityConstraintViolationException

import application.exceptions.{DuplicatedEntryException, MyException}
import application.models.MyData

import scala.concurrent.{ExecutionContext, Future}

trait MyRepository {
  def read(id: Int): Future[Option[MyData]]

  def write(data: MyData): Future[Either[MyException, MyData]]

  def truncate: Future[Unit]
}

object MyRepository {
  def apply()(implicit ec: ExecutionContext): MyRepository = new SlickMyRepository()

}

class SlickMyRepository(implicit ec: ExecutionContext) extends MyRepository {

  import SlickMyRepository._
  import profile.api._


  override def read(id: Int): Future[Option[MyData]] =
    db.run {
      myData.filter(_.id === id).result.headOption
    }

  override def write(data: MyData): Future[Either[MyException, MyData]] =
    db.run {
      myData returning myData.map(_.id) forceInsert data
    }.map { dbId =>
      Right(data.copy(id = Some(dbId)))
    }.recover {
      case _: SQLIntegrityConstraintViolationException => Left(DuplicatedEntryException)
    }

  override def truncate: Future[Unit] =
    db.run {
      myData.schema.truncate
    }
}

object SlickMyRepository {

  val profile = slick.jdbc.MySQLProfile

  import profile.api._

  class MyDataTable(tag: Tag) extends Table[MyData](tag, "mydata") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

    def name = column[String]("name")

    def * = (id.?, name) <> (MyData.tupled, MyData.unapply)

  }

  private val myData = TableQuery[MyDataTable]

  private val db = Database.forConfig("sampledb")

}


//class InMemoryMyRepository extends MyRepository {
//  val fakeDb = mutable.Map[Long, MyData]()
//
//  override def read(id: Long): Option[MyData] =
//    fakeDb.get(id)
//
//  override def write(data: MyData): Either[MyRepositoryException, Unit] =
//    if (fakeDb.contains(data.id)) {
//      Left(DuplicatedEntryException)
//    } else {
//      fakeDb.put(data.id, data)
//      Right(())
//    }
//}