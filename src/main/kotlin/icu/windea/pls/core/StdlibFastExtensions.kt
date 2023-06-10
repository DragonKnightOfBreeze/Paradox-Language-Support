package icu.windea.pls.core

fun String.trimFast(c: Char): String {
    var startIndex = 0
    var endIndex = length - 1
    var startFound = false
    while(startIndex <= endIndex) {
        val index = if(!startFound) startIndex else endIndex
        val match = this[index] == c
        if(!startFound) {
            if(!match)
                startFound = true
            else
                startIndex += 1
        } else {
            if(!match)
                break
            else
                endIndex -= 1
        }
    }
    return substring(startIndex, endIndex + 1)
}
