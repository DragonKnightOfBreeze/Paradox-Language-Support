package icu.windea.pls.core.util

import org.jetbrains.kotlin.idea.gradleTooling.*
import kotlin.properties.*
import kotlin.reflect.*

class ObservableProperty<T, V>(private val target: KMutableProperty0<T>, private val transform:(T) -> V): ReadOnlyProperty<Any?, V> {
	@Volatile private var targetValue: T? = null
	@Volatile private var value: V? = null
	@Volatile private var initialized: Boolean = false
	
	@Suppress("UNCHECKED_CAST")
	override fun getValue(thisRef: Any?, property: KProperty<*>): V {
		val newValue = target.get()
		if(initialized) {
			if(targetValue === newValue) {
				return value as V
			} else {
				targetValue = newValue
				value = transform(newValue)
				return value as V
			}
		} else {
			targetValue = newValue
			value = transform(newValue)
			initialized = true
			return value as V
		}
	}
}

fun <T, V> KMutableProperty0<T>.observe(transform: (T) -> V) = ObservableProperty(this, transform)
