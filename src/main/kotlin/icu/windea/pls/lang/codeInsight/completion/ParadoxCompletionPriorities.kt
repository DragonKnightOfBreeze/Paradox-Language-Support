package icu.windea.pls.lang.codeInsight.completion

object ParadoxCompletionPriorities {
    const val pinned = 1000.0
    const val keyword = 10.0
    const val rootKey = 100.0
    const val constant = 90.0
    const val enumValue = 90.0
    const val complexEnumValue = 80.0
    const val systemScope = 60.0
    const val scope = 60.0
    const val linkPrefix = 70.0
    const val databaseObjectType = 70.0

    const val scopeMismatchOffset = -500
}
