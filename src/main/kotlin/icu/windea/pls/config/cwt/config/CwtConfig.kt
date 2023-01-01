package icu.windea.pls.config.cwt.config

import com.intellij.psi.*
import icu.windea.pls.config.cwt.expression.*

interface CwtConfig<out T : PsiElement> {
	val pointer: SmartPsiElementPointer<out T>
	val info: CwtConfigGroupInfo
	
	val expression: CwtDataExpression? get() = null
	
	/**
	 * 解析为被内联的CWT规则，或者返回自身。
	 */
	fun resolved(): CwtConfig<*> = this
	
	/**
	 * 解析为被内联的规则，或者返回null。
	 */
	fun resolvedOrNull(): CwtConfig<*>? = null
}