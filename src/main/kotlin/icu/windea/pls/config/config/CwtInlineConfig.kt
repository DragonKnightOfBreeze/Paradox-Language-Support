package icu.windea.pls.config.config

import com.intellij.psi.*
import icu.windea.pls.cwt.psi.*

class CwtInlineConfig(
    override val pointer: SmartPsiElementPointer<CwtProperty>,
    override val info: CwtConfigGroupInfo,
    override val config: CwtPropertyConfig,
    override val name: String
) : CwtInlineableConfig<CwtProperty>