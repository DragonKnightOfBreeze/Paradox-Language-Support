@file:Suppress("UnstableApiUsage")

package icu.windea.pls.extensions.json

import com.jetbrains.jsonSchema.extension.SchemaType
import icu.windea.pls.extensions.ChronicleExtensionsBundle
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * @see ParadoxLauncherSettingsJsonSchemaProvider
 */
@RunWith(JUnit4::class)
class ParadoxLauncherSettingsJsonSchemaProviderTest : JsonTestBase() {
    @Test
    fun testGetName() {
        val provider = ParadoxLauncherSettingsJsonSchemaProvider()

        assertEquals(ChronicleExtensionsBundle.message("json.schema.launcherSettings.name"), provider.name)
    }

    @Test
    fun testGetSchemaType() {
        assertEquals(SchemaType.schema, ParadoxLauncherSettingsJsonSchemaProvider().schemaType)
    }

    @Test
    fun testGetSchemaFile() {
        val schemaFile = ParadoxLauncherSettingsJsonSchemaProvider().schemaFile

        assertNotNull(schemaFile)
        assertTrue(schemaFile!!.isValid)
        assertEquals("launcher-settings.schema.json", schemaFile.name)
    }

    @Test
    fun testIsAvailable_gameLauncherSettingsJson() {
        val file = createJsonFile("launcher-settings.json")
        file.injectFileInfo(createGameRootInfo(), "launcher-settings.json")

        assertTrue(ParadoxLauncherSettingsJsonSchemaProvider().isAvailable(file))
    }

    @Test
    fun testIsAvailable_notLauncherSettingsJson() {
        val file = createJsonFile("other.json")

        assertFalse(ParadoxLauncherSettingsJsonSchemaProvider().isAvailable(file))
    }
}
