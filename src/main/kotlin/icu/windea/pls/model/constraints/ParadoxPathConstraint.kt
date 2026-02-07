package icu.windea.pls.model.constraints

import icu.windea.pls.model.constants.PlsConstants
import icu.windea.pls.model.paths.ParadoxPath
import icu.windea.pls.model.paths.matchesExtension
import icu.windea.pls.model.paths.matchesExtensions
import icu.windea.pls.model.paths.matchesParent

@Suppress("unused")
enum class ParadoxPathConstraint {
    InLocalisationPath {
        override fun test(path: ParadoxPath) = path.root in PlsConstants.localisationRoots
    },
    InNormalLocalisationPath {
        override fun test(path: ParadoxPath) = path.root in PlsConstants.normalLocalisationRoots
    },
    InSyncedLocalisationPath {
        override fun test(path: ParadoxPath) = path.root in PlsConstants.syncedLocalisationRoots
    },

    ModDescriptorFile {
        override fun test(path: ParadoxPath) = !InLocalisationPath.test(path) && path.matchesExtension("mod")
    },
    ScriptFile {
        override fun test(path: ParadoxPath) = !InLocalisationPath.test(path) && path.matchesExtensions(PlsConstants.scriptFileExtensions)
    },
    CsvFile {
        override fun test(path: ParadoxPath) = !InLocalisationPath.test(path) && path.matchesExtensions(PlsConstants.csvFileExtensions)
    },
    LocalisationFile {
        override fun test(path: ParadoxPath) = InLocalisationPath.test(path) && path.matchesExtensions(PlsConstants.localisationFileExtensions)
    },

    ForDefine {
        override fun test(path: ParadoxPath) = path.matchesParent("common/defines") && path.matchesExtension("txt")
    },
    ForScriptedVariable {
        override fun test(path: ParadoxPath) = path.matchesParent("common/scripted_variables") && path.matchesExtension("txt")
    },

    AcceptInlineScriptUsage {
        override fun test(path: ParadoxPath) = ScriptFile.test(path) && !path.matchesParent("common/defines") && !path.matchesParent("common/scripted_variables")
    },
    AcceptDefinitionInjection {
        override fun test(path: ParadoxPath) = ScriptFile.test(path) && !path.matchesParent("common/defines") && !path.matchesParent("common/scripted_variables")
    }
    ;

    abstract fun test(path: ParadoxPath): Boolean
}
