import javafx.application.Platform;
import javafx.concurrent.Task;

import java.awt.Point;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.*;

public class CDS {

    private static final int TEMPS_MAX = 60 * 10; // (sec) Temps d'execution maximal
    private double debut_iteration;

    public class Simulation extends Thread {
        private int n;
        private ArrayList<Point> old_result;
        private ArrayList<Point> points;
        private int edgeThreshold;
        private ArrayList<Point> result;


        ArrayList<Point> getResult() {return result;}

        Simulation (int n, ArrayList<Point> old_result, ArrayList<Point> points, int edgeThreshold, String name, ThreadGroup tg) {
            super(tg,name);

            this.n = n;
            this.old_result = old_result;
            this.points = points;
            this.edgeThreshold = edgeThreshold;
        }
        public void run() {
            //System.out.println("appele");
            result = replaceNred(n, old_result, points, edgeThreshold);
            //System.out.println("termine");
        }
    }


    public ArrayList<Point> calculDominatingSet(ArrayList<Point> points, int edgeThreshold) {
        // if (false) result = readFromFile("output0.points");
        // else saveToFile("output",result);
        ArrayList<Point> rest = (ArrayList<Point>)points.clone();
        ArrayList<Point> result = new ArrayList<Point>();
        boolean readFromFile = false;
        boolean enhance = true;
        //int number_of_threads = 4;

        if (readFromFile) result = readFromFile("output57.points");

        double start = System.currentTimeMillis();
        if(!readFromFile){
            while (!rest.isEmpty()){
                Point kimK=rest.get(0);
                for (Point p: rest)
                    if (neighbours(p,rest,edgeThreshold)>neighbours(kimK,rest,edgeThreshold))
                        kimK=p;
                result.add(kimK);
                for (int i=0;i<rest.size();i++)
                    if (rest.get(i).distance(kimK)<=edgeThreshold){
                        rest.remove(i);
                        i--;
                    }
            }
        }
        int score = result.size();
        double end = System.currentTimeMillis();
        System.out.println("Glouton : " + ((end - start)/1000) + " sec");

        /*
        start = System.currentTimeMillis();
        LinkedList<Steiner.Arete> steiner = new Steiner().calculSteiner(points, edgeThreshold, result);
        end = System.currentTimeMillis();
        System.out.println("fin steiner : " + ((end - start)/1000) + " sec");

        final LinkedList<Steiner.Arete> steiner_final = (LinkedList<Steiner.Arete>) steiner.clone();
        Platform.runLater(new Runnable() {
            @Override public void run() {
                ResultDisplay.display_li_al(steiner_final);
            }
        });
        //ResultDisplay.display_li_al(steiner);
        
         */
        /*
        final LinkedList<Steiner.Arete> steiner_final = (LinkedList<Steiner.Arete>) steiner.clone();
        new Thread(() -> {

            ResultDisplay.display_li_al(steiner_final);
        }).start();
        */





		/*
		for(Point point : points) {
			ArrayList<Point> temp = getThisAsRed(2, result, points, point, edgeThreshold);
			if(isValidSolution(points, temp, edgeThreshold)) result = temp;
		}
		*/

        //int NB_PTS_A_PLACER = 80;

        int np = Runtime.getRuntime().availableProcessors();

        start = System.currentTimeMillis();

        double moyenne_temps_amelioration = 0;

        if(enhance) {
            //System.out.println("Score : "+result.size());

            int essais = 0;
            int reussites = 0;
            int n = 3;

            this.debut_iteration = System.currentTimeMillis();

            while((essais < (300/np)) || (reussites / essais > 0.05)) {
                //System.out.println("n = " + n);
                //System.out.println("essais = " + essais);
                //System.out.println("reussites = " + reussites);
                //System.out.println("--- --- ---");
                //result = replaceNred(n, result, points, edgeThreshold);



                // On bloque n à 3
                //n = 3;
                //np = 1;








                ThreadGroup tg = new ThreadGroup("main");

                List<Simulation> sims = new ArrayList<Simulation>();
                for (int i=0;i<np;i++) sims.add(new Simulation(n, result, points, edgeThreshold,"PI"+i, tg));

                int i=0;
                while (i<sims.size()){
                    if (tg.activeCount()<np){ // do we have available CPUs?
                        Simulation sim = sims.get(i);
                        sim.start();
                        i++;
                    } else {
                        try {Thread.sleep(100);} /*wait 0.1 second before checking again*/
                        catch (InterruptedException e) {e.printStackTrace();}
                    }

                }

                // on ne va pas attendre que tous les threads terminent pour commencer à récupérer les résultats
                /*
                while(tg.activeCount()>0) { // wait for threads to finish
                    try {Thread.sleep(100);}
                    catch (InterruptedException e) {e.printStackTrace();}
                }
                */

                while(tg.activeCount()>0){
                    for (i=0;i<sims.size();i++) {
                        Simulation sim = sims.get(i);
                        ArrayList<Point> sim_result = sim.getResult();
                        if(sim_result != null && sim_result.size() < result.size()){
                            result = sim_result;
                            for(Simulation sim_to_stop : sims){
                                sim_to_stop.stop();
                            }
                        }
                    }
                }








                essais++;
                boolean reussi = false;
                if(result.size() < score) {
                    reussites++;

                    reussi = true;



                    Date date = Calendar.getInstance().getTime();
                    DateFormat dateFormat = new SimpleDateFormat("hh:mm:ss");
                    String strDate = dateFormat.format(date);

                    System.out.println(strDate + " " + result.size());

                    /*
                    ArrayList<Point> finalResult = result;
                    Platform.runLater(new Runnable() {
                        @Override public void run() {
                            final LinkedList<Steiner.Arete> steiner_final2 = new Steiner().calculSteiner(points, edgeThreshold, finalResult);
                            ResultDisplay.display_li_al(steiner_final2);
                        }
                    });
                    */
                    //saveToFile("output",result);
                }
                score = result.size();

                double fin_iteration = System.currentTimeMillis();

                // Si il commence à y avoir un vrai ralentissement
                if(reussites > 0 && essais > 0 && (fin_iteration - debut_iteration) > (moyenne_temps_amelioration * 3 + (1000 * 60))){
                    break;
                }
                //System.out.println((fin_iteration - debut_iteration) + " " + moyenne_temps_amelioration + " " + (moyenne_temps_amelioration * 3 + (1000 * 60)));

                if(!(essais < (300/np)) || (reussites / essais > 0.05)){
                    n++;
                    essais = 0;
                    reussites = 0;
                }


                end = System.currentTimeMillis();
                if((end - start) > (TEMPS_MAX * 1000)) {
                    break;
                }

                if(reussi) {
                    moyenne_temps_amelioration = ((moyenne_temps_amelioration * essais) + (fin_iteration - debut_iteration)) / (essais + 1);
                    debut_iteration = System.currentTimeMillis();
                }
            }

        }






        //System.out.println("fin");


        //saveToFile("output",result);
        return result;
    }


