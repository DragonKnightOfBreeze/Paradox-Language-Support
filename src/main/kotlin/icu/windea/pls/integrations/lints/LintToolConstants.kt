package icu.windea.pls.integrations.lints

import icu.windea.pls.integrations.PlsIntegrationsBundle
import org.cef.OS
import icu.windea.pls.model.ParadoxGameType

object LintToolConstants {
    object Tiger {
        val name get() = PlsIntegrationsBundle.message("integrations.tiger.name")
        const val url = "https://github.com/amtep/tiger"

        fun pathTip(gameType: ParadoxGameType): String {
            val name = TigerLintToolService.getInstance().findTool(gameType)?.exePath ?: "tiger"
            val suffix = if (OS.isWindows()) ".exe" else ""
            return "/path/to/$name$suffix"
        }

        fun confPathTip(gameType: ParadoxGameType): String {
            val name = TigerLintToolService.getInstance().findTool(gameType)?.exePath ?: "tiger"
            val suffix = ".conf"
            return "/path/to/$name$suffix"
        }
    }
}
