package icu.windea.pls.model

import icu.windea.pls.core.orNull
import icu.windea.pls.model.constants.PlsConstants
import icu.windea.pls.model.constraints.ParadoxPathConstraint
import icu.windea.pls.model.paths.ParadoxPath

/**
 * 文件分组。
 */
enum class ParadoxFileGroup(val id: String) {
    Script("script"),
    Localisation("localisation"),
    Csv("csv"),
    ModDescriptor("mod descriptor"),
    Other("other"),
    ;

    override fun toString() = id

    companion object {
        @JvmStatic
        fun resolve(path: ParadoxPath): ParadoxFileGroup {
            return when {
                ParadoxPathConstraint.ModDescriptorFile.test(path) -> ModDescriptor
                ParadoxPathConstraint.ScriptFile.test(path) -> Script
                ParadoxPathConstraint.LocalisationFile.test(path) -> Localisation
                ParadoxPathConstraint.CsvFile.test(path) -> Csv
                else -> Other
            }
        }

        @JvmStatic
        fun resolvePossible(fileName: String): ParadoxFileGroup {
            val fileExtension = fileName.substringAfterLast('.').orNull()?.lowercase() ?: return Other
            return when {
                fileExtension == "mod" -> ModDescriptor
                fileExtension in PlsConstants.scriptFileExtensions -> Script
                fileExtension in PlsConstants.localisationFileExtensions -> Localisation
                fileExtension in PlsConstants.csvFileExtensions -> Csv
                else -> Other
            }
        }

        // NOTE PLS use its own logic to resolve actual file type, so `folders.cwt` will be ignored
    }
}
