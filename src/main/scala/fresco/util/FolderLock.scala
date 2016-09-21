package fresco.util

import java.nio.channels.{FileChannel, FileLock}
import java.nio.file.{Files, OpenOption, Paths, StandardOpenOption}

/**
  * Created by suhail on 2016-09-08.
  */
class FolderLock  (folder: => String) extends Lock {
  var isLocked = false
  lazy val lockFolder = Paths.get(folder)
  lazy val lockFilePath = Paths.get(folder, ".lock")
  var channel: FileChannel = null
  var flock: FileLock = null
  override def lock(): Lock = {
    try {
      this synchronized {
        if(isLocked) return this
        if(Files.notExists(lockFolder)) Files.createDirectories(lockFolder)
        if(Files.notExists(lockFilePath)) Files.createFile(lockFilePath)
        logger.debug(s"created file $lockFilePath")
        channel = FileChannel.open(lockFilePath, StandardOpenOption.CREATE, StandardOpenOption.WRITE)
        flock = channel.lock()
        logger.debug(s"acquired lock")
        isLocked = true
        this
      }
    } catch {
      case ex: Exception => {
        logger.error(s"exception while locking: $ex")
        if(isLocked) release()
        throw ex
      }
    }
  }

  override def tryLock(): Boolean =  {
    try {
      lock()
      true
    } catch {
      case ex: Exception => false
    }
  }

  override def release(): Unit = {
    try {
      this synchronized {
        if(!isLocked) return
        if (flock != null) flock.release()
        if (channel != null && channel.isOpen) channel.close()
        if (Files.exists(lockFilePath)) Files.delete(lockFilePath)
        logger.debug(s"lock released $lockFilePath")
        isLocked = false
      }
    } catch {
      case ex: Exception => {
        logger.error(s"exception while releasing lock: $ex")
      } //TODO: handle exception
    }
  }
}
