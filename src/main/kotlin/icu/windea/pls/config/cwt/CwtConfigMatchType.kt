package icu.windea.pls.config.cwt

/**
 * CWT规则的匹配类型。
 */
object CwtConfigMatchType {
	/**
	 * 精确匹配：除了匹配类型外，还要匹配范围、作用域等。
	 */
	const val EXACTLY = 0x01
	
	/**
	 * 需要访问文件路径索引。
	 * @see icu.windea.pls.core.psi.ParadoxFilePathIndex
	 */
	const val FILE_PATH = 0x02
	
	/**
	 * 需要访问定义索引。
	 * @see icu.windea.pls.script.psi.ParadoxDefinitionNameIndex
	 * @see icu.windea.pls.script.psi.ParadoxDefinitionTypeIndex
	 */
	const val DEFINITION = 0x04
	
	/**
	 * 需要访问本地化索引。
	 * @see icu.windea.pls.localisation.psi.ParadoxLocalisationNameIndex
	 * @see icu.windea.pls.localisation.psi.ParadoxSyncedLocalisationNameIndex
	 */
	const val LOCALISATION = 0x08
	
	/**
	 * 需要访问复杂枚举值索引。
	 * @see icu.windea.pls.script.psi.ParadoxComplexEnumIndex
	 */
	const val COMPLEX_ENUM_VALUE = 0x10
	
	/**
	 * 需要访问脚本表达式索引。
	 * @see icu.windea.pls.script.psi.ParadoxValueSetValueIndex
	 */
	const val EXPRESSION = 0x20
	
	const val NO_STUB_INDEX  = FILE_PATH
	
	const val ALL  = FILE_PATH or DEFINITION or LOCALISATION or COMPLEX_ENUM_VALUE or EXPRESSION
}