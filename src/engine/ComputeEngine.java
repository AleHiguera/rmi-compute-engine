package engine;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import compute.Compute;
import compute.Task;

// *** Importaciones para el servidor web SimpleFileServer (Java 18+) ***
import java.net.InetSocketAddress;
import java.nio.file.Path;
import com.sun.net.httpserver.SimpleFileServer;
import com.sun.net.httpserver.SimpleFileServer.OutputLevel;


public class ComputeEngine implements Compute {

    public ComputeEngine() {
        super();
    }

    public <T> T executeTask(Task<T> t) {
        System.out.println("Peticion recibida ");
        T result = t.execute();
        System.out.println("Fin");
        return result;
    }

    public static void main(String[] args) {
        String myIP = "192.168.1.137"; // <--- CAMBIA A TU IP REAL SI ES REMOTO
        startWebServer(myIP);

        System.setProperty("java.rmi.server.hostname", myIP);
        System.setProperty("java.rmi.server.codebase", "http://" + myIP + ":8000/compute.jar");

        try {
            try {
                LocateRegistry.createRegistry(Registry.REGISTRY_PORT); // Puerto 1099
                System.out.println("Registro RMI iniciado en puerto " + Registry.REGISTRY_PORT + ".");
            } catch (Exception e) {
                System.out.println("El registro RMI ya estaba corriendo.");
            }

            String name = "Compute";
            Compute engine = new ComputeEngine();
            Compute stub =
                    (Compute) UnicastRemoteObject.exportObject(engine, 0);

            Registry registry = LocateRegistry.getRegistry();
            registry.rebind(name, stub);
            System.out.println("ComputeEngine (Servidor) está listo y esperando...");
        } catch (Exception e) {
            System.err.println("ComputeEngine exception:");
            e.printStackTrace();
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
            System.out.println("Servidor Web (Servidor) activo en http://" + ip + ":" + port + "/");
        } catch (Exception e) {
            System.err.println("Advertencia Servidor Web: No se pudo iniciar el servidor web (¿Puerto 8000 ocupado?): " + e.getMessage());
        }
    }
}