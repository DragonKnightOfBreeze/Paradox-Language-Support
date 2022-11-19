package icu.windea.pls.config.internal.config

import com.intellij.psi.*
import icons.*
import icu.windea.pls.cwt.psi.*

/**
 * 系统作用域的内置配置。
 */
class ParadoxSystemScopeConfig(
	override val id: String,
	override val description: String,
	override val pointer: SmartPsiElementPointer<CwtProperty>
): InternalConfig {
	override val icon get() = PlsIcons.SystemScope
	
	override fun equals(other: Any?): Boolean {
		return this === other || other is ParadoxSystemScopeConfig && id == other.id
	}
	
	override fun hashCode(): Int {
		return id.hashCode()
	}
	
	override fun toString(): String {
		return description
	}
}
