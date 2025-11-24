package client;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.math.BigDecimal;
import compute.Compute;


// Importaciones para el servidor web SimpleFileServer (Java 18+) ***
import java.net.InetSocketAddress;
import java.nio.file.Path;
import com.sun.net.httpserver.SimpleFileServer;
import com.sun.net.httpserver.SimpleFileServer.OutputLevel;

public class ComputePi {
    public static void main(String args[]) {

        String myIP = "localhost";    // <--- CAMBIA A TU IP REAL SI ES REMOTO
        String serverIP = "localhost";  // <--- CAMBIA A LA IP DEL SERVIDOR REAL

        startWebServer(myIP);
        System.setProperty("java.rmi.server.hostname", myIP);
        System.setProperty("java.rmi.server.codebase", "http://" + myIP + ":8000/client.jar");

        try {
            String name = "Compute";

            // Determina la cantidad de dígitos, usando 45 por defecto si no se proporciona.
            int digits = (args.length > 0) ? Integer.parseInt(args[0]) : 45;

            // Usa la IP del servidor para localizar el RMI Registry
            Registry registry = LocateRegistry.getRegistry(serverIP);

            Compute comp = (Compute) registry.lookup(name);

            System.out.println("Enviando tarea Pi (" + digits + " digitos) al servidor " + serverIP + "...");
            Pi task = new Pi(digits);

            BigDecimal pi = comp.executeTask(task);

            System.out.println("Resultado recibido: " + pi);
            System.out.println("Pi: " + pi);
            System.exit(0);
        } catch (Exception e) {
            System.err.println("ComputePi exception:");
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void startWebServer(String ip) {
        try {
            int port = 8000;
            Path path = Path.of(System.getProperty("user.dir"), "out/production/rmi-compute-engine");
            System.out.println("Iniciando servidor de archivos en: " + path.toAbsolutePath());

            var server = SimpleFileServer.createFileServer(
                    new InetSocketAddress(port),
                    path,
                    OutputLevel.INFO
            );
            server.start();
            System.out.println("Servidor Web (Cliente) activo en http://" + ip + ":" + port + "/");
        } catch (Exception e) {
            System.err.println("Advertencia Servidor Web: No se pudo iniciar el servidor web (¿Puerto 8000 ocupado?): " + e.getMessage());
        }
    }
}