    public int neighbours(Point p, ArrayList<Point> points, int edgeThreshold) {
        int res=-1;
        for (Point q: points) if (p.distance(q)<=edgeThreshold) res++;
        return res;
    }

    private boolean areNeighbor(Point p1, Point p2, int edgeThreshold) {
        if (p1.distance(p2) <= edgeThreshold) return true;
        return false;
    }

    private ArrayList<Point> getNeighbours(ArrayList<Point> points, Point point, int edgeThreshold){
        ArrayList<Point> result = new ArrayList<>();
        for(Point candidate : points)
            if(point.distance(candidate) <= edgeThreshold) result.add(candidate);
        return result;
    }

    private ArrayList<Point> getThisAsRed(int n, ArrayList<Point> old_result, ArrayList<Point> points, Point point, int edgeThreshold) {
        ArrayList<Point> new_result = (ArrayList<Point>) old_result.clone();
        new_result.remove(point);
        if(n > 0) {
            ArrayList<Point> neighbours = getNeighbours(points, point, edgeThreshold);
            for(Point neighbour : neighbours) {
                new_result = getThisAsRed(n - 1, new_result, points, neighbour, edgeThreshold);
            }
        }
        return new_result;

    }

    private boolean isValidSolution(ArrayList<Point> points, ArrayList<Point> red_points, int edgeThreshold) {
        for(Point point : points) {
            ArrayList<Point> neighbours = getNeighbours(points, point, edgeThreshold);
            boolean isRed = red_points.contains(point);
            if(!isRed) {
                int nb_red_neighbours = 0;
                for(Point neighbour : neighbours) {
                    if(red_points.contains(neighbour)) nb_red_neighbours++;
                }
                if(nb_red_neighbours == 0) {
                    return false;
                }
            }
        }
        return true;
    }


