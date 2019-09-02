package utils

import org.slf4j.Logger

inline fun Logger.error(lazyMessage: () -> Any) {
    if (isErrorEnabled) error(lazyMessage().toString())
}

inline fun Logger.warn(lazyMessage: () -> Any) {
    if (isWarnEnabled) warn(lazyMessage().toString())
}

inline fun Logger.info(lazyMessage: () -> Any) {
    if (isInfoEnabled) info(lazyMessage().toString())
}

inline fun Logger.debug(lazyMessage: () -> Any) {
    if (isDebugEnabled) debug(lazyMessage().toString())
}

inline fun Logger.trace(lazyMessage: () -> Any) {
    if (isTraceEnabled) trace(lazyMessage().toString())
}