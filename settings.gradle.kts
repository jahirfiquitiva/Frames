gradle.startParameter.excludedTaskNames.addAll(listOf(":buildSrc:testClasses"))

include(":app")
include(":library")

rootProject.name = "Frames"
