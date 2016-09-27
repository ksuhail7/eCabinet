package fresco.filesystem

import java.io.File
import java.nio.file.{Files, Paths}
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.UUID

import com.google.gson.Gson
import fresco.logging.Logging
import org.apache.tika.Tika

import scala.io.Source

/**
  * Created by suhail on 2016-09-10.
  */

case class Document(storeId: String, docId: String, versions: scala.collection.mutable.Map[Int, DocumentObject] = scala.collection.mutable.Map()) {
  val gson = new Gson()

  val documentLocator = Paths.get(docId.substring(0, 2), docId.substring(2, 4), docId.substring(4)).toString

  override def toString() = {
    gson.toJson(Map("storeId" -> storeId, "docId" -> docId, "versions" -> versions))
  }
}

case class DocumentObject(val fileName: String, val fileSizeInBytes: Long, val mimeType: String, val modTime: Long, val sha1Cksum: String) extends Logging {
  require(fileSizeInBytes >= 0)
  val gson = new Gson()

  val objectLocator = Paths.get(sha1Cksum.substring(0, 2), sha1Cksum.substring(2, 4), sha1Cksum.substring(4)).toString

  override def toString = gson.toJson(Map("fileName" -> fileName, "size" -> fileSizeInBytes, "mimeType" -> mimeType, "modificationTime" -> modTime, "sha1Checksum" -> sha1Cksum))
}

object Document extends Logging {

  val tika = new Tika()

  def calculateSha1Checksum(file: File): String = {
    val source: Source = Source.fromFile(file, "ISO-8859-1")
    try {
      val digest = MessageDigest.getInstance("SHA-1");
      digest.digest(source.map {
        _.toByte
      }.toArray).map("%02x".format(_)).mkString
    } finally {
      if (source != null) source.close()
    }
  }

  def apply(storeId: String, docId: String) : Document = {
    //TODO: Fetch the document from database
    //if the document does not exists in database, create a new one
    new Document(storeId, docId)
  }

  private def documentCreationHelper(store: Store, docId: String, file: File, version: Int = 0) = {
    val doc = Document(store.id, docId)
    if(doc.versions.contains(version)) throw new Exception(s"version $version already exists for docId: $docId, storeId: ${store.id}")

    val name = file.getName
    val fileSize = file.length()
    val mimeType = getMimeType(file)
    val modTime = Files.getLastModifiedTime(file.toPath).toMillis
    val sha1 = calculateSha1Checksum(file)

    val docObject = new DocumentObject(fileName = name, fileSizeInBytes = fileSize, mimeType = mimeType, modTime = modTime, sha1Cksum = sha1)

    doc.versions.update(version, docObject)
    (doc, docObject)
  }

  def createDocument(store: Store, file: File): Document = {
    val id = generateDocId
    val ver = 0


    val (document, documentObject) = documentCreationHelper(store, id, file, ver)
    val objectLocator = documentObject.objectLocator
    val documentLocator = document.documentLocator
    val location = store.getPreferredLocation()
    val fileLocation = Paths.get(location, "objects").resolve(objectLocator)
    val manifestLocation = Paths.get(location, "store", store.id).resolve(documentLocator)

    logger.debug(s"file location: $fileLocation, manifest location: $manifestLocation")


    val parentObjectDir = fileLocation.getParent
    if (Files.notExists(parentObjectDir)) Files.createDirectories(parentObjectDir)

    if (Files.notExists(fileLocation)) Files.copy(file.toPath, fileLocation)
    if (Files.notExists(manifestLocation)) Files.createDirectories(manifestLocation)
    //create versions
    val thisVersion = manifestLocation.resolve(s"ver.$ver")
    val currentVersion = manifestLocation.resolve("current")
    val writer = Files.newBufferedWriter(Files.createFile(thisVersion))
    writer.write(document.toString)
    writer.close()
    Files.createSymbolicLink(currentVersion, thisVersion)
    logger.debug(document.toString)
    document
  }

  def updateDocument(document: Document) = {

  }


  def deleteDocument(document: Document) = {

  }

  def generateDocId: String = UUID.randomUUID().toString()

  def getMimeType(file: File): String = tika.detect(file)
}

object DocumentTest extends App {
  val repo = Repository("fresco")
  val store = new Store("0", repo)
  val files = new File("/Users/suhail/tmp").listFiles()
  files.toList.filter(_.isFile).par.foreach(file => {
    //println(s"processing file: $file")
    Document.createDocument(store, file)
  })
}