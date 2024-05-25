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
if ($data !== null && isset($data['NombreUsuario']) && isset($data['Nombre']) && isset($data['Contrasena'])) {
	// Extrae los datos recibidos
	$NombreUsuario = $data['NombreUsuario'];
	$Nombre = $data['Nombre'];
	$Contrasena = $data['Contrasena'];

	// Inserta los datos en la base de datos
	$sql = "INSERT INTO Usuarios (NombreUsuario, Nombre, Contrasena) VALUES ('$NombreUsuario', '$Nombre', '$Contrasena')";
	if ($conn->query($sql) === TRUE) {
    	// Envía una respuesta JSON de éxito
    	$response = array("success" => true, "message" => "Usuario creado con éxito");
    	echo json_encode($response);
	} else {
    	// Si hay un error al insertar los datos, envía una respuesta JSON de error
    	$response = array("success" => false, "message" => "Error al crear usuario: " . $conn->error);
    	echo json_encode($response);
	}
} else {
	// Si los datos no son válidos, envía una respuesta JSON de error
	$response = array("success" => false, "message" => "Error al recibir datos");
	echo json_encode($response);
}

$conn->close();
?>
