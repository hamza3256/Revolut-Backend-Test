package utils

inline fun String.toLong(whenNotLong: () -> Long): Long {
    return this.toLongOrNull() ?: whenNotLong()
}