package icu.windea.pls.localisation.codeStyle

import com.intellij.psi.codeStyle.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.localisation.*

@Suppress("unused")
class ParadoxLocalisationCodeStyleSettings(
	container: CodeStyleSettings
) : CustomCodeStyleSettings(ParadoxLocalisationLanguage.id, container)