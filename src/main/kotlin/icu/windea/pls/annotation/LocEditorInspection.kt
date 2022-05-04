package icu.windea.pls.annotation

/**
 * 注明此对于本地化语言的检查对应原版游戏文件中标出的关于LocEditor的检查。
 * 
 * 示例：
 * 
 * ```text
 * [LocEditor:OrphanedKeys] Keys that do not exist in English
 * ```
 */
@MustBeDocumented
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class LocEditorInspection(
	val value:String
)