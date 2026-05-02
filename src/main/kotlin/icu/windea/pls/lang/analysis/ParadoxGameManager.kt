package icu.windea.pls.lang.analysis

import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.layout.ValidationInfoBuilder
import icu.windea.pls.model.ParadoxGameType

@Suppress("unused")
@Deprecated("Use `ParadoxAnalysisUtil`", ReplaceWith("ParadoxAnalysisUtil", "icu.windea.pls.lang.analysis.ParadoxAnalysisUtil"))
object ParadoxGameManager {
    fun getQuickGameDirectory(gameType: ParadoxGameType): String? {
        return ParadoxAnalysisUtil.getQuickGameDirectory(gameType)
    }

    fun validateGameDirectory(builder: ValidationInfoBuilder, gameType: ParadoxGameType, gameDirectory: String?): ValidationInfo? {
        return ParadoxAnalysisUtil.validateGameDirectory(builder, gameType, gameDirectory)
    }

    fun getGameVersionFromGameDirectory(gameDirectory: String?): String? {
        return ParadoxAnalysisUtil.getGameVersionFromGameDirectory(gameDirectory)
    }

    fun compareGameVersion(version1: String, version2: String): Int {
        return ParadoxAnalysisUtil.compareGameVersion(version1, version2)
    }
}
