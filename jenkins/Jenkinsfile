pipeline {
    environment {
        PATH = "$PATH:/usr/local/bin"
    }
        agent any

        stages {

        stage('Gradle Build .jar') {steps {withGradle() {
                                                sh './gradlew assemble'}}}
//         stage('docker compose up') {steps {
//                                                 sh '/var/jenkins_home/docker-compose -f /var/jenkins_home/workspace/project_with_compose/src/main/docker/docker-compose.yml up '
//                                                 }}
//
//
//                }
}