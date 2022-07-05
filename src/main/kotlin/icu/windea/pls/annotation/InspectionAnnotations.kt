package icu.windea.pls.annotation

/**
 * 注明此检查对应某个原版游戏文件中标出的关于LocEditor的检查。
 */
@MustBeDocumented
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class LocEditorInspection(
	//e.g.
	//[LocEditor:OrphanedKeys] Keys that do not exist in English
	val value: String
)

/**
 * 注明此检查对应某个CWT检查。
 */
@MustBeDocumented
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class CwtInspection(
	//e.g.
	//CWT100
	val value: String

)