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
    die(json_encode(array('error' => 'Error de conexión: ' . $conn->connect_error)));
}

// Consulta SQL para obtener todas las historias
$sql = "SELECT HistoriaID, Titulo, Descripcion, Imagen FROM Historias";
$result = $conn->query($sql);

$historias = array();

if ($result->num_rows > 0) {
    while ($row = $result->fetch_assoc()) {
        // Codificar la imagen en Base64
        $imagenBase64 = base64_encode($row['Imagen']);

        // Agregar la historia al array
        $historias[] = array(
            'HistoriaID' => $row['HistoriaID'],
            'Titulo' => $row['Titulo'],
            'Descripcion' => $row['Descripcion'],
            'Imagen' => $imagenBase64
        );
    }
}

// Devolver el array de historias como JSON
echo json_encode($historias);

// Cerrar la conexión
$conn->close();
?>
