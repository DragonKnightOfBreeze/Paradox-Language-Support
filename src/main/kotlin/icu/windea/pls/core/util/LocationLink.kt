package icu.windea.pls.core.util

import icu.windea.pls.core.*

data class LocationLink(
	val sourceExpression: String,
	val targetExpression: String,
	val placeholder: String = "$"
) {
	//source -> target
	fun resolve(name: String): String? {
		val arg = resolvePlaceholder(sourceExpression, name) ?: return null
		return extractPlaceholder(targetExpression, arg)
	}
	
	//target -> source
	fun extract(name: String): String? {
		val arg = resolvePlaceholder(targetExpression, name) ?: return null
		return extractPlaceholder(sourceExpression, arg)
	}
	
	private fun resolvePlaceholder(expression: String, name: String): String? {
		return when {
			expression == placeholder -> name
			else -> {
				val index = sourceExpression.indexOf(placeholder)
				if(index == -1) return null
				name.removeSurroundingOrNull(sourceExpression.substring(0, index), sourceExpression.substring(index + 1))
			}
		}
	}
	
	private fun extractPlaceholder(expression: String, arg: String): String {
		return when {
			expression == placeholder -> arg
			else -> expression.replace(placeholder, arg)
		}
	}
}

infix fun String.linkTo(other: String) = LocationLink(this, other)