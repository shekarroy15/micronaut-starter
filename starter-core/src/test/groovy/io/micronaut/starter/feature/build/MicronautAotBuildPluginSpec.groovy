package io.micronaut.starter.feature.build

import io.micronaut.core.version.SemanticVersion
import io.micronaut.starter.ApplicationContextSpec
import io.micronaut.starter.BuildBuilder
import io.micronaut.starter.fixture.CommandOutputFixture
import io.micronaut.starter.options.BuildTool
import io.micronaut.starter.options.Language
import spock.lang.Unroll

import static io.micronaut.starter.application.ApplicationType.DEFAULT
import static io.micronaut.starter.options.BuildTool.GRADLE
import static io.micronaut.starter.options.BuildTool.GRADLE_KOTLIN
import static io.micronaut.starter.options.BuildTool.MAVEN

class MicronautAotBuildPluginSpec extends ApplicationContextSpec implements CommandOutputFixture {

    private static final String GRADLE_PLUGIN_VERSION = '3.3.2'
    private static final String AOT_PLUGIN = 'id("io.micronaut.aot") version "' + GRADLE_PLUGIN_VERSION + '"'
    private static final String APP_PLUGIN = 'id("io.micronaut.application") version "' + GRADLE_PLUGIN_VERSION + '"'

    @Unroll
    void 'application with gradle and feature micronaut-aot for language=#language'() {
        when:
        String output = build(GRADLE, language)

        then:
        output.contains(AOT_PLUGIN)
        output.contains('aot {')
        output.contains('optimizeServiceLoading = true')
        output.contains('convertYamlToJava = true')
        output.contains('precomputeOperations = true')
        output.contains('cacheEnvironment = true')
        output.contains('optimizeClassLoading = true')
        output.contains('deduceEnvironment = true')

        when:
        Optional<SemanticVersion> semanticVersion = parseSemanticVersion(output, "version = '", "'")

        then:
        semanticVersion.isPresent()

        where:
        language << Language.values().toList()
    }

    @Unroll
    void 'application with gradle and feature micronaut-aot for language=#language and Kotlin DSL'() {
        when:
        String output = build(GRADLE_KOTLIN, language)

        then:
        output.contains(AOT_PLUGIN)
        output.contains('aot {')
        output.contains('optimizeServiceLoading.set(true)')
        output.contains('convertYamlToJava.set(true)')
        output.contains('precomputeOperations.set(true)')
        output.contains('cacheEnvironment.set(true)')
        output.contains('optimizeClassLoading.set(true)')
        output.contains('deduceEnvironment.set(true)')

        when:
        Optional<SemanticVersion> semanticVersion = parseSemanticVersion(output, 'version.set("', '")')

        then:
        semanticVersion.isPresent()

        where:
        language << Language.values().toList()
    }

    static Optional<SemanticVersion> parseSemanticVersion(String output, String versionPrefix, String suffix) {
        if (!output.contains(versionPrefix)) {
            return Optional.empty()
        }

        String version = output.substring(output.indexOf(versionPrefix) + versionPrefix.length())

        if (version.indexOf(suffix) == -1) {
            return Optional.empty()
        }

        try {
            return Optional.of(new SemanticVersion(version.substring(0, version.indexOf(suffix))))
        } catch (IllegalArgumentException e) {
            return Optional.empty()
        }
    }

    @Unroll
    void 'order is correct in application with gradle and feature micronaut-aot for language=#language'() {
        when:
        String output = build(GRADLE, language)

        then:
        output.contains(AOT_PLUGIN)
        output.contains(APP_PLUGIN)
        output.indexOf(APP_PLUGIN) < output.indexOf(AOT_PLUGIN)

        where:
        language << Language.values().toList()
    }

    @Unroll
    void 'order is correct in application with gradle and feature micronaut-aot for language=#language and Kotlin DSL'() {
        when:
        String output = build(GRADLE_KOTLIN, language)

        then:
        output.contains(AOT_PLUGIN)
        output.contains(APP_PLUGIN)
        output.indexOf(APP_PLUGIN) < output.indexOf(AOT_PLUGIN)

        where:
        language << Language.values().toList()
    }

    @Unroll
    void 'application with maven and feature micronaut-aot for language=#language'() {
        when:
        String output = build(MAVEN, language)

        then:
        output.contains("<micronaut.aot.packageName>example.micronaut</micronaut.aot.packageName>")

        where:
        language << Language.values().toList()
    }

    private String build(BuildTool buildTool, Language language) {
        new BuildBuilder(beanContext, buildTool)
                .language(language)
                .applicationType(DEFAULT)
                .features(['micronaut-aot'])
                .render()
    }
}
