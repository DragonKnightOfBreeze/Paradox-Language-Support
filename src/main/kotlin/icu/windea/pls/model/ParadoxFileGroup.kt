package icu.windea.pls.model

import icu.windea.pls.core.orNull
import icu.windea.pls.model.constants.ChronicleConstants
import icu.windea.pls.model.constraints.ParadoxPathConstraint
import icu.windea.pls.model.constraints.matchesBy
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
            if (path matchesBy ParadoxPathConstraint.ModDescriptorFile) return ModDescriptor // NOTE 2.1.8 file-name-sensitive

            return when {
                path matchesBy ParadoxPathConstraint.ModDescriptorFile -> ModDescriptor
                path matchesBy ParadoxPathConstraint.ScriptFile -> Script
                path matchesBy ParadoxPathConstraint.LocalisationFile -> Localisation
                path matchesBy ParadoxPathConstraint.CsvFile -> Csv
                else -> Other
            }
        }

        @JvmStatic
        fun resolvePossible(fileName: String): ParadoxFileGroup {
            if (fileName == ChronicleConstants.descriptorModFileName) return ModDescriptor // NOTE 2.1.8 file-name-sensitive

            val fileExtension = fileName.substringAfterLast('.').orNull()?.lowercase() ?: return Other
            return when {
                fileExtension in ChronicleConstants.scriptFileExtensions -> Script
                fileExtension in ChronicleConstants.localisationFileExtensions -> Localisation
                fileExtension in ChronicleConstants.csvFileExtensions -> Csv
                else -> Other
            }
        }

        // NOTE the plugin use its own logic to resolve actual file type, so `folders.cwt` will be ignored
    }
}
