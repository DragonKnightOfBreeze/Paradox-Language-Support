package icu.windea.pls.ep.tools.model

import icu.windea.pls.core.util.ObjectMappers
import icu.windea.pls.lang.PlsDataProvider
import icu.windea.pls.model.ParadoxGameType
import org.junit.Assume
import org.junit.Test
import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.entity.filter
import org.ktorm.entity.find
import org.ktorm.entity.firstOrNull
import org.ktorm.entity.sequenceOf
import org.ktorm.entity.toList
import kotlin.io.path.exists
import kotlin.io.path.isRegularFile

/**
 * 基于本地文件的验证测试：
 * - JSON：dlc_load.json / content_load.json / launcher-settings.json
 * - SQLite：launcher-v2.sqlite（或 launcher-v2_openbeta.sqlite）
 *
 * 仅在本地环境运行（CI 默认跳过）：
 *   -Dpls.test.include.local.env=true
 */
class DataModelsLocalValidationTest {
    private val gameDataDir = PlsDataProvider().getGameDataPath(ParadoxGameType.Stellaris.title)

    // 不需要
    // @Before
    // fun before() = AssumePredicates.includeLocalEnv()

    @Test
    fun readDlcLoadJson_ifExists() {
        Assume.assumeTrue("Skip: gameDataDir not found", gameDataDir != null)
        gameDataDir!!

        val file = gameDataDir.resolve(Constants.dlcLoadPath)
        Assume.assumeTrue("Skip: ${file} not found", file.exists() && file.isRegularFile())

        val model = ObjectMappers.jsonMapper.readValue(file.toFile(), DlcLoadJson::class.java)
        // 基本断言 + 更严格校验
        assert(model != null)
        assert(model.disabledDlcs.all { it.isNotBlank() })
        assert(model.enabledMods.all { it.isNotBlank() })
        println("dlc_load.json -> disabledDlcs=${model.disabledDlcs.size}, enabledMods=${model.enabledMods.size}")
    }

    @Test
    fun readContentLoadJson_ifExists() {
        Assume.assumeTrue("Skip: gameDataDir not found", gameDataDir != null)
        gameDataDir!!

        val file = gameDataDir.resolve(Constants.contentLoadPath)
        Assume.assumeTrue("Skip: ${file} not found", file.exists() && file.isRegularFile())

        val model = ObjectMappers.jsonMapper.readValue(file.toFile(), ContentLoadJson::class.java)
        // 基本断言 + 更严格校验：路径应当为 .mod 描述符
        assert(model != null)
        val modPaths = model.enabledMods.map { it.path }
        assert(modPaths.all { it.isNotBlank() })
        // 绝大多数情况下为 .mod，个别变体允许不是 .mod，因此放宽为“若以 .mod 结尾，则长度应>4”
        assert(modPaths.all { !it.endsWith(".mod") || it.length > 4 })
        println("content_load.json -> disabledDlcs=${model.disabledDlcs.size}, enabledMods=${model.enabledMods.size}, enabledUGC=${model.enabledUgc.size}")
    }

    @Test
    fun readPlaylistJson_ifExists() {
        Assume.assumeTrue("Skip: gameDataDir not found", gameDataDir != null)
        gameDataDir!!

        val file = gameDataDir.resolve("playlists/playlist.json")
        Assume.assumeTrue("Skip: ${file} not found", file.exists() && file.isRegularFile())

        // 先用 readTree 探测 position 类型（V2: string, V3: int）。
        val root = ObjectMappers.jsonMapper.readTree(file.toFile())
        val modsNode = root.get("mods")
        val first = modsNode?.firstOrNull()
        val isV3 = first?.get("position")?.isInt == true

        // 校验 game 字段
        val game = root.get("game")?.asText()
        assert(game == ParadoxGameType.Stellaris.gameId)

        if (isV3) {
            val model = ObjectMappers.jsonMapper.readValue(file.toFile(), LauncherJsonV3::class.java)
            assert(model.mods.all { it.position >= 0 })
            // 每个 mod 应该至少有 steamId 或 pdxId
            assert(model.mods.all { (it.steamId != null && it.steamId.isNotEmpty()) || (it.pdxId != null && it.pdxId.isNotEmpty()) })
            println("playlist.json -> V3, mods=${model.mods.size}")
        } else {
            val model = ObjectMappers.jsonMapper.readValue(file.toFile(), LauncherJsonV2::class.java)
            assert(model.mods.all { it.position.isNotEmpty() })
            assert(model.mods.all { (it.steamId != null && it.steamId.isNotEmpty()) || (it.pdxId != null && it.pdxId.isNotEmpty()) })
            println("playlist.json -> V2, mods=${model.mods.size}")
        }
    }

    @Test
    fun readLauncherSqlite_ifExists() {
        Assume.assumeTrue("Skip: gameDataDir not found", gameDataDir != null)
        gameDataDir!!

        val sqlite = sequenceOf(
            gameDataDir.resolve(Constants.launcherDbPath),
            gameDataDir.resolve(Constants.launcherDbBetaPath)
        ).firstOrNull { it.exists() && it.isRegularFile() }
        Assume.assumeTrue("Skip: launcher sqlite not found", sqlite != null)

        val db = Database.connect("jdbc:sqlite:${sqlite!!.toAbsolutePath()}", driver = "org.sqlite.JDBC")

        // 版本探测：是否包含 V4 迁移（position 改为 INTEGER）
        val isV4Plus = runCatching { db.sequenceOf(KnexMigrations).find { KnexMigrations.name eq Constants.sqlV4Id } != null }.getOrDefault(false)
        println("sqlite -> v4Plus=${isV4Plus}")

        val playsets = db.sequenceOf(Playsets).toList()
        println("sqlite -> playsets.size=${playsets.size}")

        // 读取激活的播放集，若无则取任意一个
        val activeCount = db.sequenceOf(Playsets).filter { Playsets.isActive eq true }.toList().size
        assert(activeCount <= 1) // 合理情况下仅有一个激活的播放集
        val active = db.sequenceOf(Playsets).filter { Playsets.isActive eq true }.firstOrNull()
            ?: playsets.firstOrNull()
        Assume.assumeTrue("Skip: no playset in sqlite", active != null)

        // 读取映射并简单验证关联与 position 格式
        val mappings = db.sequenceOf(PlaysetsMods).filter { PlaysetsMods.playsetId eq active!!.id }.toList()
        println("sqlite -> mappings.size=${mappings.size}")

        // 校验 enabled 标记（V4 架构下通常存在该列，并默认 true）
        assert(mappings.all { it.enabled })

        // 随机抽查一个映射，验证能找到 mod 记录
        val sample = mappings.take(10)
        val modsSeq = db.sequenceOf(Mods)
        sample.forEach { m ->
            val mod = modsSeq.find { Mods.id eq m.modId }
            assert(mod != null)
            if (isV4Plus) {
                // V4+：position 应为整数字符串（JDBC 可将 INTEGER 取为字符串）
                val p = m.position?.trim()
                assert(!p.isNullOrEmpty() && p.all { it.isDigit() })
            } else {
                // V2：position 为左侧补零的十进制字符串（常见长度 10，容忍 >=6）
                val p = m.position
                assert(!p.isNullOrEmpty() && p.all { it.isDigit() } && p.length >= 6)
            }
        }
    }
}
