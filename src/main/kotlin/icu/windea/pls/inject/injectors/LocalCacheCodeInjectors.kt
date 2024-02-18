package icu.windea.pls.inject.injectors

import icu.windea.pls.inject.*
import icu.windea.pls.inject.annotations.*

//这些代码注入器用于缓存某些数据

@InjectTarget("icu.windea.pls.config.expression.CwtCardinalityExpression\$Resolver", pluginId = "icu.windea.pls")
@InjectLocalCache("resolve")
class CwtCardinalityExpressionResolverCodeInjector : CodeInjectorBase()