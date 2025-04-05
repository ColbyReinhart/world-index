plugins {
	kotlin("jvm") version "2.1.0"
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
	compileOnly("org.xerial:sqlite-jdbc:3.49.1.0")
}
