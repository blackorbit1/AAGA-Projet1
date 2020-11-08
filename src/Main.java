import javafx.application.Application;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.awt.Point;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.Thread.sleep;

public class Main {
    private static final double FACTEUR = 1.8; // facteur multiplicatif de l'ecartement des points

    public static void main(String [] args){
        //ArrayList<Point> points = readPointsFromFile("/Users/enzoportable/Documents/AAGA/Projet1/test-2.points");

        //int edgeThreshold = 55;
        int edgeThreshold = 55;

/*
        ArrayList<Point> points = GenerationPoints.get_points_set(200, (int) (edgeThreshold * 0.9), edgeThreshold);


        ResultDisplay.points = points;
        ResultDisplay.edgeThreshold = edgeThreshold;
*/


        /////////////////////////////////

        /*


        ExecutorService executorService = Executors.newSingleThreadExecutor();

        Set<Callable<String>> callables = new HashSet<Callable<String>>();

        callables.add(new Callable<String>() {
            public String call() throws Exception {
                sleep(100000);
                return "Task 1";
            }
        });
        callables.add(new Callable<String>() {
            public String call() throws Exception {
                sleep(100000);
                return "Task 2";
            }
        });
        callables.add(new Callable<String>() {
            public String call() throws Exception {
                return "Task 3";
            }
        });

        try {
            ArrayList<Future<String>> taskList = new ArrayList<>();
            BlockingQueue q = new SynchronousQueue();
            taskList.add(executors.submit(new Task1(q)));
            taskList.add(executors.submit(new Task2(q)));

            Object took = q.take();
            for (Future<String> task : taskList) {
                if (!task.isDone()) {
                    task.cancel(true);
                }
            }
            System.out.println("Got " + took);
        } catch (InterruptedException ex) {
            Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
        }

         */

        /////////////////////////////////

        ////new Thread(() -> { Application.launch(ResultDisplay.class, args); }).start();




        //Application.launch(ResultDisplay.class, args);



        /*
        ArrayList<Point> hitPoints = (new CDS()).calculDominatingSet(points, edgeThreshold);
        System.out.println(hitPoints);

        LinkedList<Steiner.Arete> steiner = new Steiner().calculSteiner(points, edgeThreshold, hitPoints);
        System.out.println(steiner);

        ResultDisplay.draw_steiner(steiner);

         */




        //(new CDS()).calculDominatingSet(points, edgeThreshold);
        tests(edgeThreshold);

    }

    public static LinkedList<Steiner.Arete> calculLiAl(ArrayList<Point> points, int edgeThreshold){
        ArrayList<Point> hitPoints = (new CDS()).calculDominatingSet(points, edgeThreshold);
        //System.out.println(hitPoints);

        LinkedList<Steiner.Arete> steiner = new Steiner().calculSteiner(points, edgeThreshold, hitPoints);
        //System.out.println(steiner);

        return steiner;
    }

    private static ArrayList<Point> readPointsFromFile(String filename){
        ArrayList<Point> points = new ArrayList<Point>();

        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(filename));
            String line = reader.readLine();
            while (line != null) {
                try {
                    int x = (int) (Integer.parseInt(line.split(" ")[0]) * FACTEUR);
                    int y = (int) (Integer.parseInt(line.split(" ")[1]) * FACTEUR);
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

    private static void tests(int edgeThreshold){
        System.out.println("début du test ..");
        //ArrayList<Point> points = GenerationPoints.get_points_set(3000, edgeThreshold, edgeThreshold);
        //ArrayList<Point> points = GenerationPoints.get_points_set(200, (int) (edgeThreshold * 0.9), edgeThreshold);

        for(int taille = 500; taille < 3000; taille += 100){
            for(int distance = 1; distance < 9; distance++){
                int dist_calcule = (((int) (edgeThreshold * (distance * 0.1)))>0)?((int) (edgeThreshold * (distance * 0.1))):2;
                System.out.println("-----------------------");
                System.out.println("taille : " + taille);
                System.out.println("distance : " + dist_calcule);
                ArrayList<Point> points = GenerationPoints.get_points_set(taille, dist_calcule, edgeThreshold);

                /*
                ResultDisplay.points = points;
                ResultDisplay.edgeThreshold = edgeThreshold;
                Application.launch(ResultDisplay.class, null);
                */

                double start_total = System.currentTimeMillis();

                ArrayList<Point> resultat = (new CDS()).calculDominatingSet(points, edgeThreshold);
                double start = System.currentTimeMillis();
                LinkedList<Steiner.Arete> steiner = new Steiner().calculSteiner(points, edgeThreshold, resultat);
                double end = System.currentTimeMillis();
                System.out.println("Steiner : " + ((end - start)/1000) + " sec");

                double end_total = System.currentTimeMillis();
                System.out.println("Total : " + ((end_total - start_total)/1000) + " sec");
                System.out.println("Score DS : " + resultat.size());


                ArrayList<Point> points_cds = new ArrayList<Point>();
                for(Steiner.Arete arete : steiner){
                    if(!points_cds.contains(arete.p1)) points_cds.add(arete.p1);
                    if(!points_cds.contains(arete.p2)) points_cds.add(arete.p2);
                }
                System.out.println("Score CDS : " + points_cds.size());

            }
        }

    }


}
