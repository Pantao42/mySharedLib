def call(Map config = [:]){
    mySettings = libraryResource "de/my/company/${config.name}"
    writeFile file: "${config.name}", mySettings
    sh "chmod a+x ./${config.name}"
}
