@file:Suppress("unused", "CanBeParameter")

package icu.windea.pls.core.util.accessor

class TestObject(
    val name: String,
    val gender: String,
    val race: String,
    val age: Int
) {
    private var awakenStatus = false

    init {
        awake()
    }

    fun awake(): String {
        return doAwake()
    }

    private fun doAwake(): String {
        awakenStatus = true
        return "awakened"
    }

    val displayName: String get() = "$name ($gender, $race)"
    val description: String = "name: $name, gender: $gender, race: $race, age: $age"

    fun helloWorld(): String {
        return "hello world"
    }

    fun hello(text: String): String {
        return "hello $text"
    }

    fun helloAll(vararg texts: String): String {
        return "hello ${texts.joinToString(", ")}"
    }

    companion object {
        var initializedStatus = false

        @JvmStatic
        fun initialize(): String {
            return doInitialize()
        }

        private fun doInitialize(): String {
            initializedStatus = true
            return "initialized"
        }
    }
}
