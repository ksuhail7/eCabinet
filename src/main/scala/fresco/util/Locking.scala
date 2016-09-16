package fresco.util

import fresco.logging.Logging

/**
  * Created by suhail on 2016-09-08.
  */
trait Lock extends Logging {
  def isLocked(): Boolean
  def lock():Lock
  def tryLock(): Boolean = {

  }
  def release():Unit
}

trait LockableResource extends Logging {
  def getLock(): Lock
}

object LockableResource extends Logging {
  def withLockOn[T <: LockableResource](lockableResource: => T)(codeToExecute: => T => Unit) = {
    val lock = lockableResource.getLock()
    try {
      if(lock != null) {
        lock.lock()
        logger.debug(s"acquired lock")
        codeToExecute(lockableResource)
      }
    } catch {
      case ex: Exception => {
        throw ex
      }
    } finally {
      if(lock != null && lock.isLocked()) lock.release()
    }
  }
}