    private boolean isValidSolution2(ArrayList<Point> points, ArrayList<Point> red_points, int edgeThreshold) {
        for(Point point : points) {
            if(!red_points.contains(point)) {
                int nb_red_neighbours = 0;
                for(Point neighbour : getNeighbours(points, point, edgeThreshold)) {
                    if(red_points.contains(neighbour)) nb_red_neighbours++;
                }
                if(nb_red_neighbours == 0) return false;
            }
        }
        return true;
    }


    private ArrayList<ArrayList<Point>> getNcombinaison(int n, Point point, ArrayList<Point> points, int edgeThreshold){
        ArrayList<ArrayList<Point>> result = new ArrayList<>();
        //System.out.print("-NC");
        if(n>0) {
            for(Point p : points) {
                if(p.distance(point) <= n*edgeThreshold*2) {
                    ArrayList<Point> rest_of_points = (ArrayList<Point>) points.clone();
                    rest_of_points.remove(p);
                    for(ArrayList<Point> temp : getNcombinaison(n-1, p, rest_of_points, edgeThreshold)) {
                        if(temp.size() == n) {
                            temp.add(p);
                            result.add(temp);
                        } else {
                            //System.out.println("Taille de la liste temp : " + temp.size());
                        }
                    }
                }
            }
        }
        return result;
    }

