def call(Map config = [:]){
    mySettingsFile = libraryResource "de/my/company/${config.name}"
    writeFile file: "${config.name}", mySettingsFile
    //sh "chmod a+x ./${config.name}"
}
