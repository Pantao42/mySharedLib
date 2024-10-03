def call(Map config = [:]) {
    mySettingsFile = libraryResource "de/my/company/${config.fileName}"
    writeFile file: "${config.fileName}", mySettingsFile
    //sh "chmod a+x ./${config.name}"
}
