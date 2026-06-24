package icu.windea.pls.csv.formatter

import com.intellij.application.options.CodeStyle
import com.intellij.psi.PsiFile
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.codeStyle.CustomCodeStyleSettings
import icu.windea.pls.csv.ParadoxCsvLanguage

class ParadoxCsvCodeStyleSettings(
    container: CodeStyleSettings
) : CustomCodeStyleSettings(ParadoxCsvLanguage.id, container) {
    companion object {
        @JvmStatic
        fun getInstance(file: PsiFile): ParadoxCsvCodeStyleSettings {
            return CodeStyle.getCustomSettings(file, ParadoxCsvCodeStyleSettings::class.java)
        }
    }
}
