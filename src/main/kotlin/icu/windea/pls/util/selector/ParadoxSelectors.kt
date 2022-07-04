package icu.windea.pls.util.selector

import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.internal.config.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*

class ParadoxGameTypeSelector<T>(
	gameType: ParadoxGameType? = null,
	from: Any? = null
) : ParadoxSelector<T> {
	private val gameType by lazy { gameType ?: selectGameType(from) }
	
	override fun select(result: T): Boolean {
		return gameType == selectGameType(result)
	}
	
	override fun selectAll(result: T): Boolean {
		return select(result)
	}
}

class ParadoxRootFileSelector<T>(
	rootFile: VirtualFile? = null,
	from: Any? = null
) : ParadoxSelector<T> {
	private val rootFile by lazy { rootFile ?: selectRootFile(from) }
	
	override fun select(result: T): Boolean {
		return rootFile == selectRootFile(result)
	}
	
	override fun selectAll(result: T): Boolean {
		return select(result)
	}
}

class ParadoxPreferRootFileSelector<T>(
	rootFile: VirtualFile? = null,
	from: Any? = null
) : ParadoxSelector<T> {
	private val rootFile by lazy { rootFile ?: selectRootFile(from) }
	
	override fun select(result: T): Boolean {
		return rootFile == selectRootFile(result)
	}
	
	override fun selectAll(result: T): Boolean {
		return true
	}
	
	override fun selectDefault(result: T): Boolean {
		return true
	}
	
	override fun comparator(): Comparator<T> {
		return compareByNullsLast({ selectRootFile(it) }, { it.path }, { rootFile == it })
	}
}

internal tailrec fun selectGameType(from: Any?): ParadoxGameType? {
	return when {
		from == null -> null
		from is VirtualFile -> from.fileInfo?.gameType
		from is PsiFile -> from.fileInfo?.gameType
		from is ParadoxScriptVariable -> runCatching { from.stub?.gameType }.getOrElse { from.fileInfo?.gameType }
		from is ParadoxDefinitionProperty -> runCatching { from.getStub()?.gameType }.getOrElse { from.fileInfo?.gameType }
		from is ParadoxLocalisationProperty -> runCatching { from.stub?.gameType }.getOrElse { from.fileInfo?.gameType }
		from is PsiElement -> selectGameType(from.parent)
		else -> null
	}
}

internal tailrec fun selectRootFile(from: Any?): VirtualFile? {
	return when {
		from == null -> null
		from is VirtualFile -> from.fileInfo?.rootFile
		from is PsiFile -> from.fileInfo?.rootFile
		from is PsiElement -> selectRootFile(from.parent)
		else -> null
	}
}

internal fun selectLocale(from: Any?): ParadoxLocaleConfig? {
	//默认使用推断的语言区域
	return when {
		from == null -> null
		from is PsiElement -> from.localeConfig
		else -> inferParadoxLocale()
	}
}