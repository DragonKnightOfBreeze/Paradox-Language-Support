package com.windea.plugin.idea.pls.cwt.editor

import com.intellij.lang.*

class CwtCommenter :Commenter{
	override fun getLineCommentPrefix() = "#"
	
	override fun getCommentedBlockCommentPrefix() = null
	
	override fun getCommentedBlockCommentSuffix() = null
	
	override fun getBlockCommentPrefix() = null
	
	override fun getBlockCommentSuffix() = null
}