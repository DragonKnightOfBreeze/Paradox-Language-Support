@file:Suppress("HasPlatformType")

package com.windea.plugin.idea.paradox.localisation.codeInsight

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.*
import com.intellij.patterns.PlatformPatterns.*
import com.intellij.util.*
import com.windea.plugin.idea.paradox.*
import com.windea.plugin.idea.paradox.localisation.psi.ParadoxLocalisationTypes.*

class ParadoxLocalisationCompletionContributor : CompletionContributor() {
	class LocaleCompletionProvider : CompletionProvider<CompletionParameters>() {
		private val lookupElements = ParadoxLocale.values.map{value->
			LookupElementBuilder.create(value.key).withTypeText(value.description)
		}

		override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
			val prefix = parameters.position.prevSibling?.text
			val fqResult = if(prefix != null) result.withPrefixMatcher(prefix) else result
			fqResult.addAllElements(lookupElements)
		}
	}

	init {
		//当用户正在输入一个locale时提示
		extend(CompletionType.BASIC, psiElement().afterSibling(psiElement(LOCALE)), LocaleCompletionProvider())
	}
}

