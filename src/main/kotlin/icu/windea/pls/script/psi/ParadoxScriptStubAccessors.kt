package icu.windea.pls.script.psi

import icu.windea.pls.core.*
import icu.windea.pls.script.psi.impl.*

val ParadoxScriptScriptedVariable.greenStub: ParadoxScriptScriptedVariableStub?
    get() = this.castOrNull<ParadoxScriptScriptedVariableImpl>()?.greenStub ?: this.stub

@Suppress("UNCHECKED_CAST")
val <T : ParadoxScriptDefinitionElement> T.stub: ParadoxScriptDefinitionElementStub<T>?
    get() = when {
        this is ParadoxScriptFile -> this.stub
        this is ParadoxScriptProperty -> this.stub
        else -> throw IllegalStateException()
    } as? ParadoxScriptDefinitionElementStub<T>?

@Suppress("UNCHECKED_CAST")
val <T : ParadoxScriptDefinitionElement> T.greenStub: ParadoxScriptDefinitionElementStub<T>?
    get() = when {
        this is ParadoxScriptFile -> this.greenStub
        this is ParadoxScriptPropertyImpl -> this.greenStub
        else -> throw IllegalStateException()
    } as? ParadoxScriptDefinitionElementStub<T>?

val ParadoxScriptProperty.greenStub: ParadoxScriptPropertyStub?
    get() = this.castOrNull<ParadoxScriptPropertyImpl>()?.greenStub ?: this.stub
