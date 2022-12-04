package icu.windea.pls.core.annotations

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
