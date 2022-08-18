package icu.windea.pls.core

import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.model.*

/**
 * 处理一些特殊注释，实现相关功能。
 */
object ParadoxMagicCommentHandler {
	/**
	 * 处理定义类型注释。
	 * * 格式：`@type:{gameType}/{type}`
	 * * 示例：`@type:stellaris/civic_or_origin`
	 * * 限制：仅当无法得到此脚本文件的文件信息（相对于模组根目录的文件路径）时才允许这样处理
	 */
	fun resolveDefinitionTypeComment(comment: PsiComment): Pair<ParadoxGameType, String>?{
		val text = comment.text.trimStart('#').trim()
		val expression = text.removePrefixOrNull("@type:") ?: return null
		val gameTypeText = expression.substringBefore('/')
		val gameType = ParadoxGameType.resolve(gameTypeText) ?: return null //这里直接返回null
		val typeText = expression.substringAfter('/')
		return gameType to typeText
	}
}