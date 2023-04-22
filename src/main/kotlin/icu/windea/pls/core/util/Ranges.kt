@file:Suppress("PackageDirectoryMismatch")

package icu.windea.pls.core

typealias FloatRange = ClosedRange<Float>

operator fun FloatRange.contains(element: Float?): Boolean {
	return element != null && contains(element)
}
