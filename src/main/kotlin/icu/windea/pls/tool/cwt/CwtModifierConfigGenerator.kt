package icu.windea.pls.tool.cwt

import com.intellij.openapi.application.*
import com.intellij.openapi.fileEditor.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.openapi.vfs.*
import com.intellij.openapi.vfs.newvfs.impl.*
import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.core.model.*
import icu.windea.pls.cwt.*
import icu.windea.pls.cwt.psi.*
import java.io.*

/**
 * 用于从`modifiers.log`生成`modifiers.cwt`，并更新`modifier_categories.cwt`
 */
class CwtModifierConfigGenerator(
	val project: Project,
	val gameType: ParadoxGameType,
	val modifiersLogPath: String,
	val modifiersCwtPath: String,
	val modifierCategoriesPath: String
) {
	data class ModifierInfo(
		val name: String,
		val categories: Set<String>
	)
	
	fun generate() {
		val newDisposable = Disposer.newDisposable()
		if(ApplicationManager.getApplication().isUnitTestMode) {
			VfsRootAccess.allowRootAccess(newDisposable, File("").absolutePath)
		}
		
		val modifiersLogFile = File(modifiersLogPath)
		val modifiersCwtFile = File(modifiersCwtPath)
		val modifierCategoriesFile = File(modifierCategoriesPath)
		val regex = when(gameType) {
			ParadoxGameType.Stellaris -> """- (.*),\s*Category:\s*(.*)""".toRegex()
			else -> """Tag:(.*),\s*Categories:\s*(.*)""".toRegex()
		}
		val infos = mutableListOf<ModifierInfo>()
		modifiersLogFile.inputStream().bufferedReader().forEachLine { line ->
			val matchResult = regex.matchEntire(line) ?: return@forEachLine
			val groupValues = matchResult.groupValues
			val modifier = groupValues[1].trim()
			val categories = groupValues[2].splitToSequence(',').mapTo(mutableSetOf()) { it.trim() }
			val info = ModifierInfo(modifier, categories)
			infos.add(info)
		}
		val text = buildString {
			append("modifiers = {").appendLine()
			val categorySize = infos.maxOfOrNull { it.categories.size } ?: 0
			infos.forEach { (name, categories) ->
				append("    ")
				append(name)
				append(" = ")
				if(categorySize == 1) {
					append(categories.single().quoteIfNecessary())
				} else {
					append("{")
					categories.forEach { category -> append(" ").append(category.quoteIfNecessary()) }
					append(" }")
				}
				appendLine()
			}
			appendLine("}")
		}
		modifiersCwtFile.writeText(text)
		
		val allCategories = infos.flatMapTo(mutableSetOf()) { it.categories }
		
		if(!modifierCategoriesFile.exists()) {
			modifierCategoriesFile.writeText("modifier_categories = {}")
		}
		
		val fileDocumentManager = FileDocumentManager.getInstance()
		val psiDocumentManager = PsiDocumentManager.getInstance(project)
		
		val modifierCategoriesVirtualFile = VfsUtil.findFileByIoFile(modifierCategoriesFile, true)!!
		val modifierCategoriesDocument = fileDocumentManager.getDocument(modifierCategoriesVirtualFile)!!
		val modifierCategoriesPsiFile = modifierCategoriesVirtualFile.toPsiFile<CwtFile>(project)!!
		val rootElement = modifierCategoriesPsiFile
			.findChild<CwtRootBlock>()!!
			.findChildOfType<CwtProperty> { it.name == "modifier_categories" }!!
		val categoryElements = rootElement
			.findChild<CwtBlock>()!!
			.findChildrenOfType<CwtProperty>()
		val categories = categoryElements.map { it.name }.toSet()
		categoryElements.forEach { categoryElement ->
			if(categoryElement.name !in allCategories) categoryElement.delete()
		}
		val newCategories = allCategories.toMutableSet()
		newCategories.removeAll(categories)
		if(newCategories.isNotEmpty()) {
			val textToInsert = newCategories.joinToString("\n") {
				"${it.quoteIfNecessary()} = {\nsupported_scopes = { any }\n}"
			}
			val newRootBlock = PsiFileFactory.getInstance(project).createFileFromText(CwtLanguage, textToInsert)
				.cast<CwtFile>()
				.findChild<CwtRootBlock>()!!
			val author = rootElement
				.findChild<CwtBlock>()!!
				.findChildOfType<CwtProperty>(false)
				?: rootElement.findChild<CwtBlock>()!!.findChild(CwtElementTypes.LEFT_BRACE)
			rootElement
				.findChild<CwtBlock>()!!
				.addRangeAfter(newRootBlock.firstChild, newRootBlock.lastChild, author)
			modifierCategoriesPsiFile.reformatted()
		}
		
		psiDocumentManager.doPostponedOperationsAndUnblockDocument(modifierCategoriesDocument)
		newDisposable.dispose()
	}
}