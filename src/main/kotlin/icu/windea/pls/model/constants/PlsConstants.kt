package icu.windea.pls.model.constants

import com.intellij.openapi.extensions.PluginId

object PlsConstants {
    val pluginId = PluginId.getId("icu.windea.pls")
    const val pluginSettingsFileName = "paradox-language-support.xml"

    val utf8Bom = byteArrayOf(0xef.toByte(), 0xbb.toByte(), 0xbf.toByte())

    val localisationRoots = arrayOf("localisation", "localization", "localisation_synced", "localization_synced")
    val normalLocalisationRoots = arrayOf("localisation", "localization")
    val syncedLocalisationRoots = arrayOf("localisation_synced", "localization_synced")

    val scriptFileExtensions = arrayOf("txt", "gfx", "gui", "asset", "lines", "dlc", "settings")
    val localisationFileExtensions = arrayOf("yml")
    val csvFileExtensions = arrayOf("csv")
    val imageFileExtensions = arrayOf("png", "dds", "tga")

    const val dummyIdentifier = "windea"

    // val eraseMarker = TextAttributes()
    // val onlyForegroundAttributesFlags = WithAttributesPresentation.AttributesFlags().withSkipBackground(true).withSkipEffects(true)
}
