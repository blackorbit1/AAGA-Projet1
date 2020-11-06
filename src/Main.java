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
    public static void main(String [] args){
        ArrayList<Point> points = readPointsFromFile("/Users/enzoportable/Documents/AAGA/Projet1/input.points");
        int edgeThreshold = 55;



        ResultDisplay.points = points;
        ResultDisplay.edgeThreshold = edgeThreshold;



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
        Application.launch(ResultDisplay.class, args);

        /*
        ArrayList<Point> hitPoints = (new CDS()).calculDominatingSet(points, edgeThreshold);
        System.out.println(hitPoints);

        LinkedList<Steiner.Arete> steiner = new Steiner().calculSteiner(points, edgeThreshold, hitPoints);
        System.out.println(steiner);

        ResultDisplay.draw_steiner(steiner);

         */




        (new CDS()).calculDominatingSet(points, edgeThreshold);

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
