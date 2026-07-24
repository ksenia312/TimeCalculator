package com.xenikii.timecalculator.shared.extensions

import java.time.ZoneId
import java.util.TimeZone

fun deviceZoneId(): ZoneId {
    return ZoneId.of(TimeZone.getDefault().id)
}
