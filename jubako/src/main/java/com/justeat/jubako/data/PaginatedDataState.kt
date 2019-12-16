package com.justeat.jubako.data

data class PaginatedDataState<T>(
    var loaded: List<T>,
    var page: List<T>,
    var loading: Boolean = false,
    var error: Throwable? = null,
    var accepted: Boolean = false
) {
    fun accept(): Boolean {
        if (!accepted) {
            accepted = true
            return true
        }
        return false
    }
}

fun PaginatedDataState<*>?.ready(): Boolean {
    return this != null && !loading && error == null
}
