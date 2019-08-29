package logging

import logging.Logger.Levels.DEBUG
import logging.Logger.Levels.ERROR
import logging.Logger.Levels.INFO
import logging.Logger.Levels.VERBOSE
import logging.Logger.Levels.WARN

interface Logger {

    fun log(
        tag: Any,
        level: Int = INFO,
        message: Any
    )

    fun debug(tag: Any, message: Any) = log(tag, DEBUG, message)
    fun verbose(tag: Any, message: Any) = log(tag, VERBOSE, message)
    fun info(tag: Any, message: Any) = log(tag, INFO, message)
    fun warn(tag: Any, message: Any) = log(tag, WARN, message)
    fun eror(tag: Any, message: Any) = log(tag, ERROR, message)

    object Levels {
        const val DEBUG = 0
        const val VERBOSE = 1
        const val INFO = 2
        const val WARN = 3
        const val ERROR = 4

        val levelDescriptions = mapOf(
            DEBUG to "Debug",
            VERBOSE to "Verbose",
            INFO to "Info",
            WARN to "Warn",
            ERROR to "Error"
        )
    }
}

class ConsoleLogger : Logger {

    override fun log(tag: Any, level: Int, message: Any) {
        val timeNowMs = System.currentTimeMillis()
        val levelDescription = Logger.Levels.levelDescriptions.getOrElse(level) { "UnknownLevel($level)" }
        println("$timeNowMs | $levelDescription | $tag | $message")
    }
}

object DefaultLogger : Logger by ConsoleLogger()

inline fun Any.log(logger: Logger, level: Int = INFO, message: () -> Any) {
    val tag = javaClass.simpleName
    logger.log(
        tag = tag,
        level = level,
        message = message()
    )
}

inline fun Any.debug(message: () -> Any) {
    return log(DefaultLogger, DEBUG, message)
}

inline fun Any.verbose(message: () -> Any) {
    return log(DefaultLogger, VERBOSE, message)
}

inline fun Any.info(message: () -> Any) {
    return log(DefaultLogger, INFO, message)
}

inline fun Any.warn(message: () -> Any) {
    return log(DefaultLogger, WARN, message)
}

inline fun Any.error(message: () -> Any) {
    return log(DefaultLogger, ERROR, message)
}
