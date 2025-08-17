package icu.windea.pls.model

enum class ParadoxFileType {
    Script,
    Localisation,
    Csv,
    ModDescriptor,
    Other,
    ;

    companion object {
        @JvmStatic
        fun resolve(path: ParadoxPath, rootInfo: ParadoxRootInfo? = null): ParadoxFileType {
            return when {
                path.length == 1 && rootInfo is ParadoxRootInfo.Game -> Other
                path.matches(ParadoxPathMatcher.ModDescriptorFile) -> ModDescriptor
                path.matches(ParadoxPathMatcher.ScriptFile) -> Script
                path.matches(ParadoxPathMatcher.LocalisationFile) -> Localisation
                path.matches(ParadoxPathMatcher.CsvFile) -> Csv
                else -> Other
            }
        }

        //NOTE PLS use its own logic to resolve actual file type, so folders.cwt will be ignored
    }
}
