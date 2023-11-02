package icu.windea.pls.extension.translation

class TranslatableStringSnippets(
	snippets: MutableList<TranslatableStringSnippet> = mutableListOf()
) : MutableList<TranslatableStringSnippet> by snippets {
	override fun toString(): String {
		return joinToString("") { it.text }
	}
}

