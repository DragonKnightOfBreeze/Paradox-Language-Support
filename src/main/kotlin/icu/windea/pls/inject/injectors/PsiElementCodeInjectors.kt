package icu.windea.pls.inject.injectors

import icu.windea.pls.inject.*
import icu.windea.pls.inject.annotations.*

//这些代码注入器用于优化性能

@InjectTarget("icu.windea.pls.script.psi.impl.ParadoxScriptPropertyImpl", pluginId = "icu.windea.pls")
@FieldCacheMethods(methods = ["getText", "getValue", "getName"], cleanupMethod = "subtreeChanged")
class ParadoxScriptPropertyCodeInjector : BaseCodeInjector()

@InjectTarget("icu.windea.pls.script.psi.impl.ParadoxScriptPropertyKeyImpl", pluginId = "icu.windea.pls")
@FieldCacheMethods(methods = ["getText", "getValue"], cleanupMethod = "subtreeChanged")
class ParadoxScriptPropertyKeyCodeInjector : BaseCodeInjector()

@InjectTarget("icu.windea.pls.script.psi.impl.ParadoxScriptBooleanImpl", pluginId = "icu.windea.pls")
@FieldCacheMethods(methods = ["getText", "getValue", "getBooleanValue"], cleanupMethod = "subtreeChanged")
class ParadoxScriptBooleanCodeInjector : BaseCodeInjector()

@InjectTarget("icu.windea.pls.script.psi.impl.ParadoxScriptIntImpl", pluginId = "icu.windea.pls")
@FieldCacheMethods(methods = ["getText", "getValue", "getIntValue"], cleanupMethod = "subtreeChanged")
class ParadoxScriptIntCodeInjector : BaseCodeInjector()

@InjectTarget("icu.windea.pls.script.psi.impl.ParadoxScriptFloatImpl", pluginId = "icu.windea.pls")
@FieldCacheMethods(methods = ["getText", "getValue", "getFloatValue"], cleanupMethod = "subtreeChanged")
class ParadoxScriptFloatCodeInjector : BaseCodeInjector()

@InjectTarget("icu.windea.pls.script.psi.impl.ParadoxScriptStringImpl", pluginId = "icu.windea.pls")
@FieldCacheMethods(methods = ["getText", "getValue", "getStringValue"], cleanupMethod = "subtreeChanged")
class ParadoxScriptStringCodeInjector : BaseCodeInjector()

@InjectTarget("icu.windea.pls.script.psi.impl.ParadoxScriptColorImpl", pluginId = "icu.windea.pls")
@FieldCacheMethods(methods = ["getText", "getValue", "getColorType", "getColorArgs"], cleanupMethod = "subtreeChanged")
class ParadoxScriptColorCodeInjector : BaseCodeInjector()