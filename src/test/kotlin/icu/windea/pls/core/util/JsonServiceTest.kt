package icu.windea.pls.core.util

import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.Test
import kotlin.test.assertEquals

class JsonServiceTest {
    @Test
    fun smokeTest() {
        val weapon = Weapon("Breeze Saber", "Saber", 180)

        val json = JsonService.mapper.writeValueAsString(weapon)
        val result = JsonService.mapper.readValue<Weapon>(json)
        assertEquals(weapon, result)
    }

    private data class Weapon(
        val name: String,
        val category: String,
        val attack: Int,
    )
}
