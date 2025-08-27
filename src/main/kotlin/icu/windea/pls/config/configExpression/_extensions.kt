package icu.windea.pls.config.configExpression

import icu.windea.pls.core.util.TypedTuple2
import icu.windea.pls.core.util.createKey
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.setValue

var CwtDataExpression.value: String? by createKey(CwtDataExpression.Keys)
var CwtDataExpression.intRange: TypedTuple2<Int?>? by createKey(CwtDataExpression.Keys)
var CwtDataExpression.floatRange: TypedTuple2<Float?>? by createKey(CwtDataExpression.Keys)
var CwtDataExpression.ignoreCase: Boolean? by createKey(CwtDataExpression.Keys)
