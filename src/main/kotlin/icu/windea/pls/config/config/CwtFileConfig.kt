package icu.windea.pls.config.config

import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.psi.SmartPsiElementPointer
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.cwt.psi.CwtFile

class CwtFileConfig(
    override val pointer: SmartPsiElementPointer<CwtFile>,
    override val configGroup: CwtConfigGroup,
    val properties: List<CwtPropertyConfig>,
    val values: List<CwtValueConfig>,
    val name: String
) : UserDataHolderBase(), CwtConfig<CwtFile>
