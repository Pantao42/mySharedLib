def call(body) {
    pipeline {
        agent any
        options {
            skipDefaultCheckout true
        }
        environment {
            mvnHome = tool 'M3'
            mvnSettingsFile = "mysettings.xml"
        }
        stages{
            stage("Checkout") {
                steps {
                    checkout scmGit(
                            branches: [[name: "${BRANCH_NAME}"]],
                            userRemoteConfigs: [[credentialsId:  'patao42atgithub',
                                                 url: 'https://github.com/Pantao42/JenkinsPipelineTest.git']])
                    //git credentialsId: 'patao42atgithub',
                      //      url: 'https://github.com/Pantao42/JenkinsPipelineTest.git'
                }
            }
            stage("Configure") {
                steps {
                    loadMavenSettings (name: "${mvnSettingsFile}")
                }
            }
            stage ("Build") {
                steps {
                    withMaven(
                            maven: 'M3',
                            globalMavenSettingsConfig: 'global-maven-config',
                            //mavenSettingsConfig: 'maven-settings',
                            mavenSettingsFilePath: "./${mvnSettings}",
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