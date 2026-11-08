package icu.windea.pls.core.data

import icu.windea.pls.core.util.io.UTF8_BOM
import kotlinx.serialization.SerializationException
import kotlinx.serialization.Serializable
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * @see JsonService
 * @see JsonOptions
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

    @Test
    fun defaultOptionsTest() {
        // DEFAULT 预设使用标准 JSON 配置，且默认跳过 UTF-8 BOM
        assertFalse(JsonOptions.DEFAULT.isLenient)
        assertFalse(JsonOptions.DEFAULT.allowTrailingComma)
        assertFalse(JsonOptions.DEFAULT.allowComments)
        assertTrue(JsonOptions.DEFAULT.skipUtf8Bom)
    }

    @Suppress("JsonStandardCompliance")
    @Test
    fun json5OptionsTest() {
        val text = """
            {
              // line comment
              name: "Saber", /* block comment */
              category: "Sword",
              attack: 180, // trailing comma below
            }
        """.trimIndent()

        val result = JsonService.read<Weapon>(text, JsonOptions.JSON5)
        assertEquals(Weapon("Saber", "Sword", 180), result)
    }

    @Suppress("JsonStandardCompliance")
    @Test
    fun json5PresetTest() {
        // JSON5 预设应在 DEFAULT 的基础上放开宽松模式、尾随逗号和注释
        assertTrue(JsonOptions.JSON5.isLenient)
        assertTrue(JsonOptions.JSON5.allowTrailingComma)
        assertTrue(JsonOptions.JSON5.allowComments)
        assertTrue(JsonOptions.JSON5.skipUtf8Bom)
    }

    @Suppress("JsonStandardCompliance")
    @Test
    fun standardJsonCannotParseCommentsWithDefaultOptionsTest() {
        val text = """{name:"Saber",category:"Sword",attack:180}"""

        assertFailsWith<SerializationException> {
            JsonService.read<Weapon>(text, JsonOptions.DEFAULT)
        }
    }

    @Suppress("JsonStandardCompliance")
    @Test
    fun json5CompatibilityOverloadTest() {
        val text = """{name:"Saber",category:"Sword",attack:180}"""

        // 保留的 json5: Boolean 兼容重载应等价于 JsonOptions.JSON5
        val result = JsonService.read<Weapon>(text, json5 = true)
        assertEquals(Weapon("Saber", "Sword", 180), result)
    }

    @Test
    fun readStringWithUtf8BomCharTest() {
        val text = "\uFEFF" + """{"name":"Saber","category":"Sword","attack":180}"""

        val result = JsonService.read<Weapon>(text)
        assertEquals(Weapon("Saber", "Sword", 180), result)
    }

    @Test
    fun readStringWithoutSkippingUtf8BomTest() {
        val text = "\uFEFF" + """{"name":"Saber","category":"Sword","attack":180}"""

        // skipUtf8Bom = false：不额外处理 BOM，解析应当失败
        assertFailsWith<SerializationException> {
            JsonService.read<Weapon>(text, JsonOptions(skipUtf8Bom = false))
        }
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
    fun readStreamWithUtf8BomAndJson5OptionsTest() {
        val text = """{name:"Saber",category:"Sword",attack:180}"""
        val bytes = UTF8_BOM + text.toByteArray()

        val result = ByteArrayInputStream(bytes).use { JsonService.read<Weapon>(it, JsonOptions.JSON5) }
        assertEquals(Weapon("Saber", "Sword", 180), result)
    }

    @Test
    fun readStreamWithoutSkippingUtf8BomTest() {
        val text = """{"name":"Saber","category":"Sword","attack":180}"""
        val bytes = UTF8_BOM + text.toByteArray()

        // skipUtf8Bom = false：不额外处理 BOM，解析应当失败
        assertFailsWith<SerializationException> {
            ByteArrayInputStream(bytes).use { JsonService.read<Weapon>(it, JsonOptions(skipUtf8Bom = false)) }
        }
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