    private ArrayList<Point> replaceNred(int n, ArrayList<Point> old_result, ArrayList<Point> points, int edgeThreshold){
        ArrayList<Point> new_result = (ArrayList<Point>) old_result.clone();
        ArrayList<ArrayList<Point>> list_of_blue_candidates = new ArrayList<>();

		/*
		for(Point p : points) {
			//System.out.println("==p==");
			ArrayList<Point> rest_of_points = (ArrayList<Point>) points.clone();
			rest_of_points.remove(p);
			for(ArrayList<Point> temp : getNcombinaison(n-1, p, rest_of_points, edgeThreshold)) {
				temp.add(p);
				list_of_blue_candidates.add(temp);
			}
		}
		*/

        int score = old_result.size();
        Random random = new Random();

        //System.out.println("new_result.size() : " + new_result.size());
        //System.out.println("score : " + score);
        //System.out.println("new_result.size() >= score : " + (new_result.size() >= score));

        int tentatives = 0;
        while(!(new_result.size() < score) && (tentatives++ < 20)) {
            //System.out.println("--new iteration");
            ArrayList<Point> red_to_replace = new ArrayList<>();
            int iterations = 0;// Pour éviter que ça boucle à l'infini si on est dans une mauvaise situation
            while(red_to_replace.size() < n) {
                if(iterations++ > 100*n*n) {
                    //System.out.println("blocage : " + (iterations-1));
                    //System.out.println("n : " + n);
                    //System.out.println(red_to_replace);
                    //return red_to_replace;
                    if((System.currentTimeMillis() - debut_iteration) > 1000*120) return old_result;
                    red_to_replace.clear();
                    iterations = 0;
                }
                Point temp = old_result.get(random.nextInt(old_result.size()));
                //System.out.println(">>" + temp);
                if(!red_to_replace.contains(temp)) {
                    boolean valide_candidate = false;
                    for(Point temp2 : red_to_replace) {
                        if(temp.distance(temp2) <= edgeThreshold*2) valide_candidate = true;
                    }
                    if(valide_candidate || red_to_replace.isEmpty()) red_to_replace.add(temp);
                }
                //System.out.println(">>" + red_to_replace);
            }
            //System.out.println("--");
            // on encadre les coordonnées possibles pour les nouveaux rouges
            int x_min = Integer.MAX_VALUE;
            int x_max = 0;
            int y_min = Integer.MAX_VALUE;
            int y_max = 0;

            for(Point p : red_to_replace) {
                if(p.x < x_min) x_min = p.x;
                if(p.x > x_max) x_max = p.x;
                if(p.y < y_min) y_min = p.y;
                if(p.y > y_max) y_max = p.y;
            }
            //System.out.println("--");


            int finalX_min = x_min;
            int finalX_max = x_max;
            int finalY_min = y_min;
            int finalY_max = y_max;
            /*
            Platform.runLater(new Runnable() {
                @Override public void run() {
                    //final LinkedList<Steiner.Arete> steiner_final2 = new Steiner().calculSteiner(points, edgeThreshold, finalResult);
                    ResultDisplay.display_rectangle(finalX_min, finalX_max, finalY_min, finalY_max);
                }
            });
            */


            // on récupere tous les points possibles
            ArrayList<Point> possible_blue_to_red = new ArrayList<>();
            for(Point p : points) {
                if(
                        p.x >= (x_min - edgeThreshold)
                    &&	p.x <= (x_max + edgeThreshold)
                    &&	p.y >= (y_min - edgeThreshold)
                    &&	p.y <= (y_max + edgeThreshold)
                ) {
                    possible_blue_to_red.add(p);
                }
            }
            //System.out.println("-- possible_blue_to_red : " + possible_blue_to_red);

			/*

			ArrayList<Point> blue_to_red = new ArrayList<>();
			for(Point red_candidate : possible_blue_to_red) {
				// TODO : est ce qu'il ne faudrait pas mettre un peu de hasard ici ?
				boolean valide_candidate = true;

				for(Point temp : blue_to_red) {
					if(red_candidate.distance(temp) < edgeThreshold) valide_candidate = false;
				}
				if(valide_candidate) {
					blue_to_red.add(red_candidate);
					if(blue_to_red.size() == n-1) break;
				}
			}

			ArrayList<Point> new_solution = (ArrayList<Point>) old_result.clone();
			for(Point red_a_supp : red_to_replace) {
				new_solution.remove(red_a_supp);
			}
			new_solution.removeAll(red_to_replace);
			new_solution.addAll(blue_to_red);

			//if(true) return new_solution;

			if(isValidSolution2(points, blue_to_red, edgeThreshold)) return new_solution;

			*/


            int essais = 0;
            while(++essais < n*n*100) {
                ArrayList<Point> blue_to_red = new ArrayList<>();

                int iterations_recherche = 0;
                while(iterations_recherche < n*n*1000) {
                    Point red_candidate = possible_blue_to_red.get(random.nextInt(possible_blue_to_red.size()));
                    boolean valide_candidate = true;
                    for(Point temp : blue_to_red) {
                        if(red_candidate.distance(temp) < edgeThreshold) valide_candidate = false;
                    }
                    if(valide_candidate) {
                        blue_to_red.add(red_candidate);
                        if(blue_to_red.size() == n-1) break;
                    }
                    iterations_recherche++;
                    // a supp
                    if(iterations_recherche == n*n*1000); //System.out.println("!! iterations recherche depassé : " + iterations_recherche);
                }

                ArrayList<Point> new_solution = (ArrayList<Point>) old_result.clone();
                for(Point red_a_supp : red_to_replace) {
                    new_solution.remove(red_a_supp);
                }
                new_solution.removeAll(red_to_replace);
                new_solution.addAll(blue_to_red);

                //if(true) return new_solution;
                //System.out.println("test : " + new_solution.size());

                if(isValidSolution2(points, new_solution, edgeThreshold)) return new_solution;
            }
            //System.out.println("rien pour ces rouges");






			/*

			// on récupere toutes les permutations de n-1 points parmis tous les points possibles ---> new_red_temp
			Point pt_imaginaire = new Point(x_max + (x_min - x_max)/2, y_max + (y_min - y_max)/2);
			//pt_imaginaire = possible_blue_to_red.get(0);
			ArrayList<ArrayList<Point>> new_red_temp = getNcombinaison(n, pt_imaginaire, possible_blue_to_red, edgeThreshold);

			System.out.println("++");

			// on regarde si (old_result - red_to_replace) + new_red_temp est valide
			//		si oui on l'ajoute à une liste de solutions
			//			-> Pour l'instant ne retourner que le 1e element de la liste de sol
			//			-> Ensuite, faire un "break" et repartir à la recherche de sol à partir de cette solution
			//			   en regardant dans les red_to_replace qu'il reste (et en faisant attention à ce qu'il n'ait
			//			   pas de points rouges obsoletes (qui ont été supp)).
			System.out.println("new_red_temp : " + new_red_temp);
			for(ArrayList<Point> proposition_red : new_red_temp) {
				System.out.println("P=");
				if(isValidSolution(points, proposition_red, edgeThreshold)) {
					System.out.println(proposition_red);
					return proposition_red;
				}
			}
			System.out.println("=== === === ===");
			*/

        }

		/*


		for(ArrayList<Point> red_to_replace : list_of_blue_candidates) {
			// on encadre les coordonnées possibles pour les nouveaux rouges
			int x_min = Integer.MAX_VALUE;
			int x_max = 0;
			int y_min = Integer.MAX_VALUE;
			int y_max = 0;

			for(Point p : red_to_replace) {
				if(p.x < x_min) x_min = p.x;
				if(p.x > x_max) x_max = p.x;
				if(p.y < y_min) y_min = p.y;
				if(p.y > y_max) y_max = p.y;
			}

			// on récupere tous les points possibles
			ArrayList<Point> possible_blue_to_red = new ArrayList<>();
			for(Point p : points) {
				if(
						p.x >= (x_min - edgeThreshold)
					&&	p.x <= (x_max + edgeThreshold)
					&&	p.y >= (y_min - edgeThreshold)
					&&	p.y <= (y_max + edgeThreshold)
				) {
					possible_blue_to_red.add(p);
				}
			}

			// on récupere toutes les permutations de n-1 points parmis tous les points possibles ---> new_red_temp
			ArrayList<ArrayList<Point>> new_red_temp = getNcombinaison(n, new Point(x_max + (x_min - x_max)/2, y_max + (y_min - y_max)/2), possible_blue_to_red, edgeThreshold);


			// on regarde si (old_result - red_to_replace) + new_red_temp est valide
			//		si oui on l'ajoute à une liste de solutions
			//			-> Pour l'instant ne retourner que le 1e element de la liste de sol
			//			-> Ensuite, faire un "break" et repartir à la recherche de sol à partir de cette solution
			//			   en regardant dans les red_to_replace qu'il reste (et en faisant attention à ce qu'il n'ait
			//			   pas de points rouges obsoletes (qui ont été supp)).
			for(ArrayList<Point> proposition_red : new_red_temp) {
				//System.out.println("proposition_red");
				if(isValidSolution(points, proposition_red, edgeThreshold)) {
					return proposition_red;
				}
			}
		}

		*/







		/*
		new_result.remove(point);
		if(n > 0) {
			ArrayList<Point> neighbours = getNeighbours(points, point, edgeThreshold);
			for(Point neighbour : neighbours) {
				new_result = getThisAsRed(n - 1, new_result, points, neighbour, edgeThreshold);
			}
		}
		return new_result;
		*/
        //System.out.println("RIEN TROUVÉ !!! ");
        return old_result;
    }


