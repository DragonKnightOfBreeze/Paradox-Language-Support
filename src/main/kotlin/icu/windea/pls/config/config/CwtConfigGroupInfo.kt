package icu.windea.pls.config.config

import com.intellij.util.*
import icu.windea.pls.config.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*

data class CwtConfigGroupInfo(
    val groupName: String
) {
    lateinit var configGroup: CwtConfigGroup
    
    val filePathExpressions = mutableSetOf<CwtDataExpression>()
    
    /**
     * @see CwtDataType.Template
     * @see CwtTemplateExpression
     */
    val templateExpressions = mutableMapOf<CwtDataExpression, MutableList<CwtTemplateExpression>>()
    
    val aliasNamesSupportScope = mutableSetOf<String>()
    
    //enum[scripted_effect_params] = xxx
    val parameterConfigs = mutableListOf<CwtDataConfig<*>>()
    
    fun acceptConfigExpression(configExpression: CwtDataExpression, config: CwtConfig<*>?) {
        when(configExpression.type) {
            CwtDataType.FilePath -> {
                configExpression.value?.let { filePathExpressions.add(configExpression) }
            }
            CwtDataType.Icon -> {
                configExpression.value?.let { filePathExpressions.add(configExpression) }
            }
            CwtDataType.Template -> {
                val templateExpression = CwtTemplateExpression.resolve(configExpression.expressionString)
                for(referenceExpression in templateExpression.referenceExpressions) {
                    templateExpressions.getOrPut(referenceExpression) { SmartList() }
                        .add(templateExpression)
                }
            }
            CwtDataType.Parameter -> {
                if(config is CwtPropertyConfig) {
                    parameterConfigs.add(config)
                }
            }
            else -> pass()
        }
    }
    
    fun acceptAliasSubNameConfigExpression(name: String, configExpression: CwtDataExpression) {
        //加上可以切换作用域的alias
        if(configExpression.type == CwtDataType.ScopeField) {
            aliasNamesSupportScope.add(name)
        }
    }
}