package com.miagenda.app.domain

import com.miagenda.app.domain.preferences.MAX_DISPLAY_NAME_LENGTH
import com.miagenda.app.domain.preferences.NameValidation
import com.miagenda.app.domain.preferences.validateDisplayName
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DisplayNameRulesTest {
    @Test
    fun blankNameIsAcceptedAsEmpty() {
        val result = validateDisplayName("   ")
        assertEquals(NameValidation.Ok(""), result)
    }

    @Test
    fun fortyCharactersAreAccepted() {
        val name = "a".repeat(MAX_DISPLAY_NAME_LENGTH)
        assertEquals(NameValidation.Ok(name), validateDisplayName(" $name "))
    }

    @Test
    fun fortyOneCharactersAreRejected() {
        val result = validateDisplayName("a".repeat(MAX_DISPLAY_NAME_LENGTH + 1))
        assertTrue(result is NameValidation.TooLong)
    }
}
