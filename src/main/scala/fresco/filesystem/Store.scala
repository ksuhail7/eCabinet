package fresco.filesystem

import java.nio.file.{Files, Paths}

import fresco.util.{Lock, LockableResource, MultiFolderLock}

/**
  * Created by suhail on 2016-09-08.
  */
class Store(val id: String, val repository: Repository) extends LockableResource {
  def storeLocations(): Seq[String] = {
    repository.fileSystems.filter(_.isActive).map(_.path).map(Paths.get(_, repository.id).toString).toList
  }

  lazy val storeLock = new StoreLock(storeLocations())

  var initialized: Boolean = false

  override def getLock(): Lock = storeLock

  initialize()


  def initialize(): Unit = {

    import LockableResource.withLockOn

    withLockOn(this) { store => {
      val storeLocations = store.storeLocations()
      val storeId = store.id
      val storePaths = storeLocations.map(Paths.get(_, "store", storeId))
      //create missing store directories
      storePaths.filter(Files.notExists(_)).par.foreach(path => Files.createDirectories(path))

      //create meta inf files
      val storeMetaInf = s"{storeid: ${store.id}}" //TODO: populate with manifest in json format
      storePaths.map(_.resolve("meta.inf")).filter(Files.notExists(_)).par.foreach(path => {
        Files.createFile(path)
        val writer = Files.newBufferedWriter(path)
        try {
          writer.write(storeMetaInf)
        } finally {
          if (writer != null) writer.close()
        }
      })
    }
    }
  }

  def getPreferredLocation(): String = {
    repository.getPreferredLocation()
  }
}

class StoreLock(folders: => Seq[String]) extends MultiFolderLock(folders)



