plugins {
    id("smithy-java.examples-conventions")
    // Package smithy models alongside jar for downstream consumers
    alias(libs.plugins.smithy.gradle.jar)
}

dependencies {
    api(project(":core"))

    itImplementation(libs.hamcrest)
}
