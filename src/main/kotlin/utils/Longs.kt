package utils

inline fun String.toLong(whenNotLong: (String) -> Long): Long {
    return toLongOrNull() ?: whenNotLong(this)
}