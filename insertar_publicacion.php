<?php
header('Content-Type: application/json');

$servername = "localhost"; // Reemplaza con la dirección de tu servidor MySQL
$username = "root";       // Reemplaza con tu nombre de usuario de MySQL
$password = "1234";       // Reemplaza con tu contraseña de MySQL
$dbname = "Android";      // Nombre de tu base de datos

// Crear conexión
$conn = new mysqli($servername, $username, $password, $dbname);

// Verificar conexión
if ($conn->connect_error) {
    die(json_encode(array('status' => 'Error', 'message' => 'Error de conexión: ' . $conn->connect_error)));
}

// Obtener datos del JSON enviado desde la aplicación
$data = json_decode(file_get_contents('php://input'), true);

// Validación de datos (opcional, pero recomendado)
if (empty($data['titulo']) || empty($data['descripcion']) || empty($data['imagen'])) {
    die(json_encode(array('status' => 'Error', 'message' => 'Faltan datos requeridos')));
}

$titulo = $data['titulo'];
$descripcion = $data['descripcion'];
$imagenBase64 = $data['imagen'];

// Decodificar la imagen Base64
$imagenBinaria = base64_decode($imagenBase64);

// Preparar la consulta SQL (usando consultas preparadas para mayor seguridad)
$stmt = $conn->prepare("INSERT INTO Historias (UsuarioID, Titulo, Descripcion, Imagen) VALUES (?, ?, ?, ?)");

// Validar si la preparación de la consulta fue exitosa
if (!$stmt) {
    error_log("Error en la preparación de la consulta: " . $conn->error);
    die(json_encode(array('status' => 'Error', 'message' => 'Error en la preparación de la consulta')));
}

// Obtener el ID del usuario (¡importante! debes obtenerlo de la aplicación)
$usuarioID = 1; // Reemplaza con el ID del usuario real

// Vincular parámetros a la consulta preparada
$stmt->bind_param("isss", $usuarioID, $titulo, $descripcion, $imagenBinaria);

// Ejecutar la consulta
if ($stmt->execute()) {
    echo json_encode(array('status' => 'Success', 'message' => 'Publicación exitosa'));
} else {
    echo json_encode(array('status' => 'Error', 'message' => 'Error al publicar: ' . $stmt->error));
}

// Cerrar la consulta y la conexión
$stmt->close();
$conn->close();
?>
