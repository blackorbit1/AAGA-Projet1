import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

public class GenerationPoints {
    private static float moyenne_distance;

    public static ArrayList<Point> get_points_set(int nb_points, int espacement, int edgeThreshold){
        ArrayList<Point> resultat = new ArrayList<>();
        moyenne_distance = 0;
        //System.out.println(espacement);

        // la zone est un carré
        int cote = (int) (Math.sqrt(nb_points) * espacement);

        // On construit le graphe à partir d'un point central
        resultat.add(new Point((int) (cote/2), (int) (cote/2)));

        while(resultat.size() < nb_points){
            int x = (int) (Math.random() * cote);
            int y = (int) (Math.random() * cote);

            Point newPoint = new Point(x, y);
            if(isConnexe(resultat, newPoint, espacement, edgeThreshold)) {
                resultat.add(newPoint);
            }
        }


        //System.out.println(moyenne_distance);

        return resultat;
    }



    private static boolean isConnexe(ArrayList<Point> points, Point point, int espacement, int edgeThreshold){
        for(Point temp : points){

            double distance_totale = 0;
            int nb_voisins = 0;

            if (point.distance(temp) < edgeThreshold) {
                for(Point temp2 : points){
                    if (point.distance(temp2) < edgeThreshold){
                        if(nb_voisins != 0 && !(((distance_totale/nb_voisins) > espacement) || (moyenne_distance > (espacement)))){
                            return false;
                        }
                        distance_totale += point.distance(temp2);
                        nb_voisins++;
                    }

                }
                //System.out.println((distance_totale/nb_voisins));
                moyenne_distance = (float) ((moyenne_distance * points.size() + (distance_totale/nb_voisins)) / (points.size() + 1));
                return true;
            }
        }
        return false;
    }
}
