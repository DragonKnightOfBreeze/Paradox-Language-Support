@file:Suppress("InspectionDescriptionNotFoundInspection", "unused")

package icu.windea.pls.localisation.inspections.vanilla

import com.intellij.codeInspection.LocalInspectionTool
import icu.windea.pls.annotations.*

@LocEditorInspection("[LocEditor:RedundantFile] File contains no active keys, does not exist in English, could be deleted")
class LocEditorRedundantFileInspection: LocalInspectionTool()

@LocEditorInspection("[LocEditor:OrphanedFile] File contains only orphaned keys")
class LocEditorOrphanedFileInspection : LocalInspectionTool()

@LocEditorInspection("[LocEditor:OrphanedKeys] Keys that do not exist in English")
class LocEditorOrphanedKeysInspection: LocalInspectionTool()

@LocEditorInspection("[LocEditor:UntranslatedKey] `{localisationKey}`")
class LocEditorUntranslatedKeyInspection: LocalInspectionTool()