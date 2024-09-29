def call(myPipeline) {
    pipeline {
        agent any
        options {
            skipDefaultCheckout true
        }
        environment {
            mvnHome = tool 'M3'
        }
        stages{
            stage("Checkout") {
                steps {
                    git branch: 'feature/mypipe_test1',
                            credentialsId: 'patao42atgithub',
                            url: 'https://github.com/Pantao42/JenkinsPipelineTest.git'
                }
            }
            stage ("Build") {
                steps {
                    withMaven(
                            maven: 'M3',
                            globalMavenSettingsConfig: 'maven-global-settings',
                            mavenSettingsConfig: '8453c57b-94fa-4a29-8cce-0e82e768a9b3',
                            mavenOpts: '-Dmaven.test.failure.ignore=true') {
                        sh "mvn clean package"
                    }
                }
            }
        }
        post {
            // If Maven was able to run the tests, even if some of the test
            // failed, record the test results and archive the jar file.
            success {
                junit '**/target/surefire-reports/TEST-*.xml'
                archiveArtifacts 'target/*.jar'
            }
        }
    }
}