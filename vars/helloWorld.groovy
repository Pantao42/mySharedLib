def call(Map config = [:]) {
	sh "echo Mein name lautet ${config.name} und heute ist der ${config.datum}"
}
