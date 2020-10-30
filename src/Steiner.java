import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;



public class Steiner {

    ///////////////////////////////////////////////
    ///////////////////////////////////////////////
    ///////                                 ///////
    ///////         Nouveaux Objets         ///////
    ///////                                 ///////
    ///////////////////////////////////////////////
    ///////////////////////////////////////////////

    /**
     * Objet représentatn une arete de graphe
     */
    private class Arete {
        public Point p1;
        public Point p2;
        public Double longueur;

        public Arete(Point p1, Point p2) {
            this.p1 = p1;
            this.p2 = p2;
            this.longueur = p1.distance(p2);
        }

        public Arete(Point p1, Point p2, Double longueur) {
            this.p1 = p1;
            this.p2 = p2;
            this.longueur = longueur;
        }

        @Override
        public String toString() {
            return "point 1 : " + p1 + "point 2 : " + p2 + "longueur : " + longueur;
        }
    }

    /**
     * Paire : permet le retour de deux variables
     */
    public class Pair<F, S> {
        public F first;
        public S second;

        public Pair(F first, S second) {
            this.first = first;
            this.second = second;
        }

        public boolean equals(Pair<F, S> paire2) {
            return (paire2.first == this.first) && (paire2.second == this.second);
        }
    }

    /**
     *
     * Objet représentant un chemin entre deux hitpoints
     * -> Il permet de traiter un tel chemin de la meme maniere qu'une arete
     *
     */
    private class Link {
        LinkedList<Arete> aretes = new LinkedList<>();
        public Point p1;
        public Point p2;
        public Double longueur;

        public Link(LinkedList<Arete> aretes) {
            this.aretes = aretes;

            this.longueur = (double) 0;
            for(Arete arete : aretes) {
                longueur += arete.longueur;
            }

            // On récupere les deux extremités du chemin
            HashMap<Point, Integer> occurence_points = new HashMap<>();
            for(Arete arete : aretes) {
                if(!occurence_points.containsKey(arete.p1)) occurence_points.put(arete.p1, 0);
                occurence_points.put(arete.p1, occurence_points.get(arete.p1) + 1);

                if(!occurence_points.containsKey(arete.p2)) occurence_points.put(arete.p2, 0);
                occurence_points.put(arete.p2, occurence_points.get(arete.p2) + 1);
            }
            ArrayList<Point> extremites = new ArrayList<>();
            for(Point point : occurence_points.keySet()) {
                if(occurence_points.get(point) == 1) extremites.add(point);
            }

            this.p1 = extremites.get(0);
            this.p2 = extremites.get(1);
        }
    }

    /**
     *
     * Objet permettant la gestion de labels de sous graphes
     *
     */
    private class Label {
        private ArrayList<Point> points;
        private int[] label;
        protected Label(ArrayList<Point> points){
            this.points = (ArrayList<Point>) points.clone();
            label = new int[points.size()];
            for (int i=0;i<points.size();i++) label[i]=i;
        }
        protected void reTag(int j, int k){
            for (int i=0;i<label.length;i++) if (label[i]==j) label[i]=k;
        }
        protected int tag(Point p){
            for (int i=0;i<points.size();i++) if (p.equals(points.get(i))) return label[i];
            return 0xBADC0DE;
        }
    }

    ////////////////////////////////////////////////////
    ////////////////////////////////////////////////////
    ///////                                      ///////
    ///////         méthodes utilitaires         ///////
    ///////                                      ///////
    ////////////////////////////////////////////////////
    ////////////////////////////////////////////////////


    private Tree2D edgesToTreeAux(LinkedList<Arete> aretes_K, Point root) {
        LinkedList<Arete> reste = new LinkedList<Arete>();
        ArrayList<Point> fils_points = new ArrayList<Point>();
        Arete current;
        while (aretes_K.size() > 0) {
            current = aretes_K.removeFirst();
            if (current.p1.equals(root)) {
                fils_points.add(current.p2);
            } else {
                if (current.p2.equals(root)) {
                    fils_points.add(current.p1);
                } else {
                    reste.add(current);
                }
            }
        }

        ArrayList<Tree2D> subTrees = new ArrayList<Tree2D>();
        for (Point fils_point: fils_points) {
            subTrees.add(edgesToTree((LinkedList<Arete>)reste.clone(),fils_point));
        }

        return new Tree2D(root, subTrees);
    }

