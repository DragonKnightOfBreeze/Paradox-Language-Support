package icu.windea.pls

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import kotlinx.serialization.json.*
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * @see ChronicleIcons
 */
@RunWith(JUnit4::class)
class ChronicleIconsTest : BasePlatformTestCase() {
    @Test
    fun extendedComplexEnumValue_iconCanBeLoaded() {
        val icon = ChronicleIcons.Configs.ExtendedComplexEnumValue
        assertTrue(icon.iconWidth > 0 && icon.iconHeight > 0)
    }

    @Test
    fun iconMappings_referenceExistingResources() {
        val classLoader = ChronicleIcons::class.java.classLoader
        val mappingText = classLoader.getResourceAsStream("ChronicleIconMappings.json")
            ?.bufferedReader()
            ?.use { it.readText() }
        assertNotNull("Cannot find ChronicleIconMappings.json", mappingText)

        val expui = Json.parseToJsonElement(mappingText!!)
            .jsonObject.getValue("icons").jsonObject
            .getValue("expui").jsonObject

        fun walk(obj: JsonObject, prefix: String) {
            for ((name, value) in obj) {
                when (value) {
                    is JsonObject -> walk(value, "$prefix$name/")
                    is JsonPrimitive -> {
                        val legacyPath = value.content.removePrefix("/")
                        val uiPath = "icons/expui/$prefix$name"
                        assertNotNull("Missing legacy icon resource: $legacyPath", classLoader.getResource(legacyPath))
                        assertNotNull("Missing New UI icon resource: $uiPath", classLoader.getResource(uiPath))
                    }
                    else -> fail("Unsupported icon mapping value for $prefix$name")
                }
            }
        }
        walk(expui, "")
    }
}
