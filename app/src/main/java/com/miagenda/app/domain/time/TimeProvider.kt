package com.miagenda.app.domain.time

import java.time.LocalDateTime

fun interface TimeProvider {
    fun now(): LocalDateTime
}
