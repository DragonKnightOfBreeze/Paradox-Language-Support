package icu.windea.pls.config.cwt

/**
 * CWT规则的匹配类型。
 */
object CwtConfigMatchType {
	/**
	 * 需要访问文件路径索引。
	 * @see icu.windea.pls.core.psi.ParadoxFilePathIndex
	 */
	const val FILE_PATH = 0x01
	
	/**
	 * 需要访问定义索引。
	 * @see icu.windea.pls.script.psi.ParadoxDefinitionNameIndex
	 * @see icu.windea.pls.script.psi.ParadoxDefinitionTypeIndex
	 */
	const val DEFINITION = 0x02
	
	/**
	 * 需要访问本地化索引。
	 * @see icu.windea.pls.localisation.psi.ParadoxLocalisationNameIndex
	 * @see icu.windea.pls.localisation.psi.ParadoxSyncedLocalisationNameIndex
	 */
	const val LOCALISATION = 0x04
	
	/**
	 * 需要访问复杂枚举值索引。
	 * @see icu.windea.pls.script.psi.ParadoxComplexEnumIndex
	 */
	const val COMPLEX_ENUM_VALUE = 0x08
	
	/**
	 * 静态匹配：带参数、需要访问索引等场合，直接认为不匹配。
	 */
	const val STATIC = 0x10
	
	/**
	 * 精确匹配：除了匹配类型外，还要匹配范围、作用域等。
	 */
	const val EXACT = 0x20
	
	const val NO_STUB_INDEX  = FILE_PATH
	
	const val ALL  = FILE_PATH or DEFINITION or LOCALISATION or COMPLEX_ENUM_VALUE
}