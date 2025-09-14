package icu.windea.pls.test

class SystemPropertyBasedFilterTestRule(private val propertyName: String) : FilterTestRule(
    "System property '$propertyName' is not present",
    { System.getProperty(propertyName) != null }
)
