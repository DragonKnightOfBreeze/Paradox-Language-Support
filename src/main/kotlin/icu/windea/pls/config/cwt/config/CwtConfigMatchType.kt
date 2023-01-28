package icu.windea.pls.config.cwt.config

/**
 * CWT规则的匹配类型。
 */
object CwtConfigMatchType {
	/**
	 * 静态匹配：带参数、需要访问索引等场合，直接认为不匹配。
	 */
	const val STATIC = 0x01
	
	/**
	 * 非精确匹配：不需要匹配数字范围、作用域等。
	 */
	const val NOT_EXACT = 0x02
	
	/**
	 * 需要访问文件路径索引。
	 * @see icu.windea.pls.core.index.ParadoxFilePathIndex
	 */
	const val FILE_PATH = 0x04
	
	/**
	 * 需要访问定义索引。
	 * @see icu.windea.pls.core.index.ParadoxDefinitionNameIndex
	 * @see icu.windea.pls.core.index.ParadoxDefinitionTypeIndex
	 */
	const val DEFINITION = 0x08
	
	/**
	 * 需要访问本地化索引。
	 * @see icu.windea.pls.core.index.ParadoxLocalisationNameIndex
	 * @see icu.windea.pls.core.index.ParadoxSyncedLocalisationNameIndex
	 */
	const val LOCALISATION = 0x10
	
	/**
	 * 需要访问复杂枚举值索引。
	 * @see icu.windea.pls.core.index.ParadoxComplexEnumValueIndex
	 */
	const val COMPLEX_ENUM_VALUE = 0x20
	
	const val INSPECTION = DEFINITION or LOCALISATION or COMPLEX_ENUM_VALUE
	const val ALL  = FILE_PATH or DEFINITION or LOCALISATION or COMPLEX_ENUM_VALUE
}
