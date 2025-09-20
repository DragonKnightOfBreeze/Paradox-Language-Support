package icu.windea.pls.ep.tools.model

import icu.windea.pls.core.util.ObjectMappers
import icu.windea.pls.lang.util.ParadoxMetadataManager
import org.junit.Test
import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.entity.filter
import org.ktorm.entity.find
import org.ktorm.entity.firstOrNull
import org.ktorm.entity.sequenceOf
import org.ktorm.entity.toList
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.util.*
import kotlin.io.path.createDirectories
import kotlin.io.path.exists

/**
 * 基于测试数据文件（src/test/resources/tools）的数据模型验证测试。
 *
 * 涵盖：
 * - playlist_v2.json / playlist_v3.json 的基本结构与字段校验
 * - 使用 SQL 脚本构建的 SQLite（V4+）最小数据集的读取与验证
 */
class DataModelsValidationTest {

    @Test
    fun parsePlaylistV2_fromResources() {
        val ins = javaClass.getResourceAsStream("/tools/playlist_v2.json")
        requireNotNull(ins) { "Missing resource: /tools/playlist_v2.json" }
        val model = ObjectMappers.jsonMapper.readValue(ins, LauncherJsonV2::class.java)
        assert(model.game == "stellaris")
        assert(model.mods.size == 3)
        assert(model.mods.all { it.enabled })
        // V2: position 为左侧补零的十六进制字符串（小写），长度通常为 10
        assert(model.mods.all { it.position.length == 10 && it.position.all { ch -> ch.isDigit() || ch in 'a'..'f' } })
        // 解析为数值后应为连续 4097, 4098, 4099
        val positions = model.mods.map { ParadoxMetadataManager.parseLauncherV2PositionToInt(it.position) }
        assert(positions == listOf(4097, 4098, 4099))
    }

    @Test
    fun parsePlaylistV3_fromResources() {
        val ins = javaClass.getResourceAsStream("/tools/playlist_v3.json")
        requireNotNull(ins) { "Missing resource: /tools/playlist_v3.json" }
        val model = ObjectMappers.jsonMapper.readValue(ins, LauncherJsonV3::class.java)
        assert(model.game == "stellaris")
        assert(model.mods.size == 3)
        assert(model.mods.all { it.enabled })
        // V3: position 为非负整数
        assert(model.mods.all { it.position >= 0 })
    }

    @Test
    fun sqliteV4_fromResources() {
        // 将 SQL 脚本加载并在临时 DB 中执行
        val sqlIns = javaClass.getResourceAsStream("/tools/sqlite_v4.sql")
        requireNotNull(sqlIns) { "Missing resource: /tools/sqlite_v4.sql" }
        val sql = sqlIns.reader(StandardCharsets.UTF_8).readText()

        val tmpDir = Path.of("build", "tmp", "test-db").also { if (!it.exists()) it.createDirectories() }
        val dbFile = tmpDir.resolve("launcher_v4_test_${UUID.randomUUID()}.sqlite")

        val db = Database.connect("jdbc:sqlite:${dbFile.toAbsolutePath()}", driver = "org.sqlite.JDBC")
        // 简单按 ';' 分割执行；自行管理事务，忽略脚本中的 BEGIN/COMMIT 语句
        val stmts = sql.split(';').map { it.trim() }.filter { it.isNotEmpty() }
        db.useConnection { conn ->
            conn.autoCommit = false
            conn.createStatement().use { s ->
                for (stmt in stmts) {
                    val upper = stmt.uppercase(Locale.ROOT)
                    if (upper == "BEGIN TRANSACTION" || upper == "BEGIN") continue
                    if (upper == "COMMIT") continue
                    s.execute(stmt)
                }
            }
            conn.commit()
            conn.autoCommit = true
        }

        // V4+ 判定
        val isV4Plus = runCatching { db.sequenceOf(KnexMigrations).find { it.name eq Constants.sqlV4Id } != null }.getOrDefault(false)
        assert(isV4Plus)

        // 读取一个激活的播放集
        val playset = db.sequenceOf(Playsets).firstOrNull { it.isActive eq true }
        assert(playset != null)

        // 读取映射与关联的模组，校验 position 与引用有效
        val mappings = db.sequenceOf(PlaysetsMods).filter { it.playsetId eq playset!!.id }.toList()
        assert(mappings.size == 3)

        val mods = db.sequenceOf(Mods).toList()
        assert(mods.size == 3)

        // position 为整数（在表中为 INTEGER，但通过我们的映射可作为字符串读取并再解析）
        mappings.forEachIndexed { idx, m ->
            val mod = db.sequenceOf(Mods).firstOrNull { it.id eq m.modId }
            assert(mod != null)
            val p = m.position?.trim()
            assert(!p.isNullOrEmpty() && p.all { it.isDigit() })
        }
    }

    @Test
    fun sqliteV2_fromResources() {
        // 将 SQL 脚本加载并在临时 DB 中执行
        val sqlIns = javaClass.getResourceAsStream("/tools/sqlite_v2.sql")
        requireNotNull(sqlIns) { "Missing resource: /tools/sqlite_v2.sql" }
        val sql = sqlIns.reader(StandardCharsets.UTF_8).readText()

        val tmpDir = Path.of("build", "tmp", "test-db").also { if (!it.exists()) it.createDirectories() }
        val dbFile = tmpDir.resolve("launcher_v2_test_${UUID.randomUUID()}.sqlite")

        val db = Database.connect("jdbc:sqlite:${dbFile.toAbsolutePath()}", driver = "org.sqlite.JDBC")
        val stmts = sql.split(';').map { it.trim() }.filter { it.isNotEmpty() }
        db.useConnection { conn ->
            conn.autoCommit = false
            conn.createStatement().use { s ->
                for (stmt in stmts) {
                    val upper = stmt.uppercase(Locale.ROOT)
                    if (upper == "BEGIN TRANSACTION" || upper == "BEGIN") continue
                    if (upper == "COMMIT") continue
                    s.execute(stmt)
                }
            }
            conn.commit()
            conn.autoCommit = true
        }

        // V4+ 判定应为 false
        val isV4Plus = runCatching { db.sequenceOf(KnexMigrations).find { it.name eq Constants.sqlV4Id } != null }.getOrDefault(false)
        assert(!isV4Plus)

        val playset = db.sequenceOf(Playsets).firstOrNull { it.isActive eq true }
        assert(playset != null)

        val mappings = db.sequenceOf(PlaysetsMods).filter { it.playsetId eq playset!!.id }.toList()
        assert(mappings.size == 3)

        // position 为左侧补零的字符串，V2 常见为十六进制 10 位；此处统一解析为数值
        val parsed = mappings.map { ParadoxMetadataManager.parseLauncherV2PositionToInt(it.position) }.sorted()
        assert(parsed.size == 3)
        assert(parsed.first() >= 1001) // 1001 (0x1001) or 4097
        assert(parsed.zipWithNext().all { (a, b) -> b - a == 1 })
        mappings.forEach { m ->
            val p = m.position ?: ""
            assert(p.length == 10 && p.all { it.isDigit() || it in 'a'..'f' })
        }
    }
}
