package fresco.filesystem

import java.nio.file.{Files, Paths}
import java.util.Date

import fresco.logging.Logging
import fresco.util.{FolderLock, Lock, LockableResource}

/**
  * Created by suhail on 2016-09-08.
  */
case class Repository(val id:String, val name:String, val fileSystems: Seq[FileSystem], val creationDate: Date = new Date()) extends LockableResource with Logging {
  logger.debug("repository created")
  initialize()
  val repositoryLock = new RepositoryLock(this)

  override def getLock(): Lock = repositoryLock

  def initialize() = {
    import LockableResource._
    //create folders
    withLockOn(this) { repo =>
      repo.fileSystems.filter(_.isActive).foreach { filesystem =>
        val rootFolder = Paths.get(filesystem.path, repo.id)
        if (Files.notExists(rootFolder)) Files.createDirectory(rootFolder)

        //create sub directories
        val subDirectories = List("objects", "store", "fallback")
        subDirectories.par.foreach(folder => {
          val directoryPath = rootFolder.resolve(folder)
          if (Files.notExists(directoryPath)) Files.createDirectory(directoryPath)
        })

        //create metadata file
        val metaDataFile = rootFolder.resolve("meta.inf")
        if (Files.notExists(metaDataFile)) Files.createFile(metaDataFile)
      }
    }
  }
}

object Repository {
  private val repositoryMap = scala.collection.mutable.Map[String, Repository]()
  def apply(repositoryLocation: String):Repository = {
    val fileSystems = List(FileSystem("fs.1", "/var/tmp/fresco/fs1"), FileSystem("fs.2", "/var/tmp/fresco/fs2"), FileSystem("fs.3", "/var/tmp/fresco/fs3"))
     repositoryMap.getOrElseUpdate(repositoryLocation, new Repository(id="fresco", name="fresco", fileSystems = fileSystems))
  }
}

class RepositoryLock(val repository: Repository) extends Lock {
  var locked = false
  var folderLocks = scala.collection.mutable.ListBuffer[Lock]()

  override def isLocked(): Boolean = locked

  override def lock(): Lock = {
    this synchronized {
      try {
        repository.fileSystems.filter(_.isActive).foreach(filesystem => {
          val folderLock = new FolderLock(filesystem.path)
          folderLock.lock()
          folderLocks.append(folderLock)
        })
        locked = true
        this
      } catch {
        case ex: Exception => {
            release()
            throw ex
          }
      }
    }
  }

  override def tryLock(): Boolean = {

  }

  override def release(): Unit = ???
}

