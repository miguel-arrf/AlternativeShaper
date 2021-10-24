package marrf.iscte.JavaAndProlog;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class LoadPrologResult {

    private String string = null;

    //private static String[] permitido = {"scbat", "scdbe", "scdin", "scell", "scgar", "schal", "sckit", "scliv", "scoff", "scsbe", "scsta", "sctec", "sctoi", "scves"};
    private static ArrayList<String> permitido = new ArrayList<>();

    void obterStringGrande() throws IOException{

        File file = new File("/Users/miguelferreira/Downloads/AlternativeShaper_HTML_Files/AlternativeShaperProfessor/shapes");
        File[] listOfFiles = file.listFiles();

        permitido = new ArrayList<>();

        assert listOfFiles != null;
        for(File name:listOfFiles){
            String string = name.getName().replaceFirst("[.][^.]+$", "");
            string = "s" + string;

            permitido.add(string);
        }


        if (null == string) {

            string= Files.readString(new File("/Users/miguelferreira/Downloads/AlternativeShaper_HTML_Files/prologResult.txt").toPath());

        }
    }

    public static void main(String[] args) throws IOException {
        LoadPrologResult textos = new LoadPrologResult();

        textos.obterStringGrande();
        textos.tirarAspasEParentesisRetos();
        textos.obterValores();


    }

    public void tirarAspasEParentesisRetos(){

        string = string.replaceAll(Pattern.quote("'[|]"), "");
        string = string.replaceAll(Pattern.quote("'"), "");
        string = string.replaceAll(Pattern.quote("[]"), "");

        System.out.println(string);
    }

    public  ArrayList<Coordenadas> obterValores(){
        String[] particoes = string.split("\\s+");

        for(int i = 0; i < particoes.length; i++){
            particoes[i] = particoes[i].replaceAll(Pattern.quote(","), "");
            particoes[i] = particoes[i].replaceAll(Pattern.quote(")"), "");
            particoes[i] = particoes[i].replaceAll(Pattern.quote("("), "");

        }

        ArrayList<Coordenadas> conjuntoCoordenadas = new ArrayList<>();

        int j = 0;
        Coordenadas coordenadas = new Coordenadas();


        for(String s: particoes){

            if(permitido.contains(s)){
                coordenadas = new Coordenadas();
                coordenadas.setTipo(s);

                j = 0;
            }

            if(j == 7){
                coordenadas.setX(Float.parseFloat(s));
            }

            if(j==8){
                coordenadas.setY(Float.parseFloat(s));
                conjuntoCoordenadas.add(coordenadas);
            }

            j++;
        }


        return conjuntoCoordenadas;

    }

}
