@file:Suppress("UnstableApiUsage")

package icu.windea.pls.extensions.json

import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.ParadoxRootInfo
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * @see JsonExtensionManager
 */
@RunWith(JUnit4::class)
class JsonExtensionManagerTest : JsonTestBase() {
    // region schema paths

    @Test
    fun testGetLauncherSettingsJsonSchemaPath() {
        assertEquals("/jsonSchema/launcher-settings.schema.json", JsonExtensionManager.getLauncherSettingsJsonSchemaPath())
    }

    @Test
    fun testGetMetadataJsonSchemaPath() {
        assertEquals("/jsonSchema/metadata.schema.json", JsonExtensionManager.getMetadataJsonSchemaPath())
    }

    // endregion

    // region isLauncherSettingsJson

    @Test
    fun testIsLauncherSettingsJson_gameRoot() {
        val file = createJsonFile("launcher-settings.json")
        file.injectFileInfo(createGameRootInfo(), "launcher-settings.json")

        assertTrue(JsonExtensionManager.isLauncherSettingsJson(file))
    }

    @Test
    fun testIsLauncherSettingsJson_gameRoot_nestedDirectory() {
        val file = createJsonFile("launcher/launcher-settings.json")
        file.injectFileInfo(createGameRootInfo(), "launcher/launcher-settings.json")

        // 2.1.8 lenient check: only the file name and game root matter
        assertTrue(JsonExtensionManager.isLauncherSettingsJson(file))
    }

    @Test
    fun testIsLauncherSettingsJson_wrongFileName() {
        val file = createJsonFile("launcher-settings.json.bak")
        file.injectFileInfo(createGameRootInfo(), "launcher-settings.json.bak")

        assertFalse(JsonExtensionManager.isLauncherSettingsJson(file))
    }

    @Test
    fun testIsLauncherSettingsJson_notJsonFile() {
        val file = createJsonFile("launcher-settings.json.txt")
        file.injectFileInfo(createGameRootInfo(), "launcher-settings.json.txt")

        assertFalse(JsonExtensionManager.isLauncherSettingsJson(file))
    }

    @Test
    fun testIsLauncherSettingsJson_missingFileInfo() {
        val file = createJsonFile("launcher-settings.json")

        assertFalse(JsonExtensionManager.isLauncherSettingsJson(file))
    }

    @Test
    fun testIsLauncherSettingsJson_modRoot() {
        val file = createJsonFile("launcher-settings.json")
        file.injectFileInfo(createModRootInfo(), "launcher-settings.json")

        assertFalse(JsonExtensionManager.isLauncherSettingsJson(file))
    }

    @Test
    fun testIsLauncherSettingsJson_injectedRoot() {
        val file = createJsonFile("launcher-settings.json")
        file.injectFileInfo(
            ParadoxRootInfo.Injected(gameType = ParadoxGameType.Stellaris),
            "launcher-settings.json"
        )

        assertFalse(JsonExtensionManager.isLauncherSettingsJson(file))
    }

    // endregion

    // region isMetadataJson

    @Test
    fun testIsMetadataJson_modRoot() {
        val file = createJsonFile(".metadata/metadata.json")
        file.injectFileInfo(createModRootInfo(), ".metadata/metadata.json")

        assertTrue(JsonExtensionManager.isMetadataJson(file))
    }

    @Test
    fun testIsMetadataJson_wrongFileName() {
        val file = createJsonFile("metadata.json.bak")
        file.injectFileInfo(createModRootInfo(), "metadata.json.bak")

        assertFalse(JsonExtensionManager.isMetadataJson(file))
    }

    @Test
    fun testIsMetadataJson_notJsonFile() {
        val file = createJsonFile("metadata.json.txt")
        file.injectFileInfo(createModRootInfo(), "metadata.json.txt")

        assertFalse(JsonExtensionManager.isMetadataJson(file))
    }

    @Test
    fun testIsMetadataJson_missingFileInfo() {
        val file = createJsonFile("metadata.json")

        assertFalse(JsonExtensionManager.isMetadataJson(file))
    }

    @Test
    fun testIsMetadataJson_gameRoot() {
        val file = createJsonFile("metadata.json")
        file.injectFileInfo(createGameRootInfo(), "metadata.json")

        assertFalse(JsonExtensionManager.isMetadataJson(file))
    }

    @Test
    fun testIsMetadataJson_injectedRoot() {
        val file = createJsonFile("metadata.json")
        file.injectFileInfo(
            ParadoxRootInfo.Injected(gameType = ParadoxGameType.Stellaris),
            "metadata.json"
        )

        assertFalse(JsonExtensionManager.isMetadataJson(file))
    }

    // endregion
}
