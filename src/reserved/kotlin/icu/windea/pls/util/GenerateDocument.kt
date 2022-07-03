package icu.windea.pls.util

import com.intellij.openapi.application.*
import com.intellij.openapi.project.*
import icu.windea.pls.*
import icu.windea.pls.model.*
import java.io.*

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
	val definitions = findAllDefinitions(type, project).filter { it.fileInfo?.rootType == ParadoxRootType.Game }
	return definitions.joinToString("\n\n", "# $documentName\n\n## Vanilla\n\n### 未分类\n\n") {
		val definitionInfo = it.definitionInfo
		val id = definitionInfo?.name!!
		val name = definitionInfo.localisation.find { loc -> loc.name.lowercase() == "name" }
			?.locationExpression?.resolve(id,it, inferParadoxLocale(), project)?.second?.let { ParadoxLocalisationTextExtractor.extract(it) }
		val description = definitionInfo.localisation.find { loc -> loc.name.lowercase() == "description" }?.locationExpression
			?.resolve(id, it, inferParadoxLocale(), project)?.second?.let { ParadoxLocalisationTextExtractor.extract(it) }
		val effect = definitionInfo.localisation.find { loc -> loc.name.lowercase() == "effect" }?.locationExpression?.resolve(id,it, inferParadoxLocale(), project)?.second?.let { ParadoxLocalisationTextExtractor.extract(it) }
		buildString {
			append("#### $name{#$id}")
			if(!description.isNullOrBlank()) append("\n\n$description")
			if(!effect.isNullOrBlank()) append("\n\n**效果：**\n\n$effect")
		}
	}
}