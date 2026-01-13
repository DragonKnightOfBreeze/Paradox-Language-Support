@file:Suppress("NOTHING_TO_INLINE")

package icu.windea.pls.lang.psi.select

import icu.windea.pls.core.collections.WalkingSequence
import icu.windea.pls.core.collections.WalkingSequenceOptions
import icu.windea.pls.core.collections.WalkingSequenceOptionsBuilder
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.registerKey
import icu.windea.pls.core.util.setValue
import icu.windea.pls.script.psi.ParadoxScriptMember
import icu.windea.pls.script.psi.ParadoxScriptMemberContainer
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptValue

// Options

/** 如果包含参数条件块，是否需要处理其中的子节点。默认为 `false`。 */
var WalkingSequenceOptions.conditional: Boolean by registerKey(WalkingSequenceOptions.Keys) { false }

/** @see WalkingSequenceOptions.conditional */
inline infix fun WalkingSequenceOptionsBuilder.conditional(value: Boolean? = null) = apply { value?.let { options.conditional = it } }

/** 如果包含内联脚本用法，是否需要先进行内联。默认为 `false`。 */
var WalkingSequenceOptions.inline: Boolean by registerKey(WalkingSequenceOptions.Keys) { false }

/** @see WalkingSequenceOptions.inline */
inline infix fun WalkingSequenceOptionsBuilder.inline(value: Boolean? = null) = apply { value?.let { options.inline = it } }

// Builders

/** @see ParadoxPsiSequenceBuilder.members */
inline fun ParadoxScriptMemberContainer.members(conditional: Boolean? = null, inline: Boolean? = null): WalkingSequence<ParadoxScriptMember> {
    return ParadoxPsiSequenceBuilder.members(this).options { conditional(conditional) + inline(inline) }
}

/** @see ParadoxPsiSequenceBuilder.members */
inline fun ParadoxScriptMemberContainer.properties(conditional: Boolean? = null, inline: Boolean? = null): WalkingSequence<ParadoxScriptProperty> {
    return members(conditional, inline).transform { filterIsInstance<ParadoxScriptProperty>() }
}

/** @see ParadoxPsiSequenceBuilder.members */
inline fun ParadoxScriptMemberContainer.values(conditional: Boolean? = null, inline: Boolean? = null): WalkingSequence<ParadoxScriptValue> {
    return members(conditional, inline).transform { filterIsInstance<ParadoxScriptValue>() }
}
