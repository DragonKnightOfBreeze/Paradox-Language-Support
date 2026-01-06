package icu.windea.pls.lang.codeStyle

import com.intellij.application.options.CodeStyle
import com.intellij.psi.PsiFile
import icu.windea.pls.cwt.codeStyle.CwtCodeStyleSettings
import icu.windea.pls.cwt.psi.CwtFile
import icu.windea.pls.script.codeStyle.ParadoxScriptCodeStyleSettings
import icu.windea.pls.script.psi.ParadoxScriptFile

object PlsCodeStyleUtil {
    fun isSpaceWithinBraces(file: PsiFile): Boolean {
        val customSettings = when (file) {
            is CwtFile -> CodeStyle.getCustomSettings(file, CwtCodeStyleSettings::class.java)
            is ParadoxScriptFile -> CodeStyle.getCustomSettings(file, ParadoxScriptCodeStyleSettings::class.java)
            else -> null
        }
         return when (customSettings) {
            is CwtCodeStyleSettings -> customSettings.SPACE_WITHIN_BRACES
            is ParadoxScriptCodeStyleSettings -> customSettings.SPACE_WITHIN_BRACES
            else -> true
        }
    }

    fun isSpaceAroundPropertySeparator(file: PsiFile): Boolean {
        val customSettings = when (file) {
            is CwtFile -> CodeStyle.getCustomSettings(file, CwtCodeStyleSettings::class.java)
            is ParadoxScriptFile -> CodeStyle.getCustomSettings(file, ParadoxScriptCodeStyleSettings::class.java)
            else -> null
        }
        return when (customSettings) {
            is CwtCodeStyleSettings -> customSettings.SPACE_AROUND_PROPERTY_SEPARATOR
            is ParadoxScriptCodeStyleSettings -> customSettings.SPACE_AROUND_PROPERTY_SEPARATOR
            else -> true
        }
    }
}
