package icu.windea.pls.lang.settings

import com.intellij.ide.AppLifecycleListener
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.progress.ProcessCanceledException
import icu.windea.pls.model.constants.PlsConstants
import java.nio.file.Paths
import kotlin.io.path.notExists
import kotlin.io.path.readText
import kotlin.io.path.writeText

class PlsSettingsMigrator : AppLifecycleListener {
    private val logger = thisLogger()
    private val migrationPropertyName = "pls.settings.migration"
    private val migrationVersion = 2

    private val componentNameMap = mapOf(
        "ParadoxSettings" to "PlsSettings",
        "ParadoxProfilesSettings" to "PlsProfilesSettings"
    )
    private val optionNameMap = mapOf(
        "comment" to "comments",
        "commentByDefault" to "commentsByDefault",
        "parameterConditionBlocks" to "parameterConditions",
        "parameterConditionBlocksByDefault" to "parameterConditionsByDefault",
        "inlineMathBlocks" to "inlineMaths",
        "inlineMathBlocksByDefault" to "inlineMathsByDefault",
    )

    private val replacementMap = buildMap {
        for (entry in componentNameMap) {
            put("<component name=\"${entry.key}\"", "<component name=\"${entry.value}\"")
        }
        for (entry in optionNameMap) {
            put("<option name=\"${entry.key}\"", "<option name=\"${entry.value}\"")
        }
    }

    override fun appFrameCreated(commandLineArgs: List<String?>) {
        migrate()
    }

    private fun migrate() {
        val v = PropertiesComponent.getInstance().getInt(migrationPropertyName, 0)
        if (v >= migrationVersion) return

        val optionsPath = PathManager.getOptionsPath()
        val settingsFileName = PlsConstants.pluginSettingsFileName
        val settingsFile = Paths.get(optionsPath, settingsFileName)
        if (settingsFile.notExists()) return

        try {
            val text = settingsFile.readText()
            val newText = replacementMap.entries.fold(text) { t, r -> t.replace(r.key, r.value) }
            if (newText == text) return
            settingsFile.writeText(newText)
            logger.info("Migration for '$settingsFileName' finished. (migration version: $v)")
        } catch (e: Exception) {
            if (e is ProcessCanceledException) return
            logger.info("Migration for '$settingsFileName' failed. Skip. (migration version: $v)")
        } finally {
            PropertiesComponent.getInstance().setValue(migrationPropertyName, v, 0)
        }
    }
}
