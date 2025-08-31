package icu.windea.pls.lang.settings

import icu.windea.pls.lang.settings.tools.ProfilesDatabase
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.nio.file.Files

class PlsProfilesSettingsDbIntegrationTest {
    private val tempDir = Files.createTempDirectory("pls-db-test")

    @Before
    fun setup() {
        val dbPath = tempDir.resolve("profiles-test.db")
        ProfilesDatabase.setDatabasePathForTest(dbPath)
        // clean all categories just in case
        ProfilesDatabase.clear("gameDescriptorSettings")
        ProfilesDatabase.clear("modDescriptorSettings")
        ProfilesDatabase.clear("gameSettings")
        ProfilesDatabase.clear("modSettings")
    }

    @After
    fun tearDown() {
        // leave files for inspection if needed; no-op
    }

    @Test
    fun testGameDescriptorSettings_Roundtrip() {
        val state = PlsProfilesSettingsState()
        val key = "/path/to/game"
        val v = ParadoxGameDescriptorSettingsState().apply {
            gameDirectory = key
            gameVersion = "1.2.3"
        }
        state.gameDescriptorSettings[key] = v
        state.updateSettings()

        val state2 = PlsProfilesSettingsState()
        val loaded = state2.gameDescriptorSettings[key]
        assertNotNull(loaded)
        assertEquals("1.2.3", loaded!!.gameVersion)
        assertEquals(key, loaded.gameDirectory)
    }

    @Test
    fun testModSettings_Roundtrip_WithNested() {
        val state = PlsProfilesSettingsState()
        val key = "/path/to/mod"
        val v = ParadoxModSettingsState().apply {
            gameDirectory = "/path/to/game"
            modDirectory = key
            options.disableTiger = true
            modDependencies = mutableListOf(
                ParadoxModDependencySettingsState().apply { modDirectory = "/path/to/dep1" },
                ParadoxModDependencySettingsState().apply { modDirectory = "/path/to/dep2" }
            )
        }
        state.modSettings[key] = v
        state.updateSettings()

        val state2 = PlsProfilesSettingsState()
        val loaded = state2.modSettings[key]
        assertNotNull(loaded)
        assertEquals(true, loaded!!.options.disableTiger)
        assertEquals(2, loaded.modDependencies.size)
        assertEquals("/path/to/dep1", loaded.modDependencies[0].modDirectory)
        assertEquals("/path/to/dep2", loaded.modDependencies[1].modDirectory)
    }

    @Test
    fun testRemove_ImmediatePersistence() {
        val state = PlsProfilesSettingsState()
        val key = "/path/to/game2"
        val v = ParadoxGameDescriptorSettingsState().apply { gameDirectory = key }
        state.gameDescriptorSettings[key] = v
        state.updateSettings()

        // remove and verify missing from a new state
        val prev = state.gameDescriptorSettings.remove(key)
        assertNotNull(prev)

        val state2 = PlsProfilesSettingsState()
        val loaded = state2.gameDescriptorSettings[key]
        assertNull(loaded)
    }
}
