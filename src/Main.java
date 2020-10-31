import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.awt.Point;

public class Main {
    public static void main(String [] args){
        ArrayList<Point> points = readPointsFromFile("/Users/enzoportable/Documents/AAGA/Projet1/input.points");
        int edgeThreshold = 60;

        for(Point point : points){
            System.out.println(point);
        }

        (new CDS()).calculDominatingSet(points, edgeThreshold);

    }

    private static ArrayList<Point> readPointsFromFile(String filename){
        ArrayList<Point> points = new ArrayList<Point>();

        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(filename));
            String line = reader.readLine();
            while (line != null) {
                try {
                    int x = Integer.parseInt(line.split(" ")[0]);
                    int y = Integer.parseInt(line.split(" ")[1]);
                    points.add(new Point(x, y));
                } catch (Throwable e) {
                    continue;
                }
                line = reader.readLine();

            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return points;
    }


}
