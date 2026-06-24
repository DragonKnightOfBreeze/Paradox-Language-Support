package icu.windea.pls.localisation.formatter

import com.intellij.application.options.CodeStyle
import com.intellij.psi.PsiFile
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.codeStyle.CustomCodeStyleSettings
import icu.windea.pls.localisation.ParadoxLocalisationLanguage

class ParadoxLocalisationCodeStyleSettings(
    container: CodeStyleSettings
) : CustomCodeStyleSettings(ParadoxLocalisationLanguage.id, container) {
    companion object {
        @JvmStatic
        fun getInstance(file: PsiFile): ParadoxLocalisationCodeStyleSettings {
            return CodeStyle.getCustomSettings(file, ParadoxLocalisationCodeStyleSettings::class.java)
        }
    }
}
