plugins {
    id("smithy-java.examples-conventions")
    alias(libs.plugins.jmh)
}

dependencies {
    // Model dependencies
    api(project(":in-memory-api"))

    // Server dependencies
    api(project(":server"))
    api(project(":server-core"))
    api(project(":server-in-memory-cbor"))
    api(project(":server-aws-rest-json1"))
    implementation(project(":server-netty"))

    // Client dependencies
    api(project(":aws:client-restjson"))
    api(project(":client-in-memory-cbor"))

    // Common dependencies
    api(project(":core"))
    api(libs.smithy.aws.traits)
}

jmh {
    warmupIterations = 2
    iterations = 5
    fork = 1
    // profilers.add("async:output=flamegraph")
    // profilers.add('gc')
}

// Disable spotbugs
tasks {
    spotbugsMain {
        enabled = false
    }

    spotbugsIt {
        enabled = false
    }
}
