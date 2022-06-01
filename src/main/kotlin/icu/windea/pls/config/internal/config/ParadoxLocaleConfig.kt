package icu.windea.pls.config.internal.config

import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.*

class ParadoxLocaleConfig(
	override val id: String,
	override val description: String,
	val languageTag: String,
	val pointer: SmartPsiElementPointer<out PsiElement>
) : IdAware, DescriptionAware, IconAware {
	override val icon get() = PlsIcons.localisationLocaleIcon
	
	val documentation = buildString {
		append(id)
		if(description.isNotEmpty()) append(" (").append(description).append(")")
	}
	
	override fun equals(other: Any?): Boolean {
		return this === other || other is ParadoxLocaleConfig && id == other.id
	}
	
	override fun hashCode(): Int {
		return id.hashCode()
	}
	
	override fun toString(): String {
		return description
	}
	
	companion object {
		/**
		 * 从内置规则文件中得到指定ID的语言区域设置。
		 */
		fun find(id: String, project: Project? = null): ParadoxLocaleConfig? {
			return getInternalConfig(project).localeMap[id]
		}
		
		/**
		 * 从内置规则文件中得到指定FLAG的语言区域设置。
		 */
		fun findByFlag(flag: String, project: Project? = null): ParadoxLocaleConfig? {
			return getInternalConfig(project).localeFlagMap[flag]
		}
		
		/**
		 * 从内置规则文件中得到得到所有语言区域设置。
		 */
		fun findAll(project: Project? = null): Map<String, ParadoxLocaleConfig> {
			return getInternalConfig(project).localeMap
		}
		
		/**
		 * 从内置规则文件中得到得到所有语言区域设置。
		 */
		fun findAllByFlag(project: Project? = null): Map<String, ParadoxLocaleConfig> {
			return getInternalConfig(project).localeFlagMap
		}
		
		/**
		 * 从内置规则文件中得到得到所有语言区域设置。
		 */
		fun findAllAsArray(project: Project? = null): Array<ParadoxLocaleConfig> {
			return getInternalConfig(project).locales
		}
	}
}