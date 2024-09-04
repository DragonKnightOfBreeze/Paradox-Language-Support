package icu.windea.pls.config.config

import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.cwt.psi.*

class CwtFileConfig(
    override val pointer: SmartPsiElementPointer<CwtFile>,
    override val configGroup: CwtConfigGroup,
    val properties: List<CwtPropertyConfig>,
    val values: List<CwtValueConfig>,
    val name: String
) : UserDataHolderBase(), CwtConfig<CwtFile>
