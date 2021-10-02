package marrf.iscte;

import java.util.ArrayList;
import java.util.UUID;

public class teste {

    public static void main(String[] args) {

        String idToPut = "c1df1a90-daf0-4929-be7e-a339e90a0e3c";

        teste teste = new teste();
        StringBuilder finalString = new StringBuilder();

        finalString.append(teste.get("basement14x17", 14,17,"e0890c7a-feda-4619-afa5-646955c8a415"));
        finalString.append(teste.get("basement7x9", 7,9,"e0890c7a-feda-4619-afa5-646955c8a415"));
        finalString.append(teste.get("basement7x8", 7,8,"e0890c7a-feda-4619-afa5-646955c8a415"));
        finalString.append(teste.get("basement7x7", 7,7,"e0890c7a-feda-4619-afa5-646955c8a415"));
        finalString.append(teste.get("basement7x6", 7,6,"e0890c7a-feda-4619-afa5-646955c8a415"));
        finalString.append(teste.get("basement7x5", 7,5,"e0890c7a-feda-4619-afa5-646955c8a415"));
        finalString.append(teste.get("basement7x4", 7,4,"e0890c7a-feda-4619-afa5-646955c8a415"));
        finalString.append(teste.get("basement7x3", 7,3,"e0890c7a-feda-4619-afa5-646955c8a415"));
        finalString.append(teste.get("basement7x2", 7,2,"e0890c7a-feda-4619-afa5-646955c8a415"));
        finalString.append(teste.get("basement3x4", 3,4,"e0890c7a-feda-4619-afa5-646955c8a415"));
        finalString.append(teste.get("basement3x3", 3,3,"e0890c7a-feda-4619-afa5-646955c8a415"));

        finalString.append(teste.get("garage6x9", 6,9,"7286c5ad-f05e-429e-8c9d-0031c7bf9451"));

        finalString.append(teste.get("vestibule3x3", 3,3,"991deab9-8099-4c7d-a339-d8e174c52aff"));

        finalString.append(teste.get("toilet2x3", 2,3,"869d203e-60aa-47cc-8437-e8370730cf17"));

        finalString.append(teste.get("techRoom3x3", 3,3,"4c1dec89-c7af-43a8-ad39-ad8847c28f38"));

        finalString.append(teste.get("kitchen7x2", 7,2,"5f26ba46-a68d-43fe-bc2b-0a83b3bd534e"));

        finalString.append(teste.get("livingRoom7x5", 7,5,"618d1ab3-33ab-4720-88fa-e4f7488c24ca"));

        finalString.append(teste.get("diningRoom7x4", 7,4,"a9913b57-ec20-41bf-b34e-07dbedec24a9"));

        finalString.append(teste.get("hall2x3", 2,3,"04df8e8d-85a7-42fb-a120-d109f2cbdd71"));
        finalString.append(teste.get("hall2x4", 2,4,"04df8e8d-85a7-42fb-a120-d109f2cbdd71"));

        finalString.append(teste.get("singleBedRoom5x4", 5,4,"ab38ffcb-9c5b-4f47-9999-33e22529659b"));
        finalString.append(teste.get("singleBedRoom7x4", 7,4,"ab38ffcb-9c5b-4f47-9999-33e22529659b"));

        finalString.append(teste.get("doubleBedRoom7x6", 7,6,"351422f5-086e-4e9b-be72-fd4fcb48b4c5"));

        finalString.append(teste.get("bathRoom5x3", 5,3,"c1df1a90-daf0-4929-be7e-a339e90a0e3c"));



        System.out.println(teste.replaceLast(finalString.toString(), ",", ""));

    }

    String replaceLast(String string, String substring, String replacement)
    {
        int index = string.lastIndexOf(substring);
        if (index == -1)
            return string;
        return string.substring(0, index) + replacement
                + string.substring(index+substring.length());
    }

    private StringBuilder get(String name, int width, int height, String shapeID){

        int x = width;
        int y = height;
        var value = 0.6 * 40;


        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("{\"name\": " + "\"").append(name).append("\",").append("\"id\":").append("\"").append(UUID.randomUUID()).append("\",").append("\"basicShapes\":");

        var startY = 0.0;
        stringBuilder.append("[");
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                if(i == x-1 && j == y-1){
                    stringBuilder.append("{ \"translationX\":").append(0.6 * 40 * i).append(", \"id\": \"").append(shapeID).append("\", \"translationY\":").append(0.6*40*j).append("}");

                }else{
                    stringBuilder.append("{ \"translationX\":").append(0.6 * 40 * i).append(", \"id\": \"").append(shapeID).append("\", \"translationY\":").append(0.6*40*j).append("},");

                }



                startY = startY + value;
            }

        }

        stringBuilder.append("], \"compositionShapes\":[]},");

        return stringBuilder.append("\n");
    }


}
