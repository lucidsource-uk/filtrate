import org.gradle.api.publish.PublishingExtension

plugins {
    id("maven-publish")
}

subprojects {
    if (!project.name.contains("test")) {
        apply(plugin = "maven-publish")
        apply(plugin = "java")

        configure<JavaPluginExtension> {
            withJavadocJar()
            withSourcesJar()
        }

        configure<PublishingExtension> {
            publications {
                create<MavenPublication>("maven") {
                    groupId = project.group as String
                    artifactId = project.name
                    version = project.version as String

                    from(components["java"])
                }
            }
        }
    }
}