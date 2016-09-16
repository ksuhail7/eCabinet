package fresco.filesystem

import java.io.File
import java.nio.file.Files
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.{Date, UUID}

import com.google.gson.Gson
import fresco.logging.Logging
import net.liftweb.json.DefaultFormats
import net.liftweb.json.Serialization.write
import org.apache.tika.Tika

import scala.io.Source

/**
  * Created by suhail on 2016-09-10.
  */
case class Document(fileName: String, fileSizeInBytes: Long, mimeType: String, modTime: Long, storeId: String) extends Logging{
  require(fileSizeInBytes >= 0)
  private var id: String = null
  private var idPrefix: String = null
  private var physicalLocation: String = null
  val gson = new Gson()
  override def toString = gson.toJson(this)
}

object Document extends Logging {

  val tika = new Tika()
  val simpleDateFormat = new SimpleDateFormat("yyyyMM/dd")

  def calculateSha1Checksum(file: File): String = {
    var source: Source = null
    try {
      val digest = MessageDigest.getInstance("SHA-1");
      source = Source.fromFile(file)
      digest.digest(source.map {
        _.toByte
      }.toArray).map("%02x".format(_)).mkString
    } finally {
      if (source != null) source.close()
    }
  }

  def buildDocument(store: Store) (file: File): Document = {
    val name = file.getName
    val fileSize = file.length()
    val mimeType = getMimeType(file)
    val modTime = Files.getLastModifiedTime(file.toPath).toMillis
    val id = generateDocId
    val document = new Document(fileName = name, fileSizeInBytes = fileSize, mimeType = mimeType, modTime = modTime, storeId = store.id)
    logger.debug(document.toString)
    document

  }

  def generateDocId: String = UUID.randomUUID().toString()

  def getMimeType(file: File): String = tika.detect(file)
}