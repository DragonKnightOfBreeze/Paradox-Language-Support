package icu.windea.pls.extension.translation

data class TranslatableStringSnippet(
	var text: String,
	val shouldTranslate: Boolean
)