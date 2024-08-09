package icu.windea.pls.config.configGroup

@MustBeDocumented
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.PROPERTY)
annotation class Tags(vararg val value: Tag)

enum class Tag {
    BuiltIn, Extended, Computed, Collected
}
