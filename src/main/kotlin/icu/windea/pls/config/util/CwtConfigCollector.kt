package icu.windea.pls.config.util

import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configContext.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*

object CwtConfigCollector {
    fun processConfigWithConfigExpression(config: CwtConfig<*>, configExpression: CwtDataExpression) {
        val configGroup = config.configGroup
        when (configExpression.type) {
            CwtDataTypes.FilePath -> {
                configExpression.value?.let { configGroup.filePathExpressions.add(configExpression) }
            }
            CwtDataTypes.Icon -> {
                configExpression.value?.let { configGroup.filePathExpressions.add(configExpression) }
            }
            CwtDataTypes.Parameter -> {
                if (config is CwtPropertyConfig) {
                    configGroup.parameterConfigs.add(config)
                }
            }
            else -> pass()
        }
    }
}
