package icu.windea.pls.csv

import icu.windea.pls.ChronicleBundle
import icu.windea.pls.PlsIcons
import icu.windea.pls.lang.ParadoxFileType

object ParadoxCsvFileType : ParadoxFileType(ParadoxCsvLanguage) {
    override fun getName() = "Paradox Csv"

    override fun getDisplayName() = ChronicleBundle.message("csv.language.name")

    override fun getDescription() = ChronicleBundle.message("csv.settings.name")

    override fun getDefaultExtension() = "csv"

    override fun getIcon() = PlsIcons.FileTypes.ParadoxCsv
}
