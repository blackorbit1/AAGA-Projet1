import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
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

        Button button = new Button("Calculer Steiner");
        button.setTranslateY(-(height/2 - 50));
        button.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                ArrayList<Point> hitPoints = (new CDS()).calculDominatingSet(points, edgeThreshold);
                System.out.println(hitPoints);

                LinkedList<Steiner.Arete> steiner = new Steiner().calculSteiner(points, edgeThreshold, hitPoints);
                System.out.println(steiner);

                draw_steiner(steiner);
            }
        });

        pane.getChildren().add(button);







        Scene scene = new Scene(pane, width, height);
        //VBox root = new VBox(sharpCanvas);
        stage.setScene(scene);
        stage.show();
    }

    public static void draw_steiner(LinkedList<Steiner.Arete> aretes){
        Stage stage = getStage();
        Canvas canvas = new Canvas(width, height);
        GraphicsContext gc = canvas.getGraphicsContext2D() ;

        gc.setStroke(Color.RED);
        gc.setLineWidth(3);

        for(Steiner.Arete arete : aretes){
            gc.strokeLine(arete.p1.x, arete.p1.y, arete.p2.x, arete.p2.y);
        }

        pane.getChildren().add(canvas);
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