@file:Suppress("PackageDirectoryMismatch")

package icu.windea.pls.core

import kotlin.properties.*
import kotlin.reflect.*

/**
 * 可观察属性。
 * 
 * 监听另一个属性的更改，如果发生，此属性的值也会同步进行更改。
 */
class ObservableProperty<T, V>(private val target: KMutableProperty0<T>, private val transform:(T) -> V): ReadOnlyProperty<Any?, V> {
	@Volatile private var targetValue: T? = null
	@Volatile private var value: Any? = UNINITIALIZED_VALUE
	
	@Suppress("UNCHECKED_CAST")
	override fun getValue(thisRef: Any?, property: KProperty<*>): V {
		val newTargetValue = target.get()
		if(value === UNINITIALIZED_VALUE) {
			targetValue = newTargetValue
			value = transform(newTargetValue)
			return value as V
		} else {
			if(targetValue === newTargetValue) {
				return value as V
			} else {
				targetValue = newTargetValue
				value = transform(newTargetValue)
				return value as V
			}
		}
	}
}

private val UNINITIALIZED_VALUE = Any()

fun <T, V> KMutableProperty0<T>.observe(transform: (T) -> V) = ObservableProperty(this, transform)
