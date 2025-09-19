package icu.windea.pls.ep.tools

import org.junit.Assert.*
import org.junit.Test
import java.nio.file.Paths

class ParadoxModImporterJsonPureTest {
    private fun resourcePath(name: String) = Paths.get(javaClass.classLoader.getResource("tools/$name").toURI())

    @Test
    fun testImportFromJson_dlcLoad() {
        val importer = ParadoxDlcLoadImporter()
        assertTrue(importer.isAvailable())
        val path = resourcePath("dlc_load.json")
        val result = importer.importFromJson(path)
        assertNull(result.gameId)
        assertEquals("Paradox", result.collectionName)
        assertTrue(result.mods.isNotEmpty())
        // 前两个应为 steam ugc
        assertEquals("1995601384", result.mods[0].remoteId)
        assertEquals("727000451", result.mods[1].remoteId)
    }

    @Test
    fun testImportFromJson_playlistV2() {
        val importer = ParadoxLauncherJsonV2Importer()
        assertTrue(importer.isAvailable())
        val path = resourcePath("playlist_v2.json")
        val result = importer.importFromJson(path)
        assertEquals("stellaris", result.gameId)
        assertEquals("My Collection", result.collectionName)
        assertEquals(3, result.mods.size)
        // 仅顺序校验（十六进制按 position 排序）
        assertEquals("A", result.mods[0].name)
        assertEquals("B", result.mods[1].name)
    }

    @Test
    fun testImportFromJson_playlistV3() {
        val importer = ParadoxLauncherJsonV3Importer()
        assertTrue(importer.isAvailable())
        val path = resourcePath("playlist_v3.json")
        val result = importer.importFromJson(path)
        assertEquals("stellaris", result.gameId)
        assertEquals("Real universe", result.collectionName)
        assertTrue(result.mods.size >= 10)
        // 校验 position 排序
        assertEquals(0, result.mods.indexOfFirst { it.name?.contains("Ariphaos", ignoreCase = true) == true })
    }
}
