<?php
// Conexión a la base de datos
$servername = "localhost";
$username = "root";
$password = "1234";
$dbname = "Android";

$conn = new mysqli($servername, $username, $password, $dbname);

// Verifica la conexión
if ($conn->connect_error) {
	die("Connection failed: " . $conn->connect_error);
}

// Recibe los datos JSON enviados desde la aplicación Android
$data = json_decode(file_get_contents("php://input"), true);

// Verifica si se recibieron datos válidos
if ($data !== null && isset($data['NombreUsuario']) && isset($data['Contrasena'])) {
	// Extrae los datos recibidos
	$NombreUsuario = $data['NombreUsuario'];
	$Contrasena = $data['Contrasena'];

	// Verifica los datos con la base de datos
	$sql = "SELECT * FROM Usuarios WHERE NombreUsuario = '$NombreUsuario' AND Contrasena = '$Contrasena'";
	$result = $conn->query($sql);
	if ($result->num_rows > 0) {
    	// Si las credenciales son correctas, envía una respuesta JSON de éxito
    	$response = array("success" => true, "message" => "Inicio de sesión exitoso");
    	echo json_encode($response);
	} else {
    	// Si las credenciales son incorrectas, envía una respuesta JSON de error
    	$response = array("success" => false, "message" => "Nombre de usuario o contraseña incorrectos");
    	echo json_encode($response);
	}
} else {
	// Si los datos no son válidos, envía una respuesta JSON de error
	$response = array("success" => false, "message" => "Error al recibir datos");
	echo json_encode($response);
}

$conn->close();
?>
