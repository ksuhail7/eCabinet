package fresco.util

import java.nio.file.Paths

/**
  * Created by suhail on 2016-09-20.
  */
class MultiFolderLock(folders: => Seq[String]) extends Lock {
  var locked = false
  var folderLocks = scala.collection.mutable.ListBuffer[Lock]()

  override def isLocked(): Boolean = locked

  override def lock(): Lock = {
    this synchronized {
      logger.debug(s"trying to acquire lock")
      if (isLocked()) return this
      folderLocks.clear()
      try {
        folders.foreach(folder => {
          val folderLock = new FolderLock(folder)
          folderLock.lock()
          folderLocks.append(folderLock)
        })
        locked = true
        logger.info(s"lock acquired")
        this
      } catch {
        case ex: Exception => {
          logger.error(s"exception while acquiring lock: $ex")
          release()
          throw ex
        }
      }
    }
  }

  override def tryLock(): Boolean = {
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
        if (!isLocked) return
        folderLocks.toList.foreach(lock => lock.release())
        locked = false
        folderLocks.clear()
        logger.info("lock released")
      }
    } catch {
      case ex: Exception => {
        logger.error(s"exception while releasing lock: $ex")
      } //TODO: handle exception
    }
  }
}
