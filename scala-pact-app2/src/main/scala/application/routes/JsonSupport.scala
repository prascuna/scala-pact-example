package application.routes

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import application.models.MyData
import spray.json._


trait JsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val myDataFormatter = jsonFormat2(MyData)
}
