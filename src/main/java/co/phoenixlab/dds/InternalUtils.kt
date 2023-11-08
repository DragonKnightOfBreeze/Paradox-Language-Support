package co.phoenixlab.dds

import java.util.*
import java.util.function.*

/**
 * Internal util class.
 */
internal object InternalUtils {
	@JvmStatic fun <T> bitsToSet(bits: Int, tClass: Class<T>): Set<T> where T : Enum<T>, T : IntSupplier {
		val ret = EnumSet.noneOf(tClass)
		val enums = tClass.enumConstants
		for(t in enums) {
			if(bits and t.asInt != 0) {
				ret.add(t)
			}
		}
		return ret
	}
	
	@JvmStatic fun <T> bitsToUnmodifiableSet(bits: Int, tClass: Class<T>): Set<T> where T : Enum<T>, T : IntSupplier {
		return Collections.unmodifiableSet(bitsToSet(bits, tClass))
	}
	
	@JvmStatic inline fun <T> verifyThat(t: T, message: String, predicate: (T) -> Boolean) {
		verifyThatNot(t, message) { !predicate(it) }
	}
	
	@JvmStatic inline fun <T> verifyThatNot(t: T, message: String, predicate: (T) -> Boolean) {
		if(predicate(t)) {
			throw InvalidDdsException(message)
		}
	}
}