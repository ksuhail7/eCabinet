package fresco.logging

import org.slf4j.LoggerFactory

/**
  * Created by suhail on 2016-09-08.
  */
trait Logging {
  val loggerName = this.getClass.getName
  lazy val logger = LoggerFactory.getLogger(loggerName)

  def debug(msg: => String): Unit = {
    if(logger.isDebugEnabled) logger.debug(msg)
  }

  def info(msg: => String): Unit = {
    if(logger.isInfoEnabled) logger.info(msg)
  }

  def warn(msg: => String): Unit = {
    if(logger.isWarnEnabled) logger.warn(msg)
  }

  def error(msg: => String): Unit = {
    if(logger.isErrorEnabled) logger.error(msg)
  }

}
