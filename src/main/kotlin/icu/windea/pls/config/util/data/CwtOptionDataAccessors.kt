package icu.windea.pls.config.util.data

import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.stringValue
import icu.windea.pls.config.configExpression.CwtCardinalityExpression

object CwtOptionDataAccessors : CwtOptionDataAccessorExtensionsAware {
    fun <T> create(action: CwtMemberConfig<*>.() -> T): CwtOptionDataAccessor<T> {
        return CwtOptionDataAccessor { action(it) }
    }

    val cardinality: CwtOptionDataAccessor<CwtCardinalityExpression?> = create {
        val option = findOption("cardinality")
        if (option == null) {
            // 如果没有注明且类型是常量，则推断为 1..~1
            if (configExpression.type == CwtDataTypes.Constant) {
                return@create CwtCardinalityExpression.resolve("1..~1")
            }
        }
        option?.stringValue?.let { s -> CwtCardinalityExpression.resolve(s) }
    }

    val cardinalityMinDefine: CwtOptionDataAccessor<String?> = create {
        findOption("cardinality_min_define")?.stringValue
    }
}
