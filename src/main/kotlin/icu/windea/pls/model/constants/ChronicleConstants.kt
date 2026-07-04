package icu.windea.pls.model.constants

import com.intellij.openapi.extensions.PluginId

object ChronicleConstants {
    val pluginId = PluginId.getId("icu.windea.pls")
    const val pluginSettingsFileName = "paradox-language-support.xml"

    val localisationRoots = arrayOf("localisation", "localization", "localisation_synced", "localization_synced")
    val normalLocalisationRoots = arrayOf("localisation", "localization")
    val syncedLocalisationRoots = arrayOf("localisation_synced", "localization_synced")

    val scriptFileExtensions = arrayOf("txt", "gfx", "gui", "asset", "font", "lines", "dlc", "settings")
    val localisationFileExtensions = arrayOf("yml")
    val csvFileExtensions = arrayOf("csv")
    val imageFileExtensions = arrayOf("png", "dds", "tga")

    const val descriptorModFileName = "descriptor.mod"
    const val metadataJsonFileName = "metadata.json"
    const val launcherSettingsJsonFileName = "launcher-settings.json"
    val metadataFileNames = setOf(descriptorModFileName, metadataJsonFileName, launcherSettingsJsonFileName)

    const val dummyIdentifier = "windea"
    const val suppressInspectionsTagName = "noinspection"

    // val eraseMarker = TextAttributes()
    // val onlyForegroundAttributesFlags = WithAttributesPresentation.AttributesFlags().withSkipBackground(true).withSkipEffects(true)
}
