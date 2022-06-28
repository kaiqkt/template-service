import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val mainPkgAndClass = "com.augenda.services.templateservice.TemplateServiceApplication"

val excludePackages: List<String> by extra {
	listOf(
		"com/augenda/services/templateserviceapplication/dto/**",
		"com/augenda/services/templateservicecommons/ext/**",
		"com/augenda/services/templateservice/TemplateServiceApplication*"
	)
}

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
	id("jacoco")
}

dependencies {
	//kotlin
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

	//spring
	implementation("org.springframework.boot:spring-boot-starter-web")

	//jackson
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
}

application {
	mainClassName = mainPkgAndClass
}

tasks.withType<KotlinCompile> {
	sourceCompatibility = JavaVersion.VERSION_1_8.name
	targetCompatibility = JavaVersion.VERSION_1_8.name

	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "1.8"
	}
}

tasks.withType<CreateStartScripts> { mainClassName = mainPkgAndClass }

tasks.jar {
	duplicatesStrategy = DuplicatesStrategy.EXCLUDE
	manifest {
		attributes("Main-Class" to mainPkgAndClass)
		attributes("Package-Version" to archiveVersion)
	}

	from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
	from(sourceSets.main.get().output)
}

detekt {
	source = files("src/main/java", "src/main/kotlin")
	config = files("detekt/detekt.yml")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

jacoco {
	toolVersion = "0.8.7"
	reportsDirectory.set(layout.buildDirectory.dir("jacoco"))
}

tasks.withType<JacocoReport> {
	reports {
		xml.isEnabled = true
		html.isEnabled = true
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
	finalizedBy(tasks.jacocoTestReport, tasks.jacocoTestCoverageVerification)
}

tasks.jacocoTestReport {
	dependsOn(tasks.test)
}
