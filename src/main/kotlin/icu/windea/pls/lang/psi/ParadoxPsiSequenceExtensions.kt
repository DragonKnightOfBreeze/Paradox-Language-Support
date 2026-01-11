package icu.windea.pls.lang.psi

import icu.windea.pls.core.collections.WalkingSequence
import icu.windea.pls.script.psi.ParadoxScriptMember
import icu.windea.pls.script.psi.ParadoxScriptMemberContainer
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptValue

fun ParadoxScriptMemberContainer.members(): WalkingSequence<ParadoxScriptMember> {
    return ParadoxPsiSequenceBuilder.members(this)
}

fun ParadoxScriptMemberContainer.properties(): WalkingSequence<ParadoxScriptProperty> {
    return ParadoxPsiSequenceBuilder.members(this).transform { filterIsInstance<ParadoxScriptProperty>() }
}

fun ParadoxScriptMemberContainer.values(): WalkingSequence<ParadoxScriptValue> {
    return ParadoxPsiSequenceBuilder.members(this).transform { filterIsInstance<ParadoxScriptValue>() }
}
