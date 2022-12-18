package icu.windea.pls.core.handler

import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.core.model.*
import icu.windea.pls.script.psi.*

/**
 * 用于处理一些特殊注释，实现相关功能。
 */
object ParadoxMagicCommentHandler {
	/**
	 * 处理定义类型注释。
	 * * 格式：`@type:{gameType}/{type}`
	 * * 示例：`@type:stellaris/civic_or_origin`
	 * * 仅当PLS无法得到此脚本文件的文件信息，从而进一步解析其中的定义时才允许这样处理
	 */
	@JvmStatic
	fun resolveFilePathComment(file: PsiFile): Pair<ParadoxGameType, ParadoxPath>? {
		//必须在文件的第一行，即，第一个PSI元素，之前不能有空白
		var current = file.firstChild ?: return null
		while(current !is PsiComment) {
			current = current.firstChild ?: return null
		}
		val comment = current.castOrNull<PsiComment>() ?: return null
		val commentText = comment.text.trimStart('#').trim()
		return resolveFilePathComment(commentText)
	}
	
	@JvmStatic
	fun resolveFilePathComment(commentText: String): Pair<ParadoxGameType, ParadoxPath>? {
		val expression = commentText.removePrefixOrNull("@path:") ?: return null
		val gameTypeText = expression.substringBefore(':')
		val gameType = ParadoxGameType.resolve(gameTypeText) ?: return null //这里直接返回null
		val pathText = expression.substringAfter(':')
		val path = ParadoxPath.resolve(pathText)
		return gameType to path
	}
	
	/**
	 * 处理定义类型注释。
	 * * 格式：`@type:{gameType}/{type}`
	 * * 示例：`@type:stellaris/civic_or_origin`
	 * * 仅当PLS无法得到此脚本文件的文件信息，从而进一步解析其中的定义时才允许这样处理
	 */
	@JvmStatic
	fun resolveDefinitionTypeComment(element: ParadoxScriptDefinitionElement): Pair<ParadoxGameType, String>? {
		//上一个PSI元素必须是空白，并且包含且仅包含一个换行，这意味着上一个注释在上一行
		val comment = (element.prevSibling ?: element.parent?.prevSibling)
			?.takeIf { it.isSingleLineBreak() }?.prevSibling?.castOrNull<PsiComment>() 
			?: return null
		val commentText = comment.text.trimStart('#').trim()
		return resolveDefinitionTypeComment(commentText)
	}
	
	@JvmStatic
	fun resolveDefinitionTypeComment(commentText: String): Pair<ParadoxGameType, String>? {
		val expression = commentText.removePrefixOrNull("@type:") ?: return null
		val gameTypeText = expression.substringBefore(':')
		val gameType = ParadoxGameType.resolve(gameTypeText) ?: return null //这里直接返回null
		val typeText = expression.substringAfter(':')
		return gameType to typeText
	}
}
