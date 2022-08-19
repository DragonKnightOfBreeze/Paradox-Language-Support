package icu.windea.pls.config.internal.config

import com.intellij.psi.*
import icons.*
import javax.swing.*

/**
 * 本地化预定义变量的内置配置。
 */
class ParadoxPredefinedVariableConfig(
	override val id: String,
	override val description: String,
	override val pointer: SmartPsiElementPointer<out PsiElement>
) : InternalConfig {
	override val icon: Icon get() = PlsIcons.Variable
	
	override fun equals(other: Any?): Boolean {
		return this === other || other is ParadoxPredefinedVariableConfig && id == other.id
	}
	
	override fun hashCode(): Int {
		return id.hashCode()
	}
	
	override fun toString(): String {
		return description
	}
}