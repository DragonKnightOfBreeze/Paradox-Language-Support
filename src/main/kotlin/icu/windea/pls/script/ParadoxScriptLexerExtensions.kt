package icu.windea.pls.script

import icu.windea.pls.script.psi.*

fun ParadoxScriptLexer.matchesTagName(): Boolean {
	val definitionInfo = this.definitionInfo ?: return false
	val configGroup = definitionInfo.configGroup
	val tags = configGroup.tags
	val text = yytext().toString()
	val tag = tags[text] ?: return false
	return tag.supportedTypes.contains(definitionInfo.type)
}