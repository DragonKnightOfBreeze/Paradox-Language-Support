package icu.windea.pls.csv

import com.intellij.openapi.fileTypes.*
import icu.windea.pls.*

object ParadoxCsvFileType: LanguageFileType(ParadoxCsvLanguage) {
    override fun getName() = "Paradox Csv"

    override fun getDescription() = PlsBundle.message("language.name.csv")

    override fun getDefaultExtension() = "csv"

    override fun getIcon() = PlsIcons.FileTypes.ParadoxCsv
}
