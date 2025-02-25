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
        stages {
            stage('Setup parameters') {
                steps {
                    script {
                        properties([
                                parameters([
                                        choice(
                                                choices: ['ONE', 'TWO'],
                                                name: 'PARAMETER_01'
                                        ),
                                        booleanParam(
                                                defaultValue: true,
                                                description: 'Stage Condition',
                                                name: 'myCondition'
                                        ),
                                        text(
                                                defaultValue: '''
                                this is a multi-line 
                                string parameter example
                                ''',
                                                name: 'MULTI-LINE-STRING'
                                        ),
                                        string(
                                                defaultValue: 'scriptcrunch',
                                                name: 'STRING-PARAMETER',
                                                trim: true
                                        )
                                ])
                        ])
                    }
                }
            }
            stage("Checkout") {
                steps {
                    checkout scmGit(
                            branches: [[name: "${BRANCH_NAME}"]],
                            userRemoteConfigs: [[credentialsId: 'patao42atgithub',
                                                 url          : 'https://github.com/Pantao42/JenkinsPipelineTest.git']])
                }
            }
            Stage("MyConditional") {
                when {
                    expression { return params.myCondition }
                }
                steps {
                    sh '''#!/bin/bash
                    echo "Conditional Step" 
                    '''
                }
            }
            stage("Configure") {
                steps {
                    loadMavenSettings(fileName: "${mvnSettingsFile}")
//                    // OpenJDK 21 konfigurieren
//                    tool name: 'OPENJDK21', type: 'jdk'
//                    env.JAVA_HOME = tool 'OPENJDK21'
//                    env.PATH = "${env.JAVA_HOME}/bin:${env.PATH}"
                }
            }
            stage("Build") {
                steps {
                    withMaven(
                            maven: 'M3',
                            globalMavenSettingsConfig: 'global-maven-config',
                            //mavenSettingsConfig: 'maven-settings',
                            mavenSettingsFilePath: "./${mvnSettingsFile}",
                            mavenOpts: '-Dmaven.test.failure.ignore=true') {
//                        sh "mvn clean package pmd:pmd"
                        sh "mvn clean package"
                    }
                }
            }
//            stage('PMD Analysis') {
//                steps {
//                    recordIssues(tools: [pmdParser(pattern: '**/target/pmd.xml')])
//                }
//            }
        }
        post {
            // If Maven was able to run the tests, even if some of the test
            // failed, record the test results and archive the jar file.
            success {
                junit '**/target/surefire-reports/TEST-*.xml'
                archiveArtifacts 'target/*.jar'
                recordIssues(tools: [pmdParser(pattern: '**/target/pmd.xml')]
            }
        }
    }
}