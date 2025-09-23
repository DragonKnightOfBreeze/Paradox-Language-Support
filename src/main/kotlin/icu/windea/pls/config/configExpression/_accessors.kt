package icu.windea.pls.config.configExpression

import icu.windea.pls.core.util.FloatRangeInfo
import icu.windea.pls.core.util.IntRangeInfo
import icu.windea.pls.core.util.createKey
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.setValue

var CwtDataExpression.value: String? by createKey(CwtDataExpression.Keys)
var CwtDataExpression.intRange: IntRangeInfo? by createKey(CwtDataExpression.Keys)
var CwtDataExpression.floatRange: FloatRangeInfo? by createKey(CwtDataExpression.Keys)
var CwtDataExpression.ignoreCase: Boolean? by createKey(CwtDataExpression.Keys)
var CwtDataExpression.suffixes: Set<String>? by createKey(CwtDataExpression.Keys)
