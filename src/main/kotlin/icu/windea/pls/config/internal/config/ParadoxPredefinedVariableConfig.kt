package icu.windea.pls.config.internal.config

import com.intellij.openapi.project.*
import com.intellij.psi.PsiElement
import com.intellij.psi.SmartPsiElementPointer
import icu.windea.pls.*
import javax.swing.*

class ParadoxPredefinedVariableConfig(
	override val id: String,
	override val description: String,
	val value: String,
	val pointer: SmartPsiElementPointer<out PsiElement>
): IdAware, DescriptionAware, IconAware {
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
	
	companion object {
		/**
		 * 从内置规则文件中得到指定ID的预定义变量设置。
		 */
		fun find(id: String, project: Project? = null): ParadoxPredefinedVariableConfig? {
			return getInternalConfig(project).predefinedVariableMap[id]
		}
		
		/**
		 * 从内置规则文件中得到得到所有预定义变量设置。
		 */
		fun findAll(project: Project? = null): Map<String, ParadoxPredefinedVariableConfig> {
			return getInternalConfig(project).predefinedVariableMap
		}
		
		/**
		 * 从内置规则文件中得到得到所有预定义变量设置。
		 */
		fun findAllAsArray(project: Project? = null): Array<ParadoxPredefinedVariableConfig> {
			return getInternalConfig(project).predefinedVariables
		}
	}
}