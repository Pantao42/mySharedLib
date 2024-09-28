def call() {
    myMvn = tool "M3"
    pipeline {
        stages{
            stage("Checkout") {
                steps {
                    git url: 'https://github.com/Pantao42/JenkinsPipelineTest.git'
                }
            }
            stage ("Build") {
                steps {
                    withMaven {
                        sh "mvn clean install"
                    }
                }
            }
        }
    }
}