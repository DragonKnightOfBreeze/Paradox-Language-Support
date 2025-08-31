package icu.windea.pls.config.configExpression

import icu.windea.pls.core.util.TypedTuple2
import icu.windea.pls.core.util.createKey
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.setValue

// 为 CwtDataExpression 附加的常用元数据键。
// 这些键通过 UserData 存储，按需由各解析器/使用方读写。
// - value:     常量值或回退值；当解析失败回退为 Constant 时常用。
// - intRange:  整型区间（min,max），用于规则限定。
// - floatRange:浮点区间（min,max），用于规则限定。
// - ignoreCase:是否忽略大小写匹配。
var CwtDataExpression.value: String? by createKey(CwtDataExpression.Keys)
var CwtDataExpression.intRange: TypedTuple2<Int?>? by createKey(CwtDataExpression.Keys)
var CwtDataExpression.floatRange: TypedTuple2<Float?>? by createKey(CwtDataExpression.Keys)
var CwtDataExpression.ignoreCase: Boolean? by createKey(CwtDataExpression.Keys)