    private Tree2D edgesToTree(LinkedList<Arete> aretes_K, Point root) {
        LinkedList<Arete> aretes = new LinkedList<Arete>();
        aretes.addAll(aretes_K);
        return edgesToTreeAux(aretes, root);
    }


    public ArrayList<Point> getPointsFromEdges(LinkedList<Arete> aretes){
        ArrayList<Point> points = new ArrayList<>();
        for(Arete arete : aretes) {
            if(!points.contains(arete.p1)) points.add(arete.p1);
            if(!points.contains(arete.p2)) points.add(arete.p2);
        }

        return points;
    }


    public LinkedList<Arete> getAretesFromPoints(ArrayList<Point> points, int edgeThreshold){
        LinkedList<Arete> liste_aretes = new LinkedList<>();
        for(Point p1 : points) {
            for(Point p2 : points) {
                if(p1 != p2 && p1.distance(p2) <= edgeThreshold) {
                    liste_aretes.add(new Arete(p1, p2));
                }
            }
        }
        return liste_aretes;
    }


    public LinkedList<Arete> getAretesFromTree(Tree2D tree){
        LinkedList<Arete> res = new LinkedList<>();
        for(Tree2D subTree : tree.getSubTrees()) {
            res.add(new Arete(tree.getRoot(), subTree.getRoot()));
            res.addAll(getAretesFromTree(subTree));
        }
        return res;
    }




    /////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////
    ///////                                               ///////
    ///////         étapes de calculs du résultat         ///////
    ///////                                               ///////
    /////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////



    public LinkedList<Arete> getAretesS(ArrayList<Point> points_g, ArrayList<Point> points_s, int[][] matrice_directions) {
        LinkedList<Arete> resultat = new LinkedList<>();

        // initialisation d'une structure de données pour savoir quelles aretes on a déjà traité (sinon elles seront toutes en double)
        HashMap<Point, ArrayList<Point>> deja_vu = new HashMap<>();
        for(Point point : points_s) {
            deja_vu.put(point, new ArrayList<>());
            deja_vu.get(point).add(point);
        }

        // On trouve le meilleur chemin pour chaque paire de points de S
        //
        //                 ______
        //  p1.__.______._/      \        ______. p2
        //       .      .         \______/
        //      /|\    /|\
        //       |      |
        //    current   |
        //     point   temp
        //
        //  current_point commence à p1
        //  temp va jusqu'à p2 avec toujours une arete d'avance sur p1
        //
        // 	On crée une arete p1 et p2 pour chaque couple de points de S
        //  On parcourt chaque points du chemin le plus court entre p1 et p2 sur G
        //  en incrémentant la distance de l'arete (p1, p2)
        //  -> Ici, la distance entre current_point et temp

        for(Point p1 : points_s) {
            for(Point p2 : points_s) {
                if(!deja_vu.get(p2).contains(p1)) {
                    Point current_point = p1;
                    Arete arete = new Arete(p1, p2, (double) 0);

                    // On parcours tout le chemin d'un bout à l'autre de l'arete pour connaitre la longueur de celle-ci
                    while(!current_point.equals(p2)) {
                        Point temp = points_g.get(matrice_directions[points_g.indexOf(current_point)][points_g.indexOf(p2)]);
                        arete.longueur = arete.longueur + current_point.distance(temp);
                        current_point = temp;
                    }
                    resultat.add(arete);

                    // On mémorise cette arete
                    deja_vu.get(p1).add(p2);
                }
            }
        }

        return resultat;
    }


