package icu.windea.pls.core.data

import kotlinx.serialization.Serializable
import org.junit.Test
import kotlin.test.assertEquals

/**
 * @see JsonService
 */
class JsonServiceTest {
    @Test
    fun smokeTest() {
        val weapon = Weapon("Breeze Saber", "Saber", 180)

        val json = JsonService.json.encodeToString(weapon)
        val result = JsonService.json.decodeFromString<Weapon>(json)
        assertEquals(weapon, result)
    }

    @Test
    fun outputFormatTest() {
        val text = JsonService.json.encodeToString(Sample("k"))

        // explicitNulls = false：不输出 null 字段
        assert(!text.contains("nullable"))
        // encodeDefaults = true：输出带默认值的字段
        assert(text.contains("items"))
        // prettyPrint = true，且缩进为 2 个空格
        assert(text.contains("  \"key\": \"k\""))
    }

    @Test
    fun json5Test() {
        val text = """
            {
              // line comment
              "name": "Saber", /* block comment */
              "category": "Sword",
              "attack": 180, // trailing comma below
            }
        """.trimIndent()

        val result = JsonService.json5.decodeFromString<Weapon>(text.stripJson5Comments())
        assertEquals(Weapon("Saber", "Sword", 180), result)
    }

    @Serializable
    private data class Weapon(
        val name: String,
        val category: String,
        val attack: Int,
    )

    @Serializable
    private data class Sample(
        val key: String,
        val nullable: String? = null,
        val items: List<String> = emptyList(),
    )
}
