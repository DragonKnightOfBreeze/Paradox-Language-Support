package icu.windea.pls.lang.psi

import icu.windea.pls.core.collections.WalkingSequenceOptions
import icu.windea.pls.core.util.createKey
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.setValue

/** 如果包含参数条件块，是否需要处理其中的子节点。 */
var WalkingSequenceOptions.inline: Boolean by createKey(WalkingSequenceOptions.Keys) { false }

/** @see inline */
infix fun WalkingSequenceOptions.inline(value: Boolean = true) = apply { inline = value }

/** 如果包含内联脚本使用，是否需要先进行内联。 */
var WalkingSequenceOptions.conditional: Boolean by createKey(WalkingSequenceOptions.Keys) { false }

/** @see conditional */
infix fun WalkingSequenceOptions.conditional(value: Boolean = true) = apply { conditional = value }