    public Pair<ArrayList<Point>, LinkedList<Arete>> applyT0toG(ArrayList<Point> points_g, LinkedList<Arete> aretes_g, ArrayList<Point> points_s, LinkedList<Arete> aretes_s, int[][] matrice_directions){
        ArrayList<Point> points_res = new ArrayList<>();
        LinkedList<Arete> aretes_res = new LinkedList<>();

        HashMap<Point,ArrayList<Point>> adj = new HashMap<>();
        for(Point point : points_g) adj.put(point, new ArrayList<>());

        //Collections.reverse(aretes_s);

        // Pour chaque arete de S allant de p1 à p2, on va enregistrer toutes les aretes et les points
        // permettant d'aller de p1 à p2 sur G
        for(Arete arete : aretes_s) {
            Point current_point = arete.p1;
            if(!points_res.contains(arete.p2)) points_res.add(arete.p2);

            //                 ______
            //  p1.__.______._/      \        ______. p2
            //       .      .         \______/
            //      /|\    /|\
            //       |      |
            //    current   |
            //     point   temp
            //
            //  current_point commence à p1
            //  temp va jusqu'à p2 avec toujours une arete d'avance sur p1
            //
            // 	pour chaque couple (p1,p2), on ajoute l'adjacence entre chaque points que constitue leur lien sur G

            while(!current_point.equals(arete.p2)) {
                Point temp = points_g.get(matrice_directions[points_g.indexOf(current_point)][points_g.indexOf(arete.p2)]);
                if(!(adj.get(current_point).contains(temp) || adj.get(temp).contains(current_point))) {
                    adj.get(current_point).add(temp);
                }
                current_point = temp;

                if(!points_res.contains(current_point)) points_res.add(current_point);
            }
        }

        // On transforme la liste d'adjacence en aretes
        //
        // -> On n'a pas enregistré les aretes directement lors du parcours des points effectué au dessus
        //    pour ne pas avoir à faire la couteuse vérification, à chaque ajout d'arete, qu'il n'y a pas
        //    la même arete mais avec p1 et p2 intervertis

        for(Point p1 : adj.keySet()) {
            for(Point p2 : adj.get(p1)) {
                aretes_res.add(new Arete(p1, p2));
            }
        }


        return new Pair<ArrayList<Point>, LinkedList<Arete>>(points_res, aretes_res);
    }

    /**
     *  permet d'obtenir une matrice donnant pour deux points d'indice i et j l'indice k
     *  du prochain sommet dans un plus court chemin de i à j
     *
     * @param points
     * @param edgeThreshold
     * @return une matrice à deux dimensions
     */
    public int[][] calculShortestPaths(ArrayList<Point> points, int edgeThreshold) {
        int[][] paths = new int[points.size()][points.size()];
        for (int i=0;i<paths.length;i++) for (int j=0;j<paths.length;j++) paths[i][j]=i;

        double[][] dist = new double[points.size()][points.size()];

        for (int i=0;i<paths.length;i++) {
            for (int j=0;j<paths.length;j++) {
                if (i==j) {dist[i][i]=0; continue;}
                if (points.get(i).distance(points.get(j)) <= edgeThreshold) dist[i][j]=points.get(i).distance(points.get(j)); else dist[i][j]=Double.POSITIVE_INFINITY;
                paths[i][j]=j;
            }
        }

        for (int k=0;k<paths.length;k++) {
            for (int i=0;i<paths.length;i++) {
                for (int j=0;j<paths.length;j++) {
                    if (dist[i][j]>dist[i][k] + dist[k][j]){
                        dist[i][j]=dist[i][k] + dist[k][j];
                        paths[i][j]=paths[i][k];
                    }
                }
            }
        }

        return paths;
    }






    /**
     *  Algorithme de Kruskal
     *
     * @param points (tous les points du graphe)
     * @param aretes_original (toutes les aretes du graphe)
     * @return un arbre, couvrant le graphe fourni, de taille minimale
     */
    public LinkedList<Arete> kruskal(ArrayList<Point> points, LinkedList<Arete> aretes_original) {
        LinkedList<Arete> resultat = new LinkedList<Arete>();
        LinkedList<Arete> aretes = new LinkedList<Arete>();

        // On récupere les aretes et on les tries
        aretes.addAll(aretes_original);
        aretes.sort((o1, o2) -> o1.longueur.compareTo(o2.longueur));


        Arete current;
        Label foret = new Label(points);

        // Tant qu'il reste des aretes, on ajoute les aretes triées en vérifiant qu'on ne fait pas de boucle
        while (aretes.size() > 0) {
            current = aretes.remove(0);
            // Si les deux bouts de l'arete ont le meme label, son ajout créerait une boucle
            if (foret.tag(current.p1)!=foret.tag(current.p2)) {
                resultat.add(current);
                foret.reTag(foret.tag(current.p1),foret.tag(current.p2));
            }
        }
        return resultat;
    }



