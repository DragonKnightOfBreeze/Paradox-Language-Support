package com.windea.plugin.idea.pls.localisation.editor

import com.intellij.lang.*

class ParadoxLocalisationCommenter : Commenter {
	override fun getLineCommentPrefix() = "#"

	override fun getCommentedBlockCommentPrefix() = null

	override fun getCommentedBlockCommentSuffix() = null

	override fun getBlockCommentPrefix() = null

	override fun getBlockCommentSuffix() = null
}
