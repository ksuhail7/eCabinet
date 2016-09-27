package fresco.filesystem

import java.io.FileOutputStream
import java.nio.file.{Files, Paths}

import com.google.gson.Gson
import fresco.util.{FolderLock, Lock, LockableResource, MultiFolderLock}

import scala.util.Random

/**
  * Created by suhail on 2016-09-08.
  */
class Repository(val id: String, val name: String, val fileSystems: Seq[FileSystem]) extends LockableResource {
  logger.debug("repository created")
  initialize()
  lazy val repositoryLock = new RepositoryLock(this.fileSystems.filter{_.isActive}.map(_.path).toList)

  override def getLock(): Lock = repositoryLock

  def initialize() = {
    import LockableResource.withLockOn
    logger.debug(s"initializing repository")
    logger.debug(s"file systems: $fileSystems")
    //create folders
    withLockOn(this) { repo => {
      val activeFileSystems = repo.fileSystems.filter {
        _.isActive
      }
      logger.debug(s"active file systems: $activeFileSystems")
      repo.fileSystems.filter(_.isActive).foreach { filesystem =>
        val rootFolder = Paths.get(filesystem.path, repo.id)
        if (Files.notExists(rootFolder)) Files.createDirectory(rootFolder)
        this.logger.debug(s"root folder: ${rootFolder.toString}")

        //create sub directories
        val subDirectories = List("objects", "store", "fallback")
        subDirectories.par.foreach(folder => {
          val directoryPath = rootFolder.resolve(folder)
          if (Files.notExists(directoryPath)) Files.createDirectory(directoryPath)
        })

        //create metadata file
        val metaDataFile = rootFolder.resolve("meta.inf")
        if (Files.notExists(metaDataFile)) Files.createFile(metaDataFile)
        val writer = Files.newBufferedWriter(metaDataFile)
        try {
          writer.write(s"{repositoryId: ${repo.id}}")        } finally {
          if(writer != null) writer.close()
        }
      }
    }
    }
  }

  def getPreferredLocation() : String = {
    val paths = fileSystems.filter(_.isActive).map(_.path).map(Paths.get(_, id).toString).toList
    paths(Random.nextInt(paths.size))
  }
}

object Repository {
  private val repositoryMap = scala.collection.mutable.Map[String, Repository]()

  def apply(repositoryId: String): Repository = {
    val fileSystems = List(FileSystem("fs.1", "/var/tmp/fresco/fs1"), FileSystem("fs.2", "/var/tmp/fresco/fs2"), FileSystem("fs.3", "/var/tmp/fresco/fs3"))
    repositoryMap.getOrElseUpdate(repositoryId, new Repository(id = "fresco", name = "fresco", fileSystems = fileSystems))
  }

  def createRepository(repositoryId: String, name: String, fileSystems: Seq[FileSystem]) : Repository = {
    if(repositoryMap.contains(repositoryId)) throw new Exception(s"repository with id: $repositoryId already exists")
    val repo = new Repository(id = repositoryId, name = name, fileSystems = fileSystems)
    repositoryMap.update(repositoryId, repo)
    repo
  }

  def deleteRepository(repository: Repository):Unit = {
    //TODO
  }

  def deleteRepository(repositoryId: String): Unit = {
    if(!repositoryMap.contains(repositoryId)) throw new Exception(s"no such repository: $repositoryId")
    val repo = repositoryMap.get(repositoryId).get
    deleteRepository(repo)
    repositoryMap.remove(repositoryId)
  }

  def updateRepository() = {
    //TODO
  }

}

class RepositoryLock(folders: =>  Seq[String]) extends MultiFolderLock(folders)


