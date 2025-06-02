package icu.windea.pls.integrations.translation

/**
 * 翻译策略。借助什么工具进行翻译。
 */
enum class PlsTranslationStrategy {
    /** 不进行任何操作 */
    Nop,
    /** 基于[Translation Plugin](https://github.com/YiiGuxing/TranslationPlugin) */
    TranslationPlugin,
    /** 基于PLS内置的AI服务 */
    AiService,
    ;
}
