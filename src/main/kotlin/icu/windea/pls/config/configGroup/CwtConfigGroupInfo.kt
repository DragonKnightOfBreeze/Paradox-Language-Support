package icu.windea.pls.config.configGroup

import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.model.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.util.*

data class CwtConfigGroupInfo(
    val groupName: String
) {
    lateinit var configGroup: CwtConfigGroup
    
    val filePathExpressions = mutableSetOf<CwtDataExpression>()
    
    /**
     * @see CwtDataTypes.Template
     * @see CwtTemplateExpression
     */
    val templateExpressions = mutableMapOf<CwtDataExpression, MutableList<CwtTemplateExpression>>()
    
    val aliasNamesSupportScope = mutableSetOf<String>()
    
    //$parameter = xxx
    val parameterConfigs = mutableListOf<CwtMemberConfig<*>>()
    
    fun acceptConfigExpression(configExpression: CwtDataExpression, config: CwtConfig<*>?) {
        when(configExpression.type) {
            CwtDataTypes.FilePath -> {
                configExpression.value?.let { filePathExpressions.add(configExpression) }
            }
            CwtDataTypes.Icon -> {
                configExpression.value?.let { filePathExpressions.add(configExpression) }
            }
            CwtDataTypes.Template -> {
                val templateExpression = CwtTemplateExpression.resolve(configExpression.expressionString)
                for(referenceExpression in templateExpression.referenceExpressions) {
                    templateExpressions.getOrPut(referenceExpression) { mutableListOf() }
                        .add(templateExpression)
                }
            }
            CwtDataTypes.Parameter -> {
                if(config is CwtPropertyConfig) {
                    parameterConfigs.add(config)
                }
            }
            else -> pass()
        }
    }
    
    fun acceptAliasSubNameConfigExpression(name: String, configExpression: CwtDataExpression) {
        //加上可以切换作用域的alias
        if(configExpression.type == CwtDataTypes.ScopeField) {
            aliasNamesSupportScope.add(name)
        }
    }
}