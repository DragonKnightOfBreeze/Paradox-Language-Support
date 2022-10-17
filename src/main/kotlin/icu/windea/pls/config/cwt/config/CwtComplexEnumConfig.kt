package icu.windea.pls.config.cwt.config

import com.intellij.psi.*
import icu.windea.pls.cwt.psi.*

/**
 * @property path (property*) path: string 相对于游戏或模组根路径的路径。
 * @property pathFile (property) path_file: string 路径下的文件名。
 * @property pathStrict (property) path_strict: boolean 
 * @property startFromRoot (property) start_from_rot: boolean
 * @property nameConfig (property) name: block 描述如何获取枚举名。将`enum_name`对应的key/value作为枚举名。
 */
data class CwtComplexEnumConfig(
	override val pointer: SmartPsiElementPointer<CwtProperty>,
	override val info: CwtConfigInfo,
	val name: String,
	val path: List<String>,
	val pathFile: String?,
	val pathStrict: Boolean,
	val startFromRoot: Boolean,
	val nameConfig: CwtPropertyConfig
): CwtConfig<CwtProperty>