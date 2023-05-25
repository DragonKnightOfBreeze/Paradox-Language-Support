package icu.windea.pls.inject.injectors

import icu.windea.pls.inject.*
import icu.windea.pls.inject.annotations.*

//这些代码注入器用于优化性能

@InjectTarget("icu.windea.pls.script.psi.impl.ParadoxScriptPropertyImpl", pluginId = "icu.windea.pls")
@FieldCacheMethods(methods = ["getText", "getName", "getValue", "getType"], cleanUpMethod = "subtreeChanged")
class ParadoxScriptPropertyCodeInjector : BaseCodeInjector()

@InjectTarget("icu.windea.pls.script.psi.impl.ParadoxScriptPropertyKeyImpl", pluginId = "icu.windea.pls")
@FieldCacheMethods(methods = ["getText", "getValue", "getType"], cleanUpMethod = "subtreeChanged")
class ParadoxScriptPropertyKeyCodeInjector : BaseCodeInjector()

@InjectTarget("icu.windea.pls.script.psi.impl.ParadoxScriptBooleanImpl", pluginId = "icu.windea.pls")
@FieldCacheMethods(methods = ["getText", "getValue", "getBooleanValue"], cleanUpMethod = "subtreeChanged")
class ParadoxScriptBooleanCodeInjector : BaseCodeInjector()

@InjectTarget("icu.windea.pls.script.psi.impl.ParadoxScriptIntImpl", pluginId = "icu.windea.pls")
@FieldCacheMethods(methods = ["getText", "getValue", "getIntValue"], cleanUpMethod = "subtreeChanged")
class ParadoxScriptIntCodeInjector : BaseCodeInjector()

@InjectTarget("icu.windea.pls.script.psi.impl.ParadoxScriptFloatImpl", pluginId = "icu.windea.pls")
@FieldCacheMethods(methods = ["getText", "getValue", "getFloatValue"], cleanUpMethod = "subtreeChanged")
class ParadoxScriptFloatCodeInjector : BaseCodeInjector()

@InjectTarget("icu.windea.pls.script.psi.impl.ParadoxScriptStringImpl", pluginId = "icu.windea.pls")
@FieldCacheMethods(methods = ["getText", "getValue", "getStringValue"], cleanUpMethod = "subtreeChanged")
class ParadoxScriptStringCodeInjector : BaseCodeInjector()

@InjectTarget("icu.windea.pls.script.psi.impl.ParadoxScriptColorImpl", pluginId = "icu.windea.pls")
@FieldCacheMethods(methods = ["getText", "getValue", "getColorType", "getColorArgs"], cleanUpMethod = "subtreeChanged")
class ParadoxScriptColorCodeInjector : BaseCodeInjector()