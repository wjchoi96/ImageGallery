package com.gallery.kakaogallery.domain.model

import java.io.IOException
import java.lang.Exception

data class NetworkConnectionException(
    override val message: String?
): IOException()

data class MaxPageException(
    override val message: String = "query is max page"
): Exception()

data class UnKnownException(
    override val message: String = "UnknownException",
    override val cause: Throwable? = null
): Exception() {
}