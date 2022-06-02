package icu.windea.pls.config.internal.config

import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.internal.*
import javax.swing.*

/**
 * 本地化预定义变量的内置配置。
 */
class ParadoxPredefinedVariableConfig(
	override val id: String,
	override val description: String,
	val value: String,
	val pointer: SmartPsiElementPointer<out PsiElement>
) : IdAware, DescriptionAware, IconAware {
	override val icon: Icon get() = PlsIcons.variableIcon
	
	val documentation = buildString {
		append(id).append(" = ").append(value)
		if(description.isNotEmpty()) append(" (").append(description).append(")")
	}
	
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