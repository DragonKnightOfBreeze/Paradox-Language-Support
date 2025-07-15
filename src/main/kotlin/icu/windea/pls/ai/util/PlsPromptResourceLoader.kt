package icu.windea.pls.ai.util

import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader

class PlsPromptResourceLoader : ClasspathResourceLoader() //这里的继承是必要的，否则找到的 classLoader 会不正确
