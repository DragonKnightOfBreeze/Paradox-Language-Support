package com.windea.plugin.idea.paradox.tool

fun main() {
	val root = "D:\\Documents\\Projects\\Dream\\Kareeze-Stories\\stellaris-mod\\documents\\generated"
	val docNameTypeMap = mapOf(
		"权利制度" to "authority",
		"公民性" to "civic",
		"起源" to "origin",
		"政府" to "government"
	)
	generateDocuments(root,docNameTypeMap)
}

fun generateDocuments(root: String, docNameTypeMap: Map<String, String>) {
	
}

