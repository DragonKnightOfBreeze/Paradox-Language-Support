package icu.windea.pls.model.constants

import icu.windea.pls.core.loadText

object PlsPreviewTexts {
    val cwtColorSettings get() = loadText("/previewTexts/colorSettings.cwt")
    val cwtCodeStyleSettings get() = loadText("/previewTexts/codeStyleSettings.cwt")

    val scriptColorSettings get() = loadText("/previewTexts/colorSettings.txt")
    val scriptCodeStyleSettings get() = loadText("/previewTexts/codeStyleSettings.txt")

    val localisationColorSettings get() = loadText("/previewTexts/colorSettings.yml")
    val localisationCodeStyleSettings get() = loadText("/previewTexts/codeStyleSettings.yml")

    val csvColorSettings get() = loadText("/previewTexts/colorSettings.csv")
    val csvCodeStyleSettings get() = loadText("/previewTexts/codeStyleSettings.csv")
}
