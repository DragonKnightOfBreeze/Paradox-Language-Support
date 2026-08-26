@file:Suppress("unused", "CanBeParameter")

package icu.windea.pls.core.accessor

class AccessorObject(
    val name: String,
    val gender: String,
    val age: Int,
) {
    private var awakenStatus = false
    var introduce = "(no introduce)"

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

    val text: String get() = "$name ($gender)"
    val description: String = "name: $name, gender: $gender, age: $age"

    fun helloWorld(): String {
        return "hello world"
    }

    fun hello(arg: String): String {
        return "hello $arg"
    }

    fun helloAll(vararg args: String): String {
        return "hello ${args.joinToString(", ")}"
    }

    companion object {
        private var initializedStatus = false
        var information = "(no information)"

        init {
            initialize()
        }

        @JvmStatic
        fun initialize(): String {
            return doInitialize()
        }

        private fun doInitialize(): String {
            initializedStatus = true
            return "initialized"
        }

        @JvmStatic
        fun greetings(): String {
            return "greetings!"
        }

        @JvmStatic
        fun greetings(arg: String): String {
            return "greetings, $arg"
        }

        @JvmStatic
        fun greetingsAll(vararg args: String): String {
            return "greetings, ${args.joinToString(", ")}"
        }
    }
}
