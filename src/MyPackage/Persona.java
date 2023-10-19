package MyPackage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import model.modeling.message;
import view.modeling.ViewableAtomic;
import GenCol.*;



public class Persona extends ViewableAtomic {
	private static double tiempoTranscurrido = 0.0	;
    private boolean escuchando;
    private boolean hablando;
    private String Nombre;
    private String Personalidad1;
    private String Personalidad2;
    private double Porcentaje1;
    private double Porcentaje2;
    private int numPersonas; 
    private List<String> personalidadesDelGrupo = new ArrayList<>();
    boolean mensajeGenerado = false;
    CSVWriter csvWriter = new CSVWriter("C:\\Users\\silvi\\OneDrive\\Escritorio\\DEVS_Suite_3.0.0_mixed_win64\\resultados.csv");
    
    public Persona(String Nombre, String Personalidad1, String Personalidad2, double Porcentaje1, double Porcentaje2, List<String> personalidadesDelGrupo,List<Persona> personasEnGrupo) {
        super(Nombre);


        this.Nombre = Nombre;
        this.Personalidad1 = Personalidad1;
        this.Personalidad2 = Personalidad2;
        this.Porcentaje1 = Porcentaje1;
        this.Porcentaje2 = Porcentaje2;
        this.personalidadesDelGrupo = personalidadesDelGrupo;


        
        LeerArchivoJSON lectorJSON = new LeerArchivoJSON();
        String rutaArchivo = "C:\\Users\\silvi\\OneDrive\\Escritorio\\DEVS_Suite_3.0.0_mixed_win64\\Personas.JSON";
        lectorJSON.leerArchivoJSON(rutaArchivo);
        List<Archivo> archivos = lectorJSON.getArchivos();
        numPersonas = archivos.size();


        for (int i = 1; i <= numPersonas - 1; i++) {
            addInport("Inport " + i);
        }

        for (int i = 1; i <= numPersonas - 1; i++) {
            addOutport("Outport " + i);
        }
        
        

    }
    private double tiempoHabladoAcumulado = 0.0;
    @Override
    public void initialize() {
    	
        escuchando = true;
        hablando = false;

        if (puedeIniciarConversacion()) {
            hablando = true;
            escuchando = false;
        }

        if (escuchando) {
            passivate(); 
        } else if (hablando) {
            double tiempoDeHabla = calcularTiempoDeHabla(personalidadesDelGrupo);
            holdIn("Hablando", tiempoDeHabla); 
        }
        super.initialize();
    }


    
    @Override
    public void deltint() {
    	
        System.out.println(this.Nombre + " deltint()");
        double tiempoDeHabla = calcularTiempoDeHabla(personalidadesDelGrupo);

        System.out.print("persona hablando: "+this.Nombre+ ", Tiempo hablando: "+ tiempoDeHabla);
        String registroCSV = this.Nombre + "," + tiempoDeHabla +"\n";
        csvWriter.writeToCSV(registroCSV);
            if (hablando) {
                tiempoHabladoAcumulado += tiempoDeHabla;
                if (tiempoHabladoAcumulado >= sigma) {
                    hablando = false;
                    escuchando = true;
                    mensajeGenerado = false;
                    passivate();
                }
            } else {
                escuchando = true;
            }
        
        

    }

    @Override
    public message out() {
        message m = new message();

        if (hablando && !mensajeGenerado) {
            int numPuertosDestino = numPersonas - 1; 
            int numeroAleatorio = generarNumeroAleatorio(numPuertosDestino);
            double tiempoDeHabla = calcularTiempoDeHabla(personalidadesDelGrupo);
            tiempoTranscurrido=tiempoTranscurrido+tiempoDeHabla;
            String puertoSalida = "Outport " + (numeroAleatorio + 1); 
            m.add(makeContent(puertoSalida, new entity("Toma tu turno de hablar")));
            mensajeGenerado = true;
        
        }
        
        return m;
    }

    @Override
    public void deltext(double e, message x) {
        if(tiempoTranscurrido>500) {
        	System.out.println("----------------------------- ");
        	passivate();
        	tiempoTranscurrido=0.0;
        }
        else {
    	System.out.println(this.Nombre+" deltext()");
        Continue(e);
        if (x.getLength() > 0) {
        	double tiempoDeHabla = calcularTiempoDeHabla(personalidadesDelGrupo);
            hablando = true;
            escuchando = false;
            holdIn("Hablando", tiempoDeHabla);
        }
        }
    }
    
