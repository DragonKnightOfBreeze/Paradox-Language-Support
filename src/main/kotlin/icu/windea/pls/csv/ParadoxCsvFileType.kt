package icu.windea.pls.csv

import icu.windea.pls.ChronicleBundle
import icu.windea.pls.ChronicleIcons
import icu.windea.pls.lang.ParadoxFileType

object ParadoxCsvFileType : ParadoxFileType(ParadoxCsvLanguage) {
    override fun getName() = "Paradox Csv"

    override fun getDisplayName() = ChronicleBundle.message("csv.fileType.displayName")

    override fun getDescription() = ChronicleBundle.message("csv.fileType.description")

    override fun getDefaultExtension() = "csv"

    override fun getIcon() = ChronicleIcons.FileTypes.ParadoxCsv
}
