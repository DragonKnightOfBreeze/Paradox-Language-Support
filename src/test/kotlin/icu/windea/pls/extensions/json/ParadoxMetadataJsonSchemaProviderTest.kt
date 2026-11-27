@file:Suppress("UnstableApiUsage")

package icu.windea.pls.extensions.json

import com.jetbrains.jsonSchema.extension.SchemaType
import icu.windea.pls.extensions.ChronicleExtensionsBundle
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * @see ParadoxMetadataJsonSchemaProvider
 */
@RunWith(JUnit4::class)
class ParadoxMetadataJsonSchemaProviderTest : JsonTestBase() {
    @Test
    fun testGetName() {
        val provider = ParadoxMetadataJsonSchemaProvider()

        assertEquals(ChronicleExtensionsBundle.message("json.schema.metadata.name"), provider.name)
    }

    @Test
    fun testGetSchemaType() {
        assertEquals(SchemaType.schema, ParadoxMetadataJsonSchemaProvider().schemaType)
    }

    @Test
    fun testGetSchemaFile() {
        val schemaFile = ParadoxMetadataJsonSchemaProvider().schemaFile

        assertNotNull(schemaFile)
        assertTrue(schemaFile!!.isValid)
        assertEquals("metadata.schema.json", schemaFile.name)
    }

    @Test
    fun testIsAvailable_modMetadataJson() {
        val file = createJsonFile(".metadata/metadata.json")
        file.injectFileInfo(createModRootInfo(), ".metadata/metadata.json")

        assertTrue(ParadoxMetadataJsonSchemaProvider().isAvailable(file))
    }

    @Test
    fun testIsAvailable_notMetadataJson() {
        val file = createJsonFile("other.json")

        assertFalse(ParadoxMetadataJsonSchemaProvider().isAvailable(file))
    }
}
