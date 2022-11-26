package icu.windea.pls.expression.psi

import com.intellij.lang.*
import com.intellij.lang.parser.*
import com.intellij.openapi.util.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.core.handler.*
import icu.windea.pls.expression.*
import icu.windea.pls.expression.psi.ParadoxExpressionElementTypes.*
import icu.windea.pls.expression.psi.ParadoxExpressionElementTypes.Companion.IDENTIFIER_TOKEN
import icu.windea.pls.expression.psi.ParadoxExpressionElementTypes.Companion.SYSTEM_SCOPE

@Suppress("UNUSED_PARAMETER")
object ParadoxExpressionParserUtil: GeneratedParserUtilBase() {
	@JvmStatic
	fun parseSystemScope(builder: PsiBuilder, level: Int) : Boolean{
		if(!nextTokenIs(builder, IDENTIFIER_TOKEN)) return false
		val configGroup = builder.getUserData(ParadoxExpressionKeys.configGroupKey) ?: return false
		val tokenText = builder.tokenText ?: return false
		if(!ParadoxExpressionHandler.isSystemScope(tokenText, configGroup)) return false
		val m = enter_section_(builder)
		val r = consumeToken(builder, IDENTIFIER_TOKEN)
		exit_section_(builder, m, IDENTIFIER_TOKEN, r)
		return r
	}
	
	@JvmStatic
	fun parseScopeLink(builder: PsiBuilder, level: Int) : Boolean{
		if(!nextTokenIs(builder, IDENTIFIER_TOKEN)) return false
		val configGroup = builder.getUserData(ParadoxExpressionKeys.configGroupKey) ?: return false
		val tokenText = builder.tokenText ?: return false
		if(!ParadoxExpressionHandler.isScopeLink(tokenText, configGroup)) return false
		val m = enter_section_(builder)
		val r = consumeToken(builder, IDENTIFIER_TOKEN)
		exit_section_(builder, m, SYSTEM_SCOPE, r)
		return r
	}
	
	@JvmStatic
	fun parseScopeLinkFromData(builder: PsiBuilder, level: Int) : Boolean{
		TODO()
	}
	
	@JvmStatic
	fun parseValueLink(builder: PsiBuilder) : Boolean{
		TODO()
	}
}
