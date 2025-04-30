package icu.windea.pls

import com.intellij.*
import icu.windea.pls.model.*
import org.jetbrains.annotations.*
import java.util.function.*

@NonNls
private const val BUNDLE = "messages.PlsDocBundle"

object PlsDocBundle {
    private val INSTANCE = DynamicBundle(PlsDocBundle::class.java, BUNDLE)

    @JvmStatic
    fun message(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any): @Nls String {
        return INSTANCE.getMessage(key, *params)
    }

    @JvmStatic
    fun lazyMessage(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any): Supplier<@Nls String> {
        return INSTANCE.getLazyMessage(key, *params)
    }

    @JvmStatic
    fun eventAttribute(name: String, gameType: ParadoxGameType?): @Nls String {
        return INSTANCE.messageOrNull("${gameType?.id ?: "general"}.event.attribute.$name") ?: INSTANCE.getMessage("general.event.attribute.default", name)
    }

    @JvmStatic
    fun technologyAttribute(name: String, gameType: ParadoxGameType?): @Nls String {
        return INSTANCE.messageOrNull("${gameType?.id ?: "general"}.technology.attribute.$name") ?: INSTANCE.getMessage("general.technology.attribute.default", name)
    }
}
