package me.jaime.astromanager;

import me.jaime.astromanager.enums.TypeBody;
import me.jaime.astromanager.objects.*;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.ArrayList;

public class AstroLoader {

    public static ArrayList<CelestialBody> loadObjects(String file) {
        ArrayList<CelestialBody> list = new ArrayList<>();
        String line = "";

        // --- CAMBIO CLAVE ---
        // 1. Usamos getResourceAsStream para leer archivos empaquetados.
        // 2. Ponemos "/" delante para buscar en la raíz de 'resources'.
        try (InputStream is = AstroLoader.class.getResourceAsStream("/" + file)) {

            // Comprobación de seguridad: ¿Existe el archivo?
            if (is == null) {
                System.err.println("❌ ERROR CRÍTICO: No se encuentra '" + file + "' dentro del JAR.");
                System.err.println("👉 Asegúrate de que 'objects.csv' está dentro de la carpeta 'resources'.");
                return list;
            }

            // 3. Leemos el flujo de datos (Stream) en lugar del archivo físico
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
                while((line = br.readLine()) != null) {
                    // Evitar líneas vacías si las hubiera
                    if (line.trim().isEmpty()) continue;

                    String[] data = line.split(",");

                    // --- TU LÓGICA DE PARSEO ORIGINAL ---
                    String name = data[0];
                    String id = data[1];
                    double magnitude = Double.parseDouble(data[2].trim());
                    double ra = AstroCalculator.parseRA(data[3].trim());
                    double dec = AstroCalculator.parseDEC(data[4].trim());
                    TypeBody typeBody = TypeBody.valueOf(data[5].trim());

                    // Simplificación: No hace falta crear un CelestialBody genérico primero,
                    // podemos comprobar el typeBody directamente.
                    if(typeBody == TypeBody.GALAXY) {
                        list.add(new Galaxy(name, id, ra, dec, magnitude));
                    } else if(typeBody == TypeBody.NEBULA) {
                        list.add(new Nebula(name, id, ra, dec, magnitude));
                    } else if(typeBody == TypeBody.CLUSTER) {
                        list.add(new Cluster(name, id, ra, dec, magnitude));
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("❌ ERROR: File cannot be read inside JAR.");
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("❌ ERROR: Wrong line at CSV.");
            System.err.println("Conflictive line: " + line);
            e.printStackTrace();
        }

        return list;
    }
}