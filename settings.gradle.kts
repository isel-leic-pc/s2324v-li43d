/*
 * This file was generated by the Gradle 'init' task.
 *
 * The settings file is used to specify which projects to include in your build.
 * For more detailed information on multi-project builds, please refer to https://docs.gradle.org/8.5/userguide/building_swift_projects.html in the Gradle documentation.
 */

plugins {
    // Apply the foojay-resolver plugin to allow automatic download of JDKs
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.7.0"
}

rootProject.name = "s2324v-li43d"
include("lecture-02-23-echoservers")
include("lecture-02-27-echoclient")
include("utils")
include("lecture-02-27-hazards")
include("lecture-03-01-control-synchronization")
