package icu.windea.pls.model.constraints

import icu.windea.pls.model.constants.ChronicleConstants
import icu.windea.pls.model.paths.ParadoxPath

/**
 * @see ParadoxPath
 */
@Suppress("unused")
enum class ParadoxPathConstraint {
    InLocalisationPath {
        override fun test(path: ParadoxPath) = path.root in ChronicleConstants.localisationRoots
    },
    InNormalLocalisationPath {
        override fun test(path: ParadoxPath) = path.root in ChronicleConstants.normalLocalisationRoots
    },
    InSyncedLocalisationPath {
        override fun test(path: ParadoxPath) = path.root in ChronicleConstants.syncedLocalisationRoots
    },

    ModDescriptorFile {
        override fun test(path: ParadoxPath) = path.fileName == ChronicleConstants.descriptorModFileName // NOTE 2.1.8 file-name-sensitive
    },
    ScriptFile {
        override fun test(path: ParadoxPath) = !InLocalisationPath.test(path) && path.matchesExtensions(ChronicleConstants.scriptFileExtensions)
    },
    CsvFile {
        override fun test(path: ParadoxPath) = !InLocalisationPath.test(path) && path.matchesExtensions(ChronicleConstants.csvFileExtensions)
    },
    LocalisationFile {
        override fun test(path: ParadoxPath) = InLocalisationPath.test(path) && path.matchesExtensions(ChronicleConstants.localisationFileExtensions)
    },

    ForDefine {
        override fun test(path: ParadoxPath) = path.matchesParent("common/defines") && path.matchesExtension("txt")
    },
    ForScriptedVariable {
        override fun test(path: ParadoxPath) = path.matchesParent("common/scripted_variables") && path.matchesExtension("txt")
    },
    ForEvent {
        override fun test(path: ParadoxPath) = path.matchesParent("events") && path.matchesExtension("txt")
    },

    ForNormalLocalisation {
        override fun test(path: ParadoxPath) = InNormalLocalisationPath.test(path) && path.matchesExtensions(ChronicleConstants.localisationFileExtensions)
    },
    ForSyncedLocalisation {
        override fun test(path: ParadoxPath) = InSyncedLocalisationPath.test(path) && path.matchesExtensions(ChronicleConstants.localisationFileExtensions)
    },

    AcceptInlineScriptUsage {
        override fun test(path: ParadoxPath) = ScriptFile.test(path) && !path.matchesParent("common/defines") && !path.matchesParent("common/scripted_variables")
    },
    AcceptDefinitionInjection {
        override fun test(path: ParadoxPath) = ScriptFile.test(path) && !path.matchesParent("common/defines") && !path.matchesParent("common/scripted_variables")
    },
    ;

    abstract fun test(path: ParadoxPath): Boolean
}
