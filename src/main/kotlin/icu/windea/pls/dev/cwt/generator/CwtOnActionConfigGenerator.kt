package icu.windea.pls.dev.cwt.generator

import icu.windea.pls.lang.model.*

/**
 * 用于从`on_actions.csv`生成`on_actions.cwt`
 */
class CwtOnActionConfigGenerator(
    val gameType: ParadoxGameType,
    val csvPath: String,
    val cwtPath: String,
) {
    //			val data = csvMapper.readerFor(ParadoxOnActionInfo::class.java).with(ParadoxOnActionInfo.schema)
    //				.readValues<ParadoxOnActionInfo>(virtualFile.inputStream.bufferedReader())
    //TODO 0.8.3
}