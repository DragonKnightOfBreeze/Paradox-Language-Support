package icu.windea.pls.ep.tools.model

import icu.windea.pls.core.util.ObjectMappers
import org.junit.Test
import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.entity.filter
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
        // V2: position 为字符串，且非空
        assert(model.mods.all { it.position.isNotEmpty() })
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
        val isV4Plus = db.sequenceOf(KnexMigrations).firstOrNull { KnexMigrations.name eq Constants.sqlV4Id } != null
        assert(isV4Plus)

        // 读取一个激活的播放集
        val playset = db.sequenceOf(Playsets).firstOrNull { Playsets.isActive eq true }
        assert(playset != null)

        // 读取映射与关联的模组，校验 position 与引用有效
        val mappings = db.sequenceOf(PlaysetsMods).filter { PlaysetsMods.playsetId eq playset!!.id }.toList()
        assert(mappings.size == 3)

        val mods = db.sequenceOf(Mods).toList()
        assert(mods.size == 3)

        // position 为整数（在表中为 INTEGER，但通过我们的映射可作为字符串读取并再解析）
        mappings.forEachIndexed { idx, m ->
            val mod = db.sequenceOf(Mods).firstOrNull { Mods.id eq m.modId }
            assert(mod != null)
            val p = m.position?.trim()
            assert(!p.isNullOrEmpty() && p.all { it.isDigit() })
        }
    }
}
