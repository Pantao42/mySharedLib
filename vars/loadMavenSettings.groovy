def call(Map config = [:]) {
    mySettingsFile = libraryResource "de/my/company/${config.name}"
    writeFile file: "${config.fileName}", mySettingsFile
    //sh "chmod a+x ./${config.name}"
}
