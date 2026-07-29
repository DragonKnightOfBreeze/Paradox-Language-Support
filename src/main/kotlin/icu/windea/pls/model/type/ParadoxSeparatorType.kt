package icu.windea.pls.model.type

import icu.windea.pls.core.optimizer.ByteOptimizer
import icu.windea.pls.core.optimizer.OptimizerFactory

enum class ParadoxSeparatorType(val text: String) {
    Equal("="),
    NotEqual("!="),
    Lt("<"),
    Gt(">"),
    Le("<="),
    Ge(">="),

    // #86 supported in ck3, vic3 and eu5 (preferred format: `k ?= v`)
    SafeAssign("? ="),
    // 2.1.10 #331 supported in stellaris 4.4 (preferred format: `k? = v`)
    SafeCallAssign("?="),
    ;

    override fun toString() = text

    companion object {
        private val optimizer = OptimizerFactory.create({ it.ordinal.toByte() }, { entries[it.toInt()] })

        @JvmStatic
        fun optimizer(): ByteOptimizer<ParadoxSeparatorType> = optimizer
    }
}
