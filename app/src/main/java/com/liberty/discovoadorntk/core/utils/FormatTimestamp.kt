package com.liberty.discovoadorntk.core.utils

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun Long.toFormatTimestamp(): String {
    val instant = Instant.ofEpochMilli(this)
    val zoned = instant.atZone(ZoneId.systemDefault())
    val fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm")
    return zoned.format(fmt)
}