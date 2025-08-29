package icu.windea.pls.model

import icu.windea.pls.core.orNull
import icu.windea.pls.model.constants.PlsConstants
import icu.windea.pls.model.paths.ParadoxPath
import icu.windea.pls.model.paths.ParadoxPathMatcher
import icu.windea.pls.model.paths.matches

enum class ParadoxFileType {
    Script,
    Localisation,
    Csv,
    ModDescriptor,
    Other,
    ;

    companion object {
        @JvmStatic
        fun resolve(path: ParadoxPath): ParadoxFileType {
            return when {
                path.matches(ParadoxPathMatcher.ModDescriptorFile) -> ModDescriptor
                path.matches(ParadoxPathMatcher.ScriptFile) -> Script
                path.matches(ParadoxPathMatcher.LocalisationFile) -> Localisation
                path.matches(ParadoxPathMatcher.CsvFile) -> Csv
                else -> Other
            }
        }

        @JvmStatic
        fun resolvePossible(fileName: String): ParadoxFileType {
            val fileExtension = fileName.substringAfterLast('.').orNull()?.lowercase() ?: return Other
            return when {
                fileExtension == "mod" -> ModDescriptor
                fileExtension in PlsConstants.scriptFileExtensions -> Script
                fileExtension in PlsConstants.localisationFileExtensions -> Localisation
                fileExtension in PlsConstants.csvFileExtensions -> Csv
                else -> Other
            }
        }

        //NOTE PLS use its own logic to resolve actual file type, so folders.cwt will be ignored
    }
}
