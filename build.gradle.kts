plugins {
	kotlin("jvm") version "2.1.0"
	id("com.github.johnrengelman.shadow") version "8.0.0"
}

repositories {
	mavenCentral()
	maven {
		name = "papermc"
		url = uri("https://repo.papermc.io/repository/maven-public/")
	}
}

dependencies {
	compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
}

java {
	toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}
