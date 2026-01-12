package icu.windea.pls.model.constraints

import icu.windea.pls.model.constants.PlsConstants
import icu.windea.pls.model.paths.ParadoxPath

enum class ParadoxPathConstraint {
    InLocalisationPath {
        override fun test(path: ParadoxPath): Boolean {
            return path.root in PlsConstants.localisationRoots
        }
    },
    InNormalLocalisationPath {
        override fun test(path: ParadoxPath): Boolean {
            return path.root in PlsConstants.normalLocalisationRoots
        }
    },
    InSyncedLocalisationPath {
        override fun test(path: ParadoxPath): Boolean {
            return path.root in PlsConstants.syncedLocalisationRoots
        }
    },
    ModDescriptorFile {
        override fun test(path: ParadoxPath): Boolean {
            return !InLocalisationPath.test(path) && testFileExtension(path, "mod")
        }
    },
    ScriptFile {
        override fun test(path: ParadoxPath): Boolean {
            return !InLocalisationPath.test(path) && testFileExtension(path, PlsConstants.scriptFileExtensions)
        }
    },
    CsvFile {
        override fun test(path: ParadoxPath): Boolean {
            return !InLocalisationPath.test(path) && testFileExtension(path, PlsConstants.csvFileExtensions)
        }
    },
    LocalisationFile {
        override fun test(path: ParadoxPath): Boolean {
            return InLocalisationPath.test(path) && testFileExtension(path, PlsConstants.localisationFileExtensions)
        }
    },
    ;

    abstract fun test(path: ParadoxPath): Boolean

    @Suppress("SameParameterValue")
    protected fun testFileExtension(path: ParadoxPath, fileExtension: String): Boolean {
        return path.fileExtension?.lowercase() == fileExtension
    }

    protected fun testFileExtension(path: ParadoxPath, fileExtensions: Array<String>): Boolean {
        return path.fileExtension?.lowercase() in fileExtensions
    }
}