    private ArrayList<ArrayList<Point>> getNpermutation(){
        return null;
    }


















    //FILE PRINTER
    private void saveToFile(String filename,ArrayList<Point> result){
        int index=0;
        try {
            while(true){
                BufferedReader input = new BufferedReader(new InputStreamReader(new FileInputStream(filename+Integer.toString(index)+".points")));
                try {
                    input.close();
                } catch (IOException e) {
                    System.err.println("I/O exception: unable to close "+filename+Integer.toString(index)+".points");
                }
                index++;
            }
        } catch (FileNotFoundException e) {
            printToFile(filename+Integer.toString(index)+".points",result);
        }
    }
    private void printToFile(String filename,ArrayList<Point> points){
        try {
            PrintStream output = new PrintStream(new FileOutputStream(filename));
            int x,y;
            for (Point p:points) output.println(Integer.toString((int)p.getX())+" "+Integer.toString((int)p.getY()));
            output.close();
        } catch (FileNotFoundException e) {
            System.err.println("I/O exception: unable to create "+filename);
        }
    }

    //FILE LOADER
    private ArrayList<Point> readFromFile(String filename) {
        String line;
        String[] coordinates;
        ArrayList<Point> points=new ArrayList<Point>();
        try {
            BufferedReader input = new BufferedReader(
                    new InputStreamReader(new FileInputStream(filename))
            );
            try {
                while ((line=input.readLine())!=null) {
                    coordinates=line.split("\\s+");
                    points.add(new Point(Integer.parseInt(coordinates[0]),
                            Integer.parseInt(coordinates[1])));
                }
            } catch (IOException e) {
                System.err.println("Exception: interrupted I/O.");
            } finally {
                try {
                    input.close();
                } catch (IOException e) {
                    System.err.println("I/O exception: unable to close "+filename);
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println("Input file not found.");
        }
        return points;
    }
}
