@file:Suppress("PropertyName")

package com.windea.plugin.idea.paradox.localisation.formatter

import com.intellij.psi.codeStyle.*
import com.windea.plugin.idea.paradox.*

class ParadoxLocalisationCodeStyleSettings(
	container: CodeStyleSettings
) : CustomCodeStyleSettings(paradoxLocalisationNamePc, container)

