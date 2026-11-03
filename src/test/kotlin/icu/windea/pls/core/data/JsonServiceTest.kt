package icu.windea.pls.core.data

import icu.windea.pls.core.util.io.UTF8_BOM
import kotlinx.serialization.Serializable
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.assertEquals

/**
 * @see JsonService
 */
class JsonServiceTest {
    // region string

    @Test
    fun smokeTest() {
        val weapon = Weapon("Breeze Saber", "Saber", 180)

        val json = JsonService.write(weapon)
        val result = JsonService.read<Weapon>(json)
        assertEquals(weapon, result)
    }

    @Test
    fun outputFormatTest() {
        val text = JsonService.write(Sample("k"))

        // explicitNulls = true：输出 null 字段
        assert(text.contains("\"nullable\": null"))
        // encodeDefaults = true：输出带默认值的字段
        assert(text.contains("items"))
        // prettyPrint = true，且缩进为 2 个空格
        assert(text.contains("  \"key\": \"k\""))
    }

    @Suppress("JsonStandardCompliance")
    @Test
    fun json5Test() {
        val text = """
            {
              // line comment
              name: "Saber", /* block comment */
              category: "Sword",
              attack: 180, // trailing comma below
            }
        """.trimIndent()

        val result = JsonService.read<Weapon>(text, json5 = true)
        assertEquals(Weapon("Saber", "Sword", 180), result)
    }

    @Test
    fun readStringWithUtf8BomCharTest() {
        val text = "\uFEFF" + """{"name":"Saber","category":"Sword","attack":180}"""

        val result = JsonService.read<Weapon>(text)
        assertEquals(Weapon("Saber", "Sword", 180), result)
    }

    // endregion

    // region stream

    @Test
    fun readStreamWithoutUtf8BomTest() {
        val bytes = """{"name":"Saber","category":"Sword","attack":180}""".toByteArray()

        val result = ByteArrayInputStream(bytes).use { JsonService.read<Weapon>(it) }
        assertEquals(Weapon("Saber", "Sword", 180), result)
    }

    @Test
    fun readStreamWithUtf8BomTest() {
        val text = """{"name":"Saber","category":"Sword","attack":180}"""
        val bytes = UTF8_BOM + text.toByteArray()

        val result = ByteArrayInputStream(bytes).use { JsonService.read<Weapon>(it) }
        assertEquals(Weapon("Saber", "Sword", 180), result)
    }

    @Test
    fun readStreamJson5WithUtf8BomTest() {
        val text = """{name:"Saber",category:"Sword",attack:180}"""
        val bytes = UTF8_BOM + text.toByteArray()

        val result = ByteArrayInputStream(bytes).use { JsonService.read<Weapon>(it, json5 = true) }
        assertEquals(Weapon("Saber", "Sword", 180), result)
    }

    @Test
    fun writeStreamAndReadStreamTest() {
        val weapon = Weapon("Breeze Saber", "Saber", 180)
        val output = ByteArrayOutputStream()

        JsonService.write(weapon, output)

        val result = ByteArrayInputStream(output.toByteArray()).use { JsonService.read<Weapon>(it) }
        assertEquals(weapon, result)
    }

    // endregion

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
