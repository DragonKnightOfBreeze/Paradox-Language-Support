package icu.windea.pls.csv

import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsIcons
import icu.windea.pls.lang.ParadoxBaseFileType

object ParadoxCsvFileType: ParadoxBaseFileType(ParadoxCsvLanguage) {
    override fun getName() = "Paradox Csv"

    override fun getDescription() = PlsBundle.message("language.name.csv")

    override fun getDefaultExtension() = "csv"

    override fun getIcon() = PlsIcons.FileTypes.ParadoxCsv
}
