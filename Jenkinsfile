pipeline {
    environment {
        PATH = "$PATH:/usr/local/bin"
    }
        agent any

        stages {

            stage('Gradle Build .jar') {steps {withGradle() {sh './gradlew assemble'}}

        }
    }
}