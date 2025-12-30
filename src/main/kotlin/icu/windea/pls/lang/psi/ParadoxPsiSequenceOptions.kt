@file:Suppress("NOTHING_TO_INLINE")

package icu.windea.pls.lang.psi

import icu.windea.pls.core.collections.WalkingSequenceOptions
import icu.windea.pls.core.collections.WalkingSequenceOptionsBuilder
import icu.windea.pls.core.util.createKey
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.setValue

/** 如果包含参数条件块，是否需要处理其中的子节点。 */
var WalkingSequenceOptions.conditional: Boolean by createKey(WalkingSequenceOptions.Keys) { false }

/** @see WalkingSequenceOptions.conditional */
inline infix fun WalkingSequenceOptionsBuilder.conditional(value: Boolean = true) = apply { options.conditional = value }

/** 如果包含内联脚本用法，是否需要先进行内联。 */
var WalkingSequenceOptions.inline: Boolean by createKey(WalkingSequenceOptions.Keys) { false }

/** @see WalkingSequenceOptions.inline */
inline infix fun WalkingSequenceOptionsBuilder.inline(value: Boolean = true) = apply { options.inline = value }
