package icu.windea.pls.integrations.lints

import com.intellij.openapi.util.SystemInfo
import icu.windea.pls.integrations.ChronicleIntegrationsBundle
import icu.windea.pls.model.ParadoxGameType

object LintToolConstants {
    object Tiger {
        val name get() = ChronicleIntegrationsBundle.message("tool.tiger.name")
        const val url = "https://github.com/amtep/tiger"

        fun pathTip(gameType: ParadoxGameType): String {
            val name = TigerLintToolService.getInstance().findTool(gameType)?.exePath ?: "tiger"
            val suffix = if (SystemInfo.isWindows) ".exe" else ""
            return "/path/to/$name$suffix"
        }

        fun confPathTip(gameType: ParadoxGameType): String {
            val name = TigerLintToolService.getInstance().findTool(gameType)?.exePath ?: "tiger"
            val suffix = ".conf"
            return "/path/to/$name$suffix"
        }
    }
}
