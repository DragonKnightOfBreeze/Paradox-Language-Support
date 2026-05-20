package icu.windea.pls.csv

import icu.windea.pls.core.loadText

object ParadoxCsvConstants {
    val colorSettingsText get() = loadText("/previewTexts/colorSettings.csv")
    val codeStyleSettingsText get() = loadText("/previewTexts/codeStyleSettings.csv")
}
