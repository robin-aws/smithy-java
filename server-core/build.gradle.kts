plugins {
    id("smithy-java.module-conventions")
}

description = "This module provides the core server functionality"

extra["displayName"] = "Smithy :: Java :: Server Core"
extra["moduleName"] = "software.amazon.smithy.java.server-core"

dependencies {
    api(project(":server-api"))
    api(project(":http-api"))
    api(project(":core"))
    api(project(":context"))
    api(project(":framework-errors"))
    implementation(libs.smithy.model)
    implementation(project(":io"))
    implementation(project(":logging"))
}
