package icu.windea.pls.script.psi

import com.intellij.lexer.*
import com.intellij.openapi.project.Project
import icu.windea.pls.config.cwt.*
import icu.windea.pls.core.*

class ParadoxScriptLexerAdapter(
	project: Project? = null
) : FlexAdapter(ParadoxScriptLexer(project))
