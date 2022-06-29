import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val mainPkgAndClass = "com.augenda.services.templateservice.TemplateServiceApplicationKt"

val excludePackages: List<String> by extra {
	listOf(
		"com/augenda/services/templateservice/application/**",
		"com/augenda/services/templateservice/commons/ext/**",
		"com/augenda/services/templateservice/TemplateServiceApplication*"
	)
}

@Suppress("UNCHECKED_CAST")
fun ignorePackagesForReport(jacocoBase: JacocoReportBase) {
	jacocoBase.classDirectories.setFrom(
		sourceSets.main.get().output.asFileTree.matching {
			exclude(jacocoBase.project.extra.get("excludePackages") as List<String>)
		}
	)
}

group = "com.augenda.services"
version = "1.0.0"
description = "Spring boot application template"

application {
	mainClass.set(mainPkgAndClass)
}

repositories {
	mavenCentral()
}

plugins {
	application
	kotlin("jvm") version "1.6.21"
	kotlin("plugin.spring") version "1.6.21"
	id("org.springframework.boot") version "2.7.1"
	id("io.spring.dependency-management") version "1.0.11.RELEASE"
	id("io.gitlab.arturbosch.detekt") version "1.19.0"
	id("org.openapi.generator") version "5.1.1"
	id("jacoco")
}

jacoco {
	toolVersion = "0.8.7"
	reportsDirectory.set(layout.buildDirectory.dir("jacoco"))
}

detekt {
	source = files("src/main/java", "src/main/kotlin")
	config = files("detekt/detekt.yml")
}

sourceSets {
	create("componentTest") {
		compileClasspath += sourceSets.main.get().output
		runtimeClasspath += sourceSets.main.get().output
	}
}

val componentTestImplementation: Configuration by configurations.getting {
	extendsFrom(configurations.implementation.get())
}

configurations["componentTestRuntimeOnly"].extendsFrom(configurations.runtimeOnly.get())

dependencies {
	//kotlin
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

	//spring
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-validation")

	//jackson
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

	testImplementation("com.ninja-squad:springmockk:3.1.1")
	testImplementation( "org.springframework.boot:spring-boot-starter-test") {
		exclude(module = "mockito-core")
	}

	componentTestImplementation("org.junit.jupiter:junit-jupiter")
	componentTestImplementation("io.rest-assured:kotlin-extensions:4.3.0")
	componentTestImplementation("de.flapdoodle.embed:de.flapdoodle.embed.mongo:3.0.0")
	componentTestImplementation(sourceSets["test"].output)
}

java.sourceSets["main"].java.srcDir("$buildDir/generated/src/main/kotlin")

openApiGenerate {
	generatorName.set("kotlin-spring")
	inputSpec.set("$rootDir/src/main/resources/api-docs.yml")
	outputDir.set("$buildDir/generated/")
	configFile.set("$rootDir/src/main/resources/api-config.json")
}

// tasks

tasks.withType<KotlinCompile> {
	sourceCompatibility = JavaVersion.VERSION_1_8.name
	targetCompatibility = JavaVersion.VERSION_1_8.name

	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "1.8"
	}
}

tasks.withType<CreateStartScripts> { mainClass.set(mainPkgAndClass) }

tasks.jar {
	duplicatesStrategy = DuplicatesStrategy.EXCLUDE
	manifest {
		attributes("Main-Class" to mainPkgAndClass)
		attributes("Package-Version" to archiveVersion)
	}

	from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
	from(sourceSets.main.get().output)
}

tasks.withType<Test> {
	useJUnitPlatform()
}

val componentTestTask = tasks.create("componentTest", Test::class) {
	description = "Runs the component tests."
	group = "verification"

	testClassesDirs = sourceSets["componentTest"].output.classesDirs
	classpath = sourceSets["componentTest"].runtimeClasspath

	useJUnitPlatform()
}

tasks.withType<JacocoReport> {
	reports {
		xml.required
		html.required
		html.outputLocation.set(layout.buildDirectory.dir("jacocoHtml"))
	}
	ignorePackagesForReport(this)
}

tasks.withType<JacocoCoverageVerification> {
	violationRules {
		rule {
			limit {
				minimum = "1.0".toBigDecimal()
				counter = "LINE"
			}
			limit {
				minimum = "1.0".toBigDecimal()
				counter = "BRANCH"
			}
		}
	}
	ignorePackagesForReport(this)
}

tasks.test {
	finalizedBy(tasks.jacocoTestReport, tasks.jacocoTestCoverageVerification, componentTestTask)
}

tasks.jacocoTestReport {
	dependsOn(tasks.test)
}