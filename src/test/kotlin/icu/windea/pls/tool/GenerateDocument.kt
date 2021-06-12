package icu.windea.pls.tool

import com.intellij.openapi.application.*
import com.intellij.openapi.project.*
import icu.windea.pls.*
import icu.windea.pls.model.*
import java.io.*
import java.util.*

//用于生成markdown文档

@Volatile
private var shouldGenerate = true

fun generate(project: Project) {
	if(shouldGenerate) {
		shouldGenerate = false
		runUndoTransparentWriteAction {
			val root = "D:\\Documents\\Projects\\Dream\\Kareeze-Stories\\stellaris-mod\\documents\\generated"
			val documentNameTypeMap = mapOf(
				"权利制度" to "authority",
				"公民性" to "civic",
				"起源" to "origin",
				"政府" to "government"
			)
			generateDocuments(root, documentNameTypeMap, project)
		}
	}
}

private fun generateDocuments(root: String, documentNameTypeMap: Map<String, String>, project: Project) {
	val rootFile = File(root)
	for((name, type) in documentNameTypeMap) {
		val documentFile = rootFile.resolve(name)
		val text = getDocumentText(name, type, project)
		documentFile.writeText(text)
	}
}

private fun getDocumentText(documentName: String, type: String, project: Project): String {
	val definitions = findDefinitions(type, project).filter { it.paradoxFileInfo?.rootType == ParadoxRootType.Stdlib }
	return definitions.joinToString("\n\n", "# $documentName\n\n## Vanilla\n\n### 未分类\n\n") {
		val definition = it.paradoxDefinitionInfo
		val id = definition?.name
		val name = definition?.localisation?.find { loc -> loc.name.lowercase() == "name" }
			?.let { l-> findLocalisation(l.keyName, inferParadoxLocale(),project) }?.extractText()
		val description = definition?.localisation?.find { loc -> loc.name.lowercase() == "description" }
			?.let { l-> findLocalisation(l.keyName, inferParadoxLocale(),project) }?.extractText()
		val effect = definition?.localisation?.find { loc -> loc.name.lowercase() == "effect" }
			?.let { l-> findLocalisation(l.keyName, inferParadoxLocale(),project) }?.extractText()
		buildString {
			append("#### $name{#$id}")
			if(!description.isNullOrBlank()) append("\n\n$description")
			if(!effect.isNullOrBlank()) append("\n\n**效果：**\n\n$effect")
		}
	}
}