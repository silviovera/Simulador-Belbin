package MyPackage;
import view.modeling.ViewableDigraph;

import java.util.ArrayList;
import java.util.List;

public class ModeloAcopladoPersonas extends ViewableDigraph {
	
    private int numPersonas;
    private List<String> personalidadesDelGrupo;
    private List<Persona> personasEnGrupo;
    
    public ModeloAcopladoPersonas() {
        super("ModeloAcopladoPersonas");
        personasEnGrupo = new ArrayList<>();
        initializeModel();
    }

    private void initializeModel() {
        LeerArchivoJSON lectorJSON = new LeerArchivoJSON();
        String rutaArchivo = "C:\\Users\\silvi\\OneDrive\\Escritorio\\DEVS_Suite_3.0.0_mixed_win64\\Personas.JSON";
        lectorJSON.leerArchivoJSON(rutaArchivo);
        List<Archivo> archivos = lectorJSON.getArchivos();
        numPersonas = archivos.size();

        personalidadesDelGrupo = new ArrayList<>();
        Persona[] personas = new Persona[numPersonas];

        for (int i = 0; i < numPersonas; i++) {
            Archivo archivo = archivos.get(i);
            personas[i] = new Persona(
                archivo.getNombre(),
                archivo.getPersonalidad1(),
                archivo.getPersonalidad2(),
                archivo.getPorcentaje1(),
                archivo.getPorcentaje2(),
                personalidadesDelGrupo,
                personasEnGrupo
            );
            add(personas[i]);

                personalidadesDelGrupo.add(archivo.getPersonalidad1());
                personalidadesDelGrupo.add(archivo.getPersonalidad2());
                personasEnGrupo.add(personas[i]);

        }
        
        for (int i = 0; i < numPersonas; i++) {
            for (int k = 1; k <= numPersonas - 1; k++) {
                int targetIndex = (i + k) % numPersonas;
                addCoupling(personas[i], "Outport " + k, personas[targetIndex], "Inport " + k);
            }
        }
        initialize();
    }
}
