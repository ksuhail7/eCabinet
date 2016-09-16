package fresco.db

import fresco.db.Helpers._
import org.mongodb.scala.bson.collection.immutable.Document
import org.mongodb.scala.{MongoClient, MongoCollection}

/**
  * Created by suhail on 2016-09-12.
  */
object Database {
  val mongoClient = MongoClient()
  val mongoDb = mongoClient.getDatabase("fresco")

  implicit def printResults() = {

  }

  def getStores() = {
    val collection: MongoCollection[Document] = mongoDb.getCollection("stores")
    val doc: Document = Document("_id" -> 0, "name" -> "MongoDB", "type" -> "database",
      "count" -> 1, "info" -> Document("x" -> 203, "y" -> 102))
    // collection.insertOne(doc).results()
    collection.find().printResults()
  }

}

object TestDatabase extends App {
  val stores = Database.getStores()
  println(s"$stores")
}


