package com.windea.plugin.idea.paradox.script.editor

import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.project.*
import com.windea.plugin.idea.paradox.*
import java.io.*
import kotlin.concurrent.*

//用于生成markdown文档

@Volatile
private var shouldGenerate = true

fun generate(project: Project) {
	thread {
		if(shouldGenerate) {
			shouldGenerate = false
			val root = "D:\\Documents\\Projects\\Dream\\Kareeze-Stories\\stellaris-mod\\documents\\generated"
			val documentNameTypeMap = mapOf(
				"权利制度" to "authority",
				"公民性" to "civic",
				"起源" to "origin",
				"政府" to "government"
			)
			generateDocuments(root, documentNameTypeMap, project)
		}
	}.start()
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
		val definitionInfo = it.paradoxDefinitionInfo
		val id = definitionInfo?.name
		val name = definitionInfo?.localisation?.find { (k, _) -> k.value == "name" }
		val description = definitionInfo?.localisation?.find { (k, _) -> k.value == "description" }?.let { (_, v) ->
			findLocalisation(v, null, project)?.extractText()
		}
		val effect = definitionInfo?.localisation?.find { (k, _) -> k.value == "effect" }?.let { (_, v) ->
			findLocalisation(v, null, project)?.extractText()
		}
		buildString {
			append("#### $name{#$id}")
			if(!description.isNullOrBlank()) append("\n\n$description")
			if(!effect.isNullOrBlank()) append("\n\n**效果：**\n\n$effect")
		}
	}
}