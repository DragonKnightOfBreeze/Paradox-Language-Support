package icu.windea.pls.config.configExpression

import icu.windea.pls.core.util.FloatRangeInfo
import icu.windea.pls.core.util.IntRangeInfo
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.registerKey
import icu.windea.pls.core.util.setValue

// region CwtDataExpression Accessors

var CwtDataExpression.wildcard: Boolean by registerKey(CwtDataExpression.Keys) { false }
var CwtDataExpression.ignoreCase: Boolean by registerKey(CwtDataExpression.Keys) { false }
var CwtDataExpression.intRange: IntRangeInfo? by registerKey(CwtDataExpression.Keys)
var CwtDataExpression.floatRange: FloatRangeInfo? by registerKey(CwtDataExpression.Keys)
var CwtDataExpression.suffixes: Set<String>? by registerKey(CwtDataExpression.Keys)

// endregion
