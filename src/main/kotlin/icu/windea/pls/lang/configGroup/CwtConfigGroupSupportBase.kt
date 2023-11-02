package icu.windea.pls.lang.configGroup

import icu.windea.pls.config.config.*

abstract class CwtConfigGroupSupportBase: CwtConfigGroupSupport {
    protected fun resolveDeclarationConfig(propertyConfig: CwtPropertyConfig, name: String): CwtDeclarationConfig {
        return CwtDeclarationConfig(propertyConfig.pointer, propertyConfig.info, name, propertyConfig)
    }
}
