package icu.windea.pls.localisation.psi

import com.intellij.lexer.*
import com.intellij.openapi.project.Project

class ParadoxLocalisationLexerAdapter(
	project: Project? = null
) : FlexAdapter(ParadoxLocalisationLexer(project))
