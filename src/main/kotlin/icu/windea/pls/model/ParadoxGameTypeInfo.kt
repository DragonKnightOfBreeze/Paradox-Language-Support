package icu.windea.pls.model

import java.util.function.Supplier

data class ParadoxGameTypeInfo(
    val gameType: ParadoxGameType,
    val lazyMessage: Supplier<String>,
)
