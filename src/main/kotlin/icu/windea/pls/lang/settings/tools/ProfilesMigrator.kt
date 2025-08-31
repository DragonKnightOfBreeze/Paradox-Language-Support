package icu.windea.pls.lang.settings.tools

import com.intellij.openapi.application.PathManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.util.application
import icu.windea.pls.model.constants.PlsConstants
import org.jdom.input.SAXBuilder
import org.jdom.output.Format
import org.jdom.output.XMLOutputter
import java.nio.file.Files
import java.nio.file.Paths

/**
 * Profiles 数据一次性迁移器（XML -> SQLite）。
 *
 * - 在 IDE 启动或插件加载时由 [PlsLifecycleListener] 调用。
 * - 仅在首次执行时迁移，并写入迁移完成的元信息标记，避免重复迁移。
 * - 迁移开始与结束会打印警告日志；迁移失败则记录异常并在下次启动重试。
 * - 迁移来源文件路径：`<options>/${PlsConstants.pluginSettingsFileName}`。
 */
object ProfilesMigrator {
    private val log = Logger.getInstance(ProfilesMigrator::class.java)
    private val outputter = XMLOutputter(Format.getCompactFormat())

    /**
     * 如果需要则执行迁移。
     */
    fun migrateIfNeeded() {
        // 在单元测试模式下跳过，避免干扰测试
        if (application.isUnitTestMode) return
        try {
            // 如果已经迁移过则跳过
            if (ProfilesDatabase.getMeta("xml_migrated") == "1") return

            val optionsDir = PathManager.getOptionsPath()
            val xmlPath = Paths.get(optionsDir, PlsConstants.pluginSettingsFileName)
            if (!Files.exists(xmlPath)) {
                log.warn("PLS Profiles 迁移：未发现旧版 XML 文件，跳过。path=$xmlPath")
                // 仍写入标记以避免每次启动都检查
                ProfilesDatabase.putMeta("xml_migrated", "1")
                return
            }

            log.warn("PLS Profiles 迁移开始（XML -> SQLite），后台静默执行。source=$xmlPath")

            val doc = SAXBuilder().build(xmlPath.toFile())
            val root = doc.rootElement // <application>
            val comp = root.getChildren("component").firstOrNull { it.getAttributeValue("name") == "ParadoxProfilesSettings" }
            if (comp == null) {
                log.warn("PLS Profiles 迁移：未找到 component=ParadoxProfilesSettings，跳过。")
                ProfilesDatabase.putMeta("xml_migrated", "1")
                return
            }

            val categories = listOf("gameDescriptorSettings", "modDescriptorSettings", "gameSettings", "modSettings")
            var migratedCount = 0
            for (cat in categories) {
                val items = comp.getChildren(cat)
                if (items.isNullOrEmpty()) continue
                for (item in items) {
                    val key = item.getAttributeValue("path") ?: continue
                    val settingsEl = item.getChild("settings") ?: continue
                    val xml = outputter.outputString(settingsEl)
                    // 写入到按分类拆分的表；若存在同 key 则覆盖
                    ProfilesDatabase.put(cat, key, xml)
                    migratedCount++
                }
            }

            ProfilesDatabase.putMeta("xml_migrated", "1")
            log.warn("PLS Profiles 迁移完成，共迁移 $migratedCount 条记录。")
        } catch (e: Exception) {
            log.warn("PLS Profiles 迁移失败：${e.message}", e)
            // 不写入标记，下次启动重试
        }
    }
}