	public boolean puedeIniciarConversacion() {
        double mayorPorcentaje = 0.0;
        LeerArchivoJSON lectorJSON = new LeerArchivoJSON();
        String rutaArchivo = "C:\\Users\\silvi\\OneDrive\\Escritorio\\DEVS_Suite_3.0.0_mixed_win64\\Personas.JSON";
        lectorJSON.leerArchivoJSON(rutaArchivo);
        List<Archivo> archivos = lectorJSON.getArchivos();

        for (Archivo archivo : archivos) {
            String personalidad1 = archivo.getPersonalidad1();
            String personalidad2 = archivo.getPersonalidad2();

            if (("Cohesionador".equals(personalidad1) || "Cohesionador".equals(personalidad2) ||
                 "Coordinador".equals(personalidad1) || "Coordinador".equals(personalidad2) ||
                 "Impulsor".equals(personalidad1) || "Impulsor".equals(personalidad2))) {
                
                double porcentaje = archivo.getPorcentaje1();
                double porcentaje2 = archivo.getPorcentaje2();

                if (porcentaje > mayorPorcentaje) {
                    mayorPorcentaje = porcentaje;
                }

                if (porcentaje2 > mayorPorcentaje) {
                    mayorPorcentaje = porcentaje2;
                }
            }
        }

        return (Personalidad1.equals("Cohesionador") || Personalidad2.equals("Cohesionador") ||
                Personalidad1.equals("Coordinador") || Personalidad2.equals("Coordinador") ||
                Personalidad1.equals("Impulsor") || Personalidad2.equals("Impulsor")) &&
                (Porcentaje1 >= mayorPorcentaje || Porcentaje2 >= mayorPorcentaje);
    }
	public double calcularTiempoDeHabla(List<String> personalidadesDelGrupo) {

	    Map<String, Double> tiemposDeHablaAdicionales = new HashMap<>();

	    //combinaciones de personalidades
	    tiemposDeHablaAdicionales.put("Investigador de Recursos-Investigador de Recursos", 2.0);
	    tiemposDeHablaAdicionales.put("Investigador de Recursos-Cohesionador", 1.0);
	    tiemposDeHablaAdicionales.put("Investigador de Recursos-Coordinador", 3.0);
	    tiemposDeHablaAdicionales.put("Investigador de Recursos-Cerebro", 3.0);
	    tiemposDeHablaAdicionales.put("Investigador de Recursos-Monitor Evaluador", 3.0);
	    tiemposDeHablaAdicionales.put("Investigador de Recursos-Especialista", 3.0);
	    tiemposDeHablaAdicionales.put("Investigador de Recursos-Impulsor", 3.0);
	    tiemposDeHablaAdicionales.put("Investigador de Recursos-Implementador", 3.0);
	    tiemposDeHablaAdicionales.put("Investigador de Recursos-Finalizador ", 3.0);
    	tiemposDeHablaAdicionales.put("Cohesionador-Investigador de Recursos", 2.0);
	    tiemposDeHablaAdicionales.put("Cohesionador-Cohesionador", 1.0);
	    tiemposDeHablaAdicionales.put("Cohesionador-Coordinador", 3.0);
	    tiemposDeHablaAdicionales.put("Cohesionador-Cerebro", 3.0);
	    tiemposDeHablaAdicionales.put("Cohesionador-Monitor Evaluador", 3.0);
	    tiemposDeHablaAdicionales.put("Cohesionador-Especialista", 3.0);
	    tiemposDeHablaAdicionales.put("Cohesionador-Impulsor", 3.0);
	    tiemposDeHablaAdicionales.put("Cohesionador-Implementador", 3.0);
	    tiemposDeHablaAdicionales.put("Cohesionador-Finalizador ", 3.0);
	    tiemposDeHablaAdicionales.put("Coordinador-Investigador de Recursos", 2.0);
	    tiemposDeHablaAdicionales.put("Coordinador-Cohesionador", 1.0);
	    tiemposDeHablaAdicionales.put("Coordinador-Coordinador", 3.0);
	    tiemposDeHablaAdicionales.put("Coordinador-Cerebro", 3.0);
	    tiemposDeHablaAdicionales.put("Coordinador-Monitor Evaluador", 3.0);
	    tiemposDeHablaAdicionales.put("Coordinador-Especialista", 3.0);
	    tiemposDeHablaAdicionales.put("Coordinador-Impulsor", 3.0);
	    tiemposDeHablaAdicionales.put("Coordinador-Implementador", 3.0);
	    tiemposDeHablaAdicionales.put("Coordinador-Finalizador ", 3.0);
	    tiemposDeHablaAdicionales.put("Cerebro-Investigador de Recursos", 2.0);
	    tiemposDeHablaAdicionales.put("Cerebro-Cohesionador", 2.0);
	    tiemposDeHablaAdicionales.put("Cerebro-Coordinador", 4.0);
	    tiemposDeHablaAdicionales.put("Cerebro-Cerebro", 3.0);
	    tiemposDeHablaAdicionales.put("Cerebro-Monitor Evaluador", 3.0);
	    tiemposDeHablaAdicionales.put("Cerebro-Especialista", 3.0);
	    tiemposDeHablaAdicionales.put("Cerebro-Impulsor", 3.0);
	    tiemposDeHablaAdicionales.put("Cerebro-Implementador", 3.0);
	    tiemposDeHablaAdicionales.put("Cerebro-Finalizador ", 3.0);
	    tiemposDeHablaAdicionales.put("Monitor Evaluador-Investigador de Recursos", 5.0);
	    tiemposDeHablaAdicionales.put("Monitor Evaluador-Cohesionador", 1.0);
	    tiemposDeHablaAdicionales.put("Monitor Evaluador-Coordinador", 3.0);
	    tiemposDeHablaAdicionales.put("Monitor Evaluador-Cerebro", 3.0);
	    tiemposDeHablaAdicionales.put("Monitor Evaluador-Monitor Evaluador", 3.0);
	    tiemposDeHablaAdicionales.put("Monitor Evaluador-Especialista", 3.0);
	    tiemposDeHablaAdicionales.put("Monitor Evaluador-Impulsor", 3.0);
	    tiemposDeHablaAdicionales.put("Monitor Evaluador-Implementador", 3.0);
	    tiemposDeHablaAdicionales.put("Monitor Evaluador-Finalizador ", 3.0);
	   	tiemposDeHablaAdicionales.put("Especialista-Investigador de Recursos", 2.0);
	    tiemposDeHablaAdicionales.put("Especialista-Cohesionador", 1.0);
	    tiemposDeHablaAdicionales.put("Especialista-Coordinador", 3.0);
	    tiemposDeHablaAdicionales.put("Especialista-Cerebro", 3.0);
	    tiemposDeHablaAdicionales.put("Especialista-Monitor Evaluador", 3.0);
	    tiemposDeHablaAdicionales.put("Especialista-Especialista", 3.0);
	    tiemposDeHablaAdicionales.put("Especialista-Impulsor", 3.0);
	    tiemposDeHablaAdicionales.put("Especialista-Implementador", 3.0);
	    tiemposDeHablaAdicionales.put("Especialista-Finalizador ", 3.0);
	    tiemposDeHablaAdicionales.put("Impulsor-Investigador de Recursos", 2.0);
	    tiemposDeHablaAdicionales.put("Impulsor-Cohesionador", 1.0);
	    tiemposDeHablaAdicionales.put("Impulsor-Coordinador", 3.0);
	    tiemposDeHablaAdicionales.put("Impulsor-Cerebro", 3.0);
	    tiemposDeHablaAdicionales.put("Impulsor-Monitor Evaluador", 3.0);
	    tiemposDeHablaAdicionales.put("Impulsor-Especialista", 3.0);
	    tiemposDeHablaAdicionales.put("Impulsor-Impulsor", 3.0);
	    tiemposDeHablaAdicionales.put("Impulsor-Implementador", 3.0);
	    tiemposDeHablaAdicionales.put("Impulsor-Finalizador ", 3.0);
	    tiemposDeHablaAdicionales.put("Implementador-Investigador de Recursos", 2.0);
	    tiemposDeHablaAdicionales.put("Implementador-Cohesionador", 1.0);
	    tiemposDeHablaAdicionales.put("Implementador-Coordinador", 3.0);
	    tiemposDeHablaAdicionales.put("Implementador-Cerebro", 3.0);
	    tiemposDeHablaAdicionales.put("Implementador-Monitor Evaluador", 3.0);
	    tiemposDeHablaAdicionales.put("Implementador-Especialista", 3.0);
	    tiemposDeHablaAdicionales.put("Implementador-Impulsor", 3.0);
	    tiemposDeHablaAdicionales.put("Implementador-Implementador", 3.0);
	    tiemposDeHablaAdicionales.put("Implementador-Finalizador ", 3.0);
	    tiemposDeHablaAdicionales.put("Finalizador-Investigador de Recursos", 2.0);
	    tiemposDeHablaAdicionales.put("Finalizador-Cohesionador", 1.0);
	    tiemposDeHablaAdicionales.put("Finalizador-Coordinador", 3.0);
	    tiemposDeHablaAdicionales.put("Finalizador-Cerebro", 3.0);
	    tiemposDeHablaAdicionales.put("Finalizador-Monitor Evaluador", 3.0);
	    tiemposDeHablaAdicionales.put("Finalizador-Especialista", 3.0);
	    tiemposDeHablaAdicionales.put("Finalizador-Impulsor", 3.0);
	    tiemposDeHablaAdicionales.put("Finalizador-Implementador", 3.0);
	    tiemposDeHablaAdicionales.put("Finalizador-Finalizador ", 3.0);

	    double tiempoDeHabla = 0.0;
	    double promedioPorcentajes = (Porcentaje1 + Porcentaje2) / 2.0;
	    

	    for (String personalidadGrupo : personalidadesDelGrupo) {
	        String combinacion1 = Personalidad1 + "-" + personalidadGrupo;
	        String combinacion2 = Personalidad2 + "-" + personalidadGrupo;

	        if (tiemposDeHablaAdicionales.containsKey(combinacion1)) {
	            tiempoDeHabla += tiemposDeHablaAdicionales.get(combinacion1);
	        }
	        if (tiemposDeHablaAdicionales.containsKey(combinacion2)) {
	            tiempoDeHabla += tiemposDeHablaAdicionales.get(combinacion2);
	        }
	    }
	    tiempoDeHabla=tiempoDeHabla*promedioPorcentajes;
	    return tiempoDeHabla;
	}
	private int generarNumeroAleatorio(int maximo) {
	    Random random = new Random();
	    return random.nextInt(maximo);
	}

}