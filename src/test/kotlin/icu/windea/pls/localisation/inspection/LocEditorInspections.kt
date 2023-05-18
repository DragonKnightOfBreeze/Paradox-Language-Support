@file:Suppress("InspectionDescriptionNotFoundInspection", "unused")

package icu.windea.pls.localisation.inspection

import com.intellij.codeInspection.*

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

@LocEditorInspection("[LocEditor:RedundantFile] File contains no active keys, does not exist in English, could be deleted")
class LocEditorRedundantFileInspection: LocalInspectionTool()

@LocEditorInspection("[LocEditor:OrphanedFile] File contains only orphaned keys")
class LocEditorOrphanedFileInspection : LocalInspectionTool()

@LocEditorInspection("[LocEditor:OrphanedKeys] Keys that do not exist in English")
class LocEditorOrphanedKeysInspection: LocalInspectionTool()

@LocEditorInspection("[LocEditor:UntranslatedKey] `{localisationKey}`")
class LocEditorUntranslatedKeyInspection: LocalInspectionTool()