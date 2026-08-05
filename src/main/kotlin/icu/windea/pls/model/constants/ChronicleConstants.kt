package icu.windea.pls.model.constants

import com.intellij.openapi.extensions.PluginId

object ChronicleConstants {
    val pluginId = PluginId.getId("icu.windea.pls")
    const val pluginSettingsFileName = "paradox-language-support.xml"

    val localisationRoots = arrayOf("localisation", "localization", "localisation_synced", "localization_synced")
    val normalLocalisationRoots = arrayOf("localisation", "localization")
    val syncedLocalisationRoots = arrayOf("localisation_synced", "localization_synced")

    val scriptFileExtensions = arrayOf("txt", "gfx", "gui", "asset", "font", "lines", "dlc", "settings", "map", "heightmap")
    val localisationFileExtensions = arrayOf("yml")
    val csvFileExtensions = arrayOf("csv")
    val imageFileExtensions = arrayOf("png", "dds", "tga")

    const val descriptorModFileName = "descriptor.mod"
    const val metadataJsonFileName = "metadata.json"
    const val launcherSettingsJsonFileName = "launcher-settings.json"
    val metadataFileNames = arrayOf(descriptorModFileName, metadataJsonFileName, launcherSettingsJsonFileName)

    // in order to be compatible with eu5 config files
    val configFilePathPrefixes = arrayOf("game/", "game/in_game/", "game/main_menu/", "game/loading_screen/")

    const val dummyIdentifier = "windea"
    const val suppressInspectionsTagName = "noinspection"

    // val eraseMarker = TextAttributes()
    // val onlyForegroundAttributesFlags = WithAttributesPresentation.AttributesFlags().withSkipBackground(true).withSkipEffects(true)
}
