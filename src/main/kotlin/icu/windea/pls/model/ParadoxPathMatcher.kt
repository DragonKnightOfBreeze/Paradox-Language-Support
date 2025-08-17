package icu.windea.pls.model

import icu.windea.pls.model.constants.PlsConstants

@Suppress("unused", "SameParameterValue")
enum class ParadoxPathMatcher {
    InLocalisationPath {
        private val roots = arrayOf("localisation", "localization", "localisation_synced", "localization_synced")
        override fun matches(path: ParadoxPath) = path.root in roots
    },
    InNormalLocalisationPath {
        private val roots = arrayOf("localisation", "localization")
        override fun matches(path: ParadoxPath) = path.root in roots
    },
    InSyncedLocalisationPath {
        private val roots = arrayOf("localisation_synced", "localization_synced")
        override fun matches(path: ParadoxPath) = path.root in roots
    },
    ModDescriptorFile {
        override fun matches(path: ParadoxPath): Boolean {
            return !InLocalisationPath.matches(path) && matchFileExtension(path, "mod")
        }
    },
    ScriptFile {
        override fun matches(path: ParadoxPath): Boolean {
            return !InLocalisationPath.matches(path) && matchFileExtension(path, PlsConstants.scriptFileExtensions)
        }
    },
    CsvFile {
        override fun matches(path: ParadoxPath): Boolean {
            return !InLocalisationPath.matches(path) && matchFileExtension(path, PlsConstants.csvFileExtensions)
        }
    },
    LocalisationFile {
        override fun matches(path: ParadoxPath): Boolean {
            return InLocalisationPath.matches(path) && matchFileExtension(path, PlsConstants.localisationFileExtensions)
        }
    },
    ;

    protected fun matchFileExtension(path: ParadoxPath, fileExtension: String): Boolean {
        return path.fileExtension?.lowercase() == fileExtension
    }

    protected fun matchFileExtension(path: ParadoxPath, fileExtensions: Array<String>): Boolean {
        return path.fileExtension?.lowercase() in fileExtensions
    }

    abstract fun matches(path: ParadoxPath): Boolean
}
