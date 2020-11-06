import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.awt.Point;
import java.util.ArrayList;
import java.util.LinkedList;

public class ResultDisplay extends Application {

    public static ArrayList<Point> points = null;
    public static int edgeThreshold = 0;

    public static int width = 1500;
    public static int height = 1000;

    private static Stage stage;
    private static StackPane pane;
    private static Scene scene;

    private static int li_al_position = -1;
    private static int label_score_position = -1;

    public static Stage getStage() {
        return stage;
    }


    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;
        pane = new StackPane();
        Canvas sharpCanvas = drawGraph(width,  height, points, edgeThreshold);
        //Canvas blurryCanvas = createCanvasGrid(600, 300, false);
        pane.getChildren().add(sharpCanvas);

        Button button = new Button("Calculer Li & al.");
        button.setTranslateY(-(height/2 - 50));
        button.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                Task task = new Task<Void>() {
                    @Override public Void call() {
                        new CDS().calculDominatingSet(points, edgeThreshold);
                        return null;
                    }
                };
                new Thread(task).start();
            }
        });

        pane.getChildren().add(button);
        scene = new Scene(pane, width, height);

        stage.setScene(scene);
        stage.show();
    }

    public static void display_li_al(LinkedList<Steiner.Arete> aretes, int score){
        Stage stage = getStage();
        Canvas canvas = new Canvas(width, height);
        GraphicsContext gc = canvas.getGraphicsContext2D() ;

        gc.setStroke(Color.RED);
        gc.setLineWidth(3);

        ArrayList<Point> points_cds = new ArrayList<Point>();
        for(Steiner.Arete arete : aretes){
            if(!points_cds.contains(arete.p1)) points_cds.add(arete.p1);
            if(!points_cds.contains(arete.p2)) points_cds.add(arete.p2);
        }

        for(Steiner.Arete arete : aretes){
            gc.strokeLine(arete.p1.x, arete.p1.y, arete.p2.x, arete.p2.y);
        }

        System.out.println(">>>> Le score : " + points_cds.size());
        Label label_score = new Label("Score : " + points_cds.size());
        label_score.setTranslateY(-(height/2 - 100));
        label_score.setTranslateX(-(width/2 - 100));
        label_score.setFont(new Font(20));

        //System.out.println("pane stack : " + pane.getChildren());

        // On enleve le dernier resultat s'il y en a un
        if(li_al_position > label_score_position) if(li_al_position > -1) pane.getChildren().remove(li_al_position);
        if(label_score_position > -1) pane.getChildren().remove(label_score_position);
        if(li_al_position > -1) pane.getChildren().remove(li_al_position);

        li_al_position = pane.getChildren().size();
        pane.getChildren().add(canvas);
        label_score_position = pane.getChildren().size();
        pane.getChildren().add(label_score);

        // affichage
        scene.setRoot(pane);
        stage.setScene(scene);
        stage.show();
    }



    private Canvas drawGraph(int width, int height, ArrayList<Point> points, int edgeThreshold) {
        Canvas canvas = new Canvas(width, height);
        GraphicsContext gc = canvas.getGraphicsContext2D() ;

        gc.setStroke(Color.GRAY);

        for(Point p1 : points){
            for(Point p2 : points){
                if(p1.distance(p2) < edgeThreshold) gc.strokeLine(p1.x, p1.y, p2.x, p2.y);
            }

        }
        for(Point point : points){
            int circle_diameter = 5;
            gc.fillOval(point.x-((int) circle_diameter/2),point.y-((int) circle_diameter/2),circle_diameter, circle_diameter);
        }
        /*
        gc.setLineWidth(1.0);
        for (int x = 0; x < width; x+=10) {
            double x1 ;

            x1 = x ;

            gc.moveTo(x1, 0);
            gc.lineTo(x1, height);
            gc.stroke();
        }

        for (int y = 0; y < height; y+=10) {
            double y1 ;
            if (sharp) {
                y1 = y + 0.5 ;
            } else {
                y1 = y ;
            }
            gc.moveTo(0, y1);
            gc.lineTo(width, y1);
            gc.stroke();
        }

         */
        return canvas ;
    }

    public static void main(String[] args) {
        launch(args);
    }
}