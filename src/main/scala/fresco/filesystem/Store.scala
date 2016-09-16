package fresco.filesystem

import java.io.File
import java.nio.file.{DirectoryStream, Files, Path, Paths}
import java.text.SimpleDateFormat
import java.util.Date

import fresco.util.{FolderLock, Lock, LockableResource}

/**
  * Created by suhail on 2016-09-08.
  */
class Store(val id: String, val repository: Repository) extends LockableResource {
  lazy val storeRootPath = Paths.get(repository.folder, "store")
  lazy val storeLocation = storeRootPath.resolve(id)
  lazy val storeLock = new StoreLock(this)
  var initialized:Boolean = false

  override def getLock(): Lock = storeLock
  val documentBuilder = Document.buildDocument(this)

  initialize()

  def getDocument(docId: String) = {}
  def createDocument(document: Document) = {
    import Document._
    val pathToDocument = storeLocation.resolve(simpleDateFormat.format(new Date())).resolve(generateDocId)
    println(s"path to document: $pathToDocument")

  }


  def updateDocument(document: Document) = {
    ???
  }

  def initialize(): Unit = {
    import LockableResource._
    if(Files.notExists(storeRootPath)) Files.createDirectory(storeRootPath)
    this.logger.debug(s"root: $storeRootPath, location: $storeLocation")
    if (Files.notExists(storeLocation)) Files.createDirectory(storeLocation)
    withLockOn(this) { store =>
      if (!initialized) {
        val metaInf = storeLocation.resolve("meta.inf")
        if(Files.notExists(metaInf)) Files.createFile(metaInf)
      }
    }
  }
}

object StoreFactory {
  val storeCache = scala.collection.mutable.Map[String, Store]()
  var repository: Repository = null
  def apply(repo: Repository) = {
    repository = repo
    initializeStores()
  }

  val directoryFilter = new DirectoryStream.Filter[Path] {
    override def accept(entry: Path): Boolean = {
      return Files.isDirectory(entry)
    }
  }

  def initializeStores(): Unit = {
    import LockableResource._
    withLockOn(repository) { repo =>
      val storeRootPath = Paths.get(repo.folder, "store")
      storeRootPath.toFile().listFiles().filter(_.isDirectory).foreach{ dir =>
        storeCache.put(dir.getName, new Store(dir.getName, repository))
      }

      }
    }
  def getStore(id: String) = storeCache.getOrElseUpdate(id, new Store(id, repository))

}
class StoreLock(store: => Store) extends FolderLock(store.storeLocation.toString)

object StoreMain extends App {
  val repo = Repository("/Users/suhail/tmp/fresco")
  val store = new Store("0", repo)
  val dir = new File("/Users/suhail/tmp")
  dir.listFiles().filter(p => p.getName.startsWith("yolinux")).foreach(file => store.buildDocument(file))
}
