package icu.windea.pls.util.selector

import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*

object ParadoxSelectorHandler {
	tailrec fun selectGameType(from: Any?): ParadoxGameType? {
		return when {
			from == null -> null
			from is VirtualFile -> from.fileInfo?.rootInfo?.gameType
			from is PsiFile -> from.fileInfo?.rootInfo?.gameType
				?: ParadoxMagicCommentHandler.resolveFilePathComment(from)?.first
			from is ParadoxScriptVariable -> runCatching { from.stub?.gameType }.getOrNull()
				?: selectGameType(from.parent)
			from is ParadoxDefinitionProperty -> runCatching { from.getStub()?.gameType }.getOrNull()
				?: from.definitionInfo?.gameType
				?: ParadoxMagicCommentHandler.resolveDefinitionTypeComment(from)?.first //这个如果合法的话会被上一个选择逻辑覆盖
				?: selectGameType(from.parent)
			from is ParadoxLocalisationProperty -> runCatching { from.stub?.gameType }.getOrNull()
				?: selectGameType(from.parent)
			from is PsiElement -> selectGameType(from.parent)
			else -> null
		}
	}
	
	tailrec fun selectRootFile(from: Any?): VirtualFile? {
		return when {
			from == null -> null
			from is VirtualFile -> from.fileInfo?.rootFile
			from is PsiFile -> from.fileInfo?.rootFile
			from is PsiElement -> selectRootFile(from.parent)
			else -> null
		}
	}
}