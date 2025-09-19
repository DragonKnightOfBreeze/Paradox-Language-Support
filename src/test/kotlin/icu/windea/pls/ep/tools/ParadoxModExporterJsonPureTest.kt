package icu.windea.pls.ep.tools

import icu.windea.pls.core.util.ObjectMappers
import icu.windea.pls.ep.tools.model.LauncherJsonV2
import icu.windea.pls.ep.tools.model.LauncherJsonV3
import icu.windea.pls.ep.tools.model.ParadoxModInfo
import icu.windea.pls.model.ParadoxModSource
import org.junit.Assert.*
import org.junit.Test

class ParadoxModExporterJsonPureTest {
    @Test
    fun testExportV2_toJson() {
        val exporter = ParadoxLauncherJsonV2Exporter()
        assertTrue(exporter.isAvailable())
        val mods = listOf(
            ParadoxModInfo(name = "A", source = ParadoxModSource.Steam, remoteId = "1", enabled = true),
            ParadoxModInfo(name = "B", source = ParadoxModSource.Paradox, remoteId = "2", enabled = true),
            ParadoxModInfo(name = "C", source = ParadoxModSource.Local, enabled = true),
        )
        val json = exporter.toJson("stellaris", "My Collection", mods)
        val parsed = ObjectMappers.jsonMapper.readValue(json, LauncherJsonV2::class.java)
        assertEquals("stellaris", parsed.game)
        assertEquals("My Collection", parsed.name)
        // 本地源被过滤
        assertEquals(2, parsed.mods.size)
        assertEquals("0000001001", parsed.mods[0].position)
        assertEquals("0000001002", parsed.mods[1].position)
    }

    @Test
    fun testExportV3_toJson() {
        val exporter = ParadoxLauncherJsonV3Exporter()
        assertTrue(exporter.isAvailable())
        val mods = listOf(
            ParadoxModInfo(name = "A", source = ParadoxModSource.Steam, remoteId = "1", enabled = true),
            ParadoxModInfo(name = "B", source = ParadoxModSource.Paradox, remoteId = "2", enabled = true),
            ParadoxModInfo(name = "C", source = ParadoxModSource.Local, enabled = true),
        )
        val json = exporter.toJson("stellaris", "My Collection", mods)
        val parsed = ObjectMappers.jsonMapper.readValue(json, LauncherJsonV3::class.java)
        assertEquals("stellaris", parsed.game)
        assertEquals("My Collection", parsed.name)
        // 本地源被过滤
        assertEquals(2, parsed.mods.size)
        assertEquals(0, parsed.mods[0].position)
        assertEquals(1, parsed.mods[1].position)
    }
}
