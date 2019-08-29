package utils

import io.javalin.http.Context

inline fun Context.requireParam(param: String, whenAbsent: (String) -> String): String {
    return queryParam(param) ?: whenAbsent(param)
}