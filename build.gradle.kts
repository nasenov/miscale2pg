import org.springframework.boot.gradle.tasks.bundling.BootBuildImage

plugins {
  java
  id("org.springframework.boot") version "4.1.0"
  id("io.spring.dependency-management") version "1.1.7"
  id("org.graalvm.buildtools.native") version "1.1.4"
  id("com.diffplug.spotless") version "8.8.0"
}

group = "dev.nasenov"

version = "0.0.10" // x-release-please-version

java {
  toolchain {
    languageVersion = JavaLanguageVersion.of(25)
  }
}

repositories {
  mavenCentral()
}

dependencies {
  implementation("org.springframework.boot:spring-boot-starter-webmvc")
  implementation("org.springframework.boot:spring-boot-starter-actuator")
  implementation("org.springframework.boot:spring-boot-starter-jdbc")
  implementation("org.springframework.boot:spring-boot-starter-flyway")
  implementation("org.springframework.boot:spring-boot-starter-validation")
  implementation("org.flywaydb:flyway-database-postgresql")
  implementation("tools.jackson.dataformat:jackson-dataformat-csv")
  implementation("io.micrometer:micrometer-registry-prometheus")
  compileOnly("org.projectlombok:lombok")
  annotationProcessor("org.projectlombok:lombok")
  runtimeOnly("org.postgresql:postgresql")
  testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
  testImplementation("org.springframework.boot:spring-boot-testcontainers")
  testImplementation("org.testcontainers:testcontainers-postgresql")
  testImplementation("org.testcontainers:testcontainers-junit-jupiter")
  testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
  useJUnitPlatform()
}

tasks.named<BootBuildImage>("bootBuildImage") {
  imageName.set("ghcr.io/nasenov/${project.name}")
  tags.set(listOf("${imageName.get()}:${project.version}"))
  environment.put("BP_OCI_SOURCE", "https://github.com/nasenov/${project.name}")
}

configure<com.diffplug.gradle.spotless.SpotlessExtension> {
  java {
    target("src/*/java/**/*.java")
    importOrder()
    removeUnusedImports()
    forbidWildcardImports()
    forbidModuleImports()
    googleJavaFormat()
  }

  kotlinGradle {
    ktfmt().googleStyle()
  }

  json {
    target("**/*.json")
    prettier()
  }

  yaml {
    target("**/*.yaml")
    prettier()
  }
}
