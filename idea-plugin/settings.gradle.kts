/*
 * Based on intellij-streamdeck-plugin, Copyright JetBrains s.r.o. and contributors.
 * Modified by Bits, LLC. Use of this source code is governed by the Apache 2.0 license.
 */

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "klikor-ide-bridge"

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}