    /**
     *  Fonction reccursive permettant d'obtenir la liste des liens entre chaque hitpoints
     *  de l'arbre encapsulés dans des objets Link
     *
     * @param arbre
     * @param link_road (accumulateur d'aretes pour chaque chemin entre deux hitpoints)
     * @param hit_points
     * @return la liste des liens entre chaque hitpoints de l'arbre
     */
    public LinkedList<Link> getLinks(Tree2D arbre, LinkedList<Arete> link_road, ArrayList<Point> hit_points){
        LinkedList<Link> result = new LinkedList<>();

        // Si on se trouve sur un hitpoint, ça veut dire qu'on a fait l'entiereté d'un lien entre 2 hitpoint
        if(hit_points.contains(arbre.getRoot()) && link_road.size() > 0) {
            Link link = new Link(link_road);
            result.add(link);
            // On recommence l'accumulation d'aretes à partir de ce point
            link_road = new LinkedList<>();
        }

        // On continue la chaine d'appels reccursifs pour chaque fils en ajoutant l'arete [racine --- fils] à la liste d'aretes
        for(Tree2D fils : arbre.getSubTrees()) {
            LinkedList<Arete> link_road_temp;

            // S'il y a plus de 1 fils, on fait un crée un nouveau  link_road pour pas qu'il y a de conflit
            // dans la suite de la reccursion
            if(arbre.getSubTrees().size() > 1) {
                link_road_temp = new LinkedList<>();
                link_road_temp.addAll(link_road);
            } else {
                link_road_temp = link_road;
            }
            link_road_temp.add(new Arete(arbre.getRoot(), fils.getRoot()));
            result.addAll(getLinks(fils, link_road_temp, hit_points));
        }

        return result;
    }

    /**
     *  Algorithme de construction d'une approximation d'un arbre de Steiner
     *
     * @param points (points du graphe)
     * @param edgeThreshold (distance en dessous de laquelle il y a une arete entre deux points)
     * @param hitPoints (points à traverser)
     *
     * @return la liste d'aretes constituant l'arbre de Steiner
     */
    public LinkedList<Arete> simpleSteiner(ArrayList<Point> points, int edgeThreshold, ArrayList<Point> hitPoints){
        int[][] matrice_directions = calculShortestPaths(points, edgeThreshold);

        // Construction de l'arbre K
        LinkedList<Arete> aretesS = getAretesS(points, hitPoints, matrice_directions);

        // Kruskal sur K
        LinkedList<Arete> aretes_K = kruskal(hitPoints, aretesS);

        // appliquer K à G -> H
        Pair<ArrayList<Point>, LinkedList<Arete>> paire = applyT0toG(points, getAretesFromPoints(points, edgeThreshold), hitPoints, aretes_K, matrice_directions);
        ArrayList<Point> points_H = (ArrayList<Point>) paire.first;
        LinkedList<Arete> aretes_H = (LinkedList<Arete>) paire.second;

        // Kruskal sur H
        return kruskal(points_H, aretes_H);
    }

    /**
     *  Algorithme de construction d'une approximation d'un arbre de Steiner avec un budget donné
     *
     * @param points
     * @param edgeThreshold
     * @param hitPoints
     * @return
     */
    public LinkedList<Arete> budgetSteiner(ArrayList<Point> points, int edgeThreshold, ArrayList<Point> hitPoints, Point mother_house, int budget){
        // On appelle la méthode permettant de creer une arbre de Steiner
        LinkedList<Arete> aretes_steiner = simpleSteiner(points, edgeThreshold, hitPoints);


        // On recupere les liens entre chaque hitpoints
        LinkedList<Link> links = getLinks(edgesToTree(aretes_steiner, hitPoints.get(0)), new LinkedList<Arete>(), hitPoints);

        // On calcule le cout de cet arbre et on ajoute toutes les aretes dans une liste qui sera retourné à la fin de la fonction
        LinkedList<Arete> aretes_finales = new LinkedList<>();
        float cout = 0;

        for(Link link : links) {
            aretes_finales.addAll(link.aretes);
            cout += link.longueur;
        }

        LinkedList<Link> links_retirees = new LinkedList<>();

        // tant qu'on est au dessus du budget
        while(cout > budget) {
            // On récupere les extremités de l'arbre
            HashMap<Point, Integer> occurence_points = new HashMap<>();
            for(Link link : links) {
                if(!occurence_points.containsKey(link.p1)) occurence_points.put(link.p1, 0);
                occurence_points.put(link.p1, occurence_points.get(link.p1) + 1);

                if(!occurence_points.containsKey(link.p2)) occurence_points.put(link.p2, 0);
                occurence_points.put(link.p2, occurence_points.get(link.p2) + 1);
            }

            LinkedList<Link> links_candidates = new LinkedList<>();
            for(Link link : links) {
                if(occurence_points.get(link.p1) == 1) links_candidates.add(link);
                if(occurence_points.get(link.p2) == 1) links_candidates.add(link);
            }

            // On les tries
            links_candidates.sort((o1, o2) -> o1.longueur.compareTo(o2.longueur));


            // On retire le lien entre deux hitpoints le plus couteux
            Link link_to_remove = links_candidates.getLast();

            // Si jamais on se rend compte que le lien le plus couteux contient le point "maison mere", on en cherche un moins couteux qui ne le contienne pas
            if(link_to_remove.p1.equals(mother_house) || link_to_remove.p2.equals(mother_house))
                for(int i = links_candidates.size() - 1; link_to_remove.p1.equals(mother_house) || link_to_remove.p2.equals(mother_house); i--)
                    link_to_remove = links_candidates.get(i);

            // On retire toutes les aretes du lien retiré
            aretes_finales.removeAll(link_to_remove.aretes);
            cout -= link_to_remove.longueur;

            links_retirees.add(link_to_remove);
            links.remove(link_to_remove);


            // On vérifie si le retrait de toutes ces aretes ne permet pas le replacement d'un
            // ancien lien moins couteux

            Link link_to_reuse = links_retirees.get(0);
            Double cost_link_to_reuse = Double.MAX_VALUE;


            for(Link link : links_retirees) {
                if((occurence_points.containsKey(link.p1) || occurence_points.containsKey(link.p2))
                        && (cout + link.longueur) <= budget
                        && link.longueur < cost_link_to_reuse) {
                    link_to_reuse = link;
                    cost_link_to_reuse = link.longueur;
                }
            }

            if(cost_link_to_reuse != Double.MAX_VALUE) {
                links.add(link_to_reuse);
                aretes_finales.addAll(link_to_reuse.aretes);

                links_retirees.remove(link_to_reuse);
                cout += link_to_reuse.longueur;
            }


        }

        return aretes_finales;

    }

    ///////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////
    ///////                                                     ///////
    ///////         méthodes appellées par le programme         ///////
    ///////                                                     ///////
    ///////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////


    /**
     * Méthode appellée par le programme pour construire un arbre de steiner sans budget
     */
    public Tree2D calculSteiner(ArrayList<Point> points, int edgeThreshold, ArrayList<Point> hitPoints) {
        // On appelle la méthode permettant de creer une arbre de Steiner
        LinkedList<Arete> aretes_steiner = simpleSteiner(points, edgeThreshold, hitPoints);

        return edgesToTree(aretes_steiner, aretes_steiner.get(0).p1);
    }

    /**
     * Méthode appellée par le programme pour construire un arbre de steiner avec budget
     */
    public Tree2D calculSteinerBudget(ArrayList<Point> points, int edgeThreshold, ArrayList<Point> hitPoints) {
        // Point "maison mere"
        Point mother_house = hitPoints.get(0);

        // Budget
        int budget = 1664;


        LinkedList<Arete> aretes_steiner = budgetSteiner(points, edgeThreshold, hitPoints, mother_house, budget);


        return edgesToTree(aretes_steiner, aretes_steiner.get(0).p1);
    }
}
