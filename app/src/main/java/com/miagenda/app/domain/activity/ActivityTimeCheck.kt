package com.miagenda.app.domain.activity

import java.time.LocalTime

sealed interface ActivityTimeCheck {
    data object Valid : ActivityTimeCheck
    data object EndBeforeStart : ActivityTimeCheck
}

/**
 * Una actividad ocupa un solo día. La hora de fin puede ser igual a la de
 * inicio. Si la hora de fin es anterior, el dato se rechaza: en esta versión
 * una actividad no puede cruzar la medianoche.
 */
fun checkActivityTimes(start: LocalTime, end: LocalTime): ActivityTimeCheck {
    return if (end < start) {
        ActivityTimeCheck.EndBeforeStart
    } else {
        ActivityTimeCheck.Valid
    }
}
