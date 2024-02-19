package icu.windea.pls.config.config

import com.intellij.psi.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.lang.*

class CwtLocalisationCommandConfig private constructor(
    override val pointer: SmartPsiElementPointer<out CwtProperty>,
    override val info: CwtConfigGroupInfo,
    val name: String,
    val supportedScopes: Set<String>
) : CwtConfig<CwtProperty> {
    companion object Resolver {
        fun resolve(config: CwtPropertyConfig, name: String): CwtLocalisationCommandConfig {
            val supportedScopes = buildSet {
                config.stringValue?.let { v -> add(ParadoxScopeHandler.getScopeId(v)) }
                config.values?.forEach { it.stringValue?.let { v -> add(ParadoxScopeHandler.getScopeId(v)) } }
            }.ifEmpty { ParadoxScopeHandler.anyScopeIdSet }
            return CwtLocalisationCommandConfig(config.pointer, config.info, name, supportedScopes)
        }
    }
}