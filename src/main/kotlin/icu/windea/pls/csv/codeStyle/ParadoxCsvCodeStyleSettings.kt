package icu.windea.pls.csv.codeStyle

import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.codeStyle.CustomCodeStyleSettings
import icu.windea.pls.csv.ParadoxCsvLanguage

class ParadoxCsvCodeStyleSettings(
    container: CodeStyleSettings
) : CustomCodeStyleSettings(ParadoxCsvLanguage.id, container)
