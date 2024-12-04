plugins {
    id("smithy-java.module-conventions")
    id("smithy-java.protocol-testing-conventions")
}

description = "This module provides the In-Memory CBOR support for servers."

extra["displayName"] = "Smithy :: Java :: Server In-Memory CBOR"
extra["moduleName"] = "software.amazon.smithy.java.server-in-memory-cbor"

dependencies {
    api(project(":server"))
    api(libs.smithy.protocol.traits)
    implementation(project(":server-core"))
    implementation(project(":context"))
    implementation(project(":core"))
    implementation(project(":rpcv2-cbor-codec"))
    implementation(project(":in-memory-api"))
}
