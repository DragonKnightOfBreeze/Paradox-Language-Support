package icu.windea.pls.config.cwt

import com.intellij.psi.*
import icu.windea.pls.cwt.psi.*

data class CwtOptionValueConfig(
	override val pointer: SmartPsiElementPointer<CwtValue>, //NOTE 未使用
	val value:String,
	val booleanValue:Boolean? = null,
	val intValue:Int? = null,
	val floatValue:Float? = null,
	val stringValue:String? = null,
	val values:List<CwtOptionValueConfig>? = null,
	val options: List<CwtOptionConfig>? = null
): CwtConfig<CwtValue>