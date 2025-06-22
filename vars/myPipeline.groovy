import org.codehaus.groovy.runtime.dgmimpl.arrays.IntegerArrayGetAtMetaMethod

def call(body) {
    // Konfiguration aus dem body Parameter extrahieren
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()
    
    pipeline {
        agent any
//        def buildNum = BUILD_ID as Integer
//        def num = countBuildRemain as Integer
//        def result = (buildNum) - (num)
//        TZ=Europe/Berlin
        triggers {
            pollSCM 'H/15 * * * *'
        }
        options {
            skipDefaultCheckout true
            disableConcurrentBuilds()
            timeout(time: 120, unit: 'MINUTES')
//            buildDiscarder(logRotator(numToKeepStr: '3'))
            buildDiscarder BuildHistoryManager([[actions: [DeleteBuild()],
                                                 conditions: [BuildResult(matchAborted: true)]]])
            preserveStashes(buildCount: 1)
        }
        environment {
            mvnHome = tool 'M3'
            mvnSettingsFile = "mysettings.xml"
            myCondition = "false"
            changeSetPath = "${config.module_path ?: ''}"
        }
        stages {
            stage("Checkout") {
                steps {
                    checkout scmGit(
                            branches: [[name: "${BRANCH_NAME}"]],
                            userRemoteConfigs: [[credentialsId: 'patao42atgithub',
                                                 url          : 'https://github.com/Pantao42/multimodule.git']])
                }
            }
            stage("checkChangeset") {
                when {
                    allOf {
                        not {
                            changeset "${changeSetPath}**/*"
                        }
                        not {
                            triggeredBy 'BuildUpstreamCause'
                        }
                        not {
                            triggeredBy 'TimerTrigger'
                        }
                        not {
                            triggeredBy 'UserIDCause'
                        }
                    }
                }
                steps {
                    script {
                        sh """
                        echo "Aktuelles Verzeichnis:"
                        pwd
                        echo "Listing:"
                        ls -al
                        echo "Changeset-Pfad Überprüfung: ${changeSetPath}"
                        """
                        currentBuild.result = 'NOT_BUILT'
                        error "Build aborted, because changeSet does not include files configured via changeSetPath"
                    }
                }
            }
            stage("Configure") {
                steps {
                    loadMavenSettings(fileName: "${mvnSettingsFile}")
//                    // OpenJDK 21 konfigurieren
//                    tool name: 'OPENJDK21', type: 'jdk'
//                    env.JAVA_HOME = tool 'OPENJDK21'
//                    env.PATH = "${env.JAVA_HOME}/bin:${env.PATH}"

                    withMaven(
                            maven: 'M3',
                            globalMavenSettingsConfig: 'global-maven-config',
                            mavenSettingsFilePath: "../${mvnSettingsFile}",
                            mavenOpts: '-Dmaven.test.failure.ignore=true') {
                        sh '''
                            cd ${changeSetPath}
                            mvn clean verify
                            
                           '''
                    }
                }
            }
            stage("Build") {
                steps {
                    withMaven(
                            maven: 'M3',
                            globalMavenSettingsConfig: 'global-maven-config',
                            //mavenSettingsConfig: 'maven-settings',
                            mavenSettingsFilePath: "../${mvnSettingsFile}",
                            mavenOpts: '-Dmaven.test.failure.ignore=true') {
//                        sh "mvn clean package pmd:pmd"
                        sh '''
                            mvn package
                            pwd
                        '''
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
                recordIssues(tools: [pmdParser(pattern: '**/target/pmd.xml')])
            }
        }
    }
}