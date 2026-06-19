package icu.windea.pls.lang.codeInsight.completion

import com.intellij.codeInsight.lookup.LookupElement
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.registerKey
import icu.windea.pls.core.util.setValue
import javax.swing.Icon

object ChronicleLookupKeys : KeyRegistry()

var LookupElement.completionId: String? by registerKey(ChronicleLookupKeys)
var LookupElement.extraLookupElements: List<LookupElement>? by registerKey(ChronicleLookupKeys)
var LookupElement.priority: Double? by registerKey(ChronicleLookupKeys)
var LookupElement.patchableIcon: Icon? by registerKey(ChronicleLookupKeys)
var LookupElement.patchableTailText: String? by registerKey(ChronicleLookupKeys)
var LookupElement.localizedNames: Set<String>? by registerKey(ChronicleLookupKeys)
var LookupElement.scopeMatched: Boolean by registerKey(ChronicleLookupKeys) { true }
var LookupElement.forceInsertCurlyBraces: Boolean by registerKey(ChronicleLookupKeys) { false }
