package icu.windea.pls.config.config

import com.intellij.psi.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.lang.model.*

data class CwtOptionValueConfig(
    override val pointer: SmartPsiElementPointer<CwtValue>, //NOTE 目前不会用到，因此始终传入emptyPointer()以优化性能
    override val info: CwtConfigGroupInfo,
    val value: String,
    val valueType: CwtType,
    val options: List<CwtOptionConfig>? = null,
    val optionValues: List<CwtOptionValueConfig>? = null,
) : CwtConfig<CwtValue>