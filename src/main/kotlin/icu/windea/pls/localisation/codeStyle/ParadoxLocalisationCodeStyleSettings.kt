package icu.windea.pls.localisation.codeStyle

import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.codeStyle.CustomCodeStyleSettings
import icu.windea.pls.localisation.ParadoxLocalisationLanguage

class ParadoxLocalisationCodeStyleSettings(
    container: CodeStyleSettings
) : CustomCodeStyleSettings(ParadoxLocalisationLanguage.id, container)
