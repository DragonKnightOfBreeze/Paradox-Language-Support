package icu.windea.pls.config.configExpression

import icu.windea.pls.core.util.*

var CwtDataExpression.value: String? by createKey(CwtDataExpression.Keys)
var CwtDataExpression.intRange: TypedTuple2<Int?>? by createKey(CwtDataExpression.Keys)
var CwtDataExpression.floatRange: TypedTuple2<Float?>? by createKey(CwtDataExpression.Keys)
var CwtDataExpression.ignoreCase: Boolean? by createKey(CwtDataExpression.Keys)
