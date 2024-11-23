plugins {
    id("smithy-java.module-conventions")
}

description = "This module provides client in-memory transport functionality"

extra["displayName"] = "Smithy :: Java :: Client In-Memory"
extra["moduleName"] = "software.amazon.smithy.java.client-in-memory"

dependencies {
    api(project(":client-core"))
    api(project(":in-memory-api"))
    implementation(project(":logging"))

    testImplementation(project(":rpcv2-cbor-codec"))
    testImplementation(project(":aws:client-awsjson"))
    testImplementation(project(":dynamic-client"))
}
