plugins {
    id("smithy-java.module-conventions")
}

description = "This module provides the Smithy Java In-Memory API"

extra["displayName"] = "Smithy :: Java :: In-Memory"
extra["moduleName"] = "software.amazon.smithy.java.in-memory-api"

dependencies {
    api(project(":io"))
}
