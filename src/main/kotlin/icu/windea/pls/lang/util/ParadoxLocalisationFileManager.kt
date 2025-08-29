package icu.windea.pls.lang.util

import icu.windea.pls.localisation.psi.ParadoxLocalisationFile

object ParadoxLocalisationFileManager {
    fun getLocaleIdFromFileName(file: ParadoxLocalisationFile): String? {
        val fileName = file.name
        if (!fileName.endsWith(".yml", true)) return null
        val dotIndex = fileName.lastIndexOf('.').let { if (it == -1) fileName.lastIndex else it }
        val prefixIndex = fileName.lastIndexOf("l_", dotIndex)
        if (prefixIndex == -1) return null
        return fileName.substring(prefixIndex, fileName.length - 4)
    }

    fun getExpectedFileName(file: ParadoxLocalisationFile, localeId: String): String {
        val fileName = file.name
        val dotIndex = fileName.lastIndexOf('.').let { if (it == -1) fileName.lastIndex else it }
        val prefixIndex = fileName.lastIndexOf("l_", dotIndex)
        if (prefixIndex == -1) {
            return fileName.substring(0, dotIndex) + "_" + localeId + ".yml"
        } else {
            return fileName.substring(0, prefixIndex) + localeId + ".yml"
        }
    }
}
