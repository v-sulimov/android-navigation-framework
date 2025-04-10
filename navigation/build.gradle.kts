plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.maven.publish)
}

android {
    namespace = "com.vsulimov.navigation"
    compileSdk = 35

    defaultConfig {
        minSdk = 24
        lint.targetSdk = 35
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

dependencies {

}

publishing {
    publications {
        afterEvaluate {
            create<MavenPublication>("release") {
                groupId = "com.vsulimov"
                artifactId = "navigation"
                version = "1.0.0"

                from(components["release"])

                pom {
                    packaging = "aar"
                    name = "Android Navigation Framework"
                    description = "A lightweight and flexible navigation framework for Android, designed to manage screen navigation using a stack-based approach with Fragments."
                    licenses {
                        license {
                            name = "The MIT License (MIT)"
                            url = "https://mit-license.org/"
                        }
                    }
                    developers {
                        developer {
                            name = "Vitaly Sulimov"
                            email = "v.sulimov.dev@imap.cc"
                        }
                    }
                    scm {
                        url = "https://git.vsulimov.com/android-navigation-framework.git"
                    }
                }
            }
        }
    }
}
