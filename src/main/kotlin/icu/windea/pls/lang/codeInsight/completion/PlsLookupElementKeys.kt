package icu.windea.pls.lang.codeInsight.completion

import com.intellij.codeInsight.lookup.LookupElement
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.createKey
import icu.windea.pls.core.util.registerKey
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.setValue
import javax.swing.Icon

object PlsLookupElementKeys : KeyRegistry()

var LookupElement.completionId: String? by registerKey(PlsLookupElementKeys)
var LookupElement.extraLookupElements: List<LookupElement>? by registerKey(PlsLookupElementKeys)

var LookupElement.priority: Double? by registerKey(PlsLookupElementKeys)
var LookupElement.patchableIcon: Icon? by registerKey(PlsLookupElementKeys)
var LookupElement.patchableTailText: String? by registerKey(PlsLookupElementKeys)
var LookupElement.localizedNames: Set<String>? by registerKey(PlsLookupElementKeys)
var LookupElement.scopeMatched: Boolean by registerKey(PlsLookupElementKeys) { true }
var LookupElement.forceInsertCurlyBraces: Boolean by registerKey(PlsLookupElementKeys) { false }
