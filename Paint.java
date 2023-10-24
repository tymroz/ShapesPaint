import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;
import javafx.stage.Stage;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import java.io.*;
import java.util.ArrayList;

/**
 * @author Tymoteusz Roźmiarek
 */
public class Paint extends Application implements Serializable {
    private final transient Pane gr = new Pane();
    private transient ContextMenu contextMenu;
    private transient MenuItem changeColorItem;
    private transient Scene scene;
    public int dl;
    ArrayList<Shape> figures = new ArrayList<>();

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        root.setCenter(gr);
        HBox buttonPane = new HBox(5);
        buttonPane.setSpacing(40);
        buttonPane.setPadding(new Insets(10,30,10,30));
        Button btOkrag = new Button("Okrag");
        Button btProstokat = new Button("Prostokat");
        Button btWielokat = new Button("Wielokat");
        Button btInfo = new Button("Info");
        Button btZapisz = new Button("Zapisz");
        Button btOdtworz = new Button("Odtworz");
        Button btWyczysc = new Button("Wyczysc Panel");

        buttonPane.getChildren().addAll(btInfo, btOkrag, btProstokat, btWielokat, btZapisz, btOdtworz, btWyczysc );

        btWyczysc.setOnAction(e -> {
            gr.getChildren().clear();
        });

        btOkrag.setOnAction(e -> {
            root.setOnMouseClicked(new OkragTworzenie());
            root.setOnMouseDragged(new OkragTworzenie());
        });
        btProstokat.setOnAction(e -> {
            root.setOnMouseClicked(new ProstokatTworzenie());
            root.setOnMouseDragged(new ProstokatTworzenie());
        });
        btWielokat.setOnAction(e -> {
            root.setOnMouseClicked(new WielokatTworzenie());
            root.setOnMouseDragged(new WielokatTworzenie());
        });
        btInfo.setOnAction(e -> {
            Dialog<String> dialog = new Dialog<>();
            ButtonType type = new ButtonType("Ok", ButtonBar.ButtonData.CANCEL_CLOSE);
            dialog.setTitle("Info");
            dialog.setContentText("INSTRUKCJA UŻYTKOWANIA:\nWybieramy w Menu 'Wybór Figury' figurę,którą chcemy narysować\nRysujemy Figurę, za pomocą lewego przycisku myszy.\nOkrąg: pierwsze nacisniecie przycisku oznacza wybranie srodka okregu, drugie punktu na okręgu.\nProstokat:pierwsze nacisniecie przycisku oznacza wybranie jednego wierzchołka, drugie wierzcholka przeciwleglego.\nWielokąt: Po kliknięciu na obszarze rysowania, pojawi się kolejny wierzchołek wielokąta. Kliknięcie dwukrotnie zakończy rysowanie wielokąta.\n\nKliknięcie prawym przyciskiem myszy na figurę spowoduje wyświetlenie menu kontekstowego, w którym możesz zmienić kolor figury.\nFigury można przesuwać przytrzymując lewy klawisz myszy, skalować za pomocą scrolla oraz obracać przytrzymując klawisz Shift i klikając na figurę\n\nPaint.java\nautor: Tymoteusz Roźmiarek ");
            dialog.getDialogPane().getButtonTypes().add(type);
            dialog.showAndWait();
        });
        btZapisz.setOnAction(e -> {
            try {
                FileOutputStream fileOut = new FileOutputStream("okregi.txt");
                ObjectOutputStream out = new ObjectOutputStream(fileOut);
                FileOutputStream fileOut1 = new FileOutputStream("prostokaty.txt");
                ObjectOutputStream out1 = new ObjectOutputStream(fileOut1);
                FileOutputStream fileOut2 = new FileOutputStream("wielokaty.txt");
                ObjectOutputStream out2 = new ObjectOutputStream(fileOut2);

                for (Shape figure : figures) {
                    if (figure instanceof Okrag okrag) {
                        okrag.zapiszOkrag(out);
                    }
                    if (figure instanceof Prostokat prostokat) {
                        prostokat.zapiszProstokat(out1);
                    }
                    if (figure instanceof Wielokat wielokat) {
                        wielokat.zapiszWielokat(out2);
                    }
                }

                out.close();
                fileOut.close();
                out1.close();
                fileOut1.close();
                out2.close();
                fileOut2.close();

                //System.out.println("Zapisano figury do pliku.");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        btOdtworz.setOnAction(e -> {
            try {
                FileInputStream fis = new FileInputStream("okregi.txt");
                ObjectInputStream ois = new ObjectInputStream(fis);
                FileInputStream fis1 = new FileInputStream("prostokaty.txt");
                ObjectInputStream ois1 = new ObjectInputStream(fis1);
                FileInputStream fis2 = new FileInputStream("wielokaty.txt");
                ObjectInputStream ois2 = new ObjectInputStream(fis2);
                figures.clear();
                while (fis.available() > 0) {
                    Okrag okrag = wczytajOkrag(ois);
                    gr.getChildren().add(okrag);
                    figures.add(okrag);
                }
                while (fis1.available() > 0) {
                    Prostokat prostokat = wczytajProstokat(ois1);
                    gr.getChildren().add(prostokat);
                    figures.add(prostokat);
                }
                while (fis2.available() > 0) {
                    Wielokat wielokat = wczytajWielokat(ois2);
                    gr.getChildren().add(wielokat);
                    figures.add(wielokat);
                }

                ois.close();
                fis.close();
                ois1.close();
                fis1.close();
                ois2.close();
                fis2.close();
            } catch (IOException | ClassNotFoundException ex) {
                ex.printStackTrace();
            }
        });

        root.setTop(buttonPane);

        scene = new Scene(root, 1000, 700);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Figury geometryczne");

        primaryStage.show();
    }

    /**
     * metoda wczytujaca dane, potrzebne do stworzenia wielokata, z pliku
     * @param in
     * @return Wielokat stworzony z danych zapisanych w pliku
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public Wielokat wczytajWielokat(ObjectInputStream in) throws IOException, ClassNotFoundException {
        Object obj = in.readObject();
        if (obj instanceof Wielokat) {
            int n = in.readInt();
            double[] punkty = new double[n];
            for(int i = 0; i < n; i++){
                double w = in.readDouble();
                punkty[i] = w;
            }
            double rotacja = in.readDouble();
            String fillColor = (String) in.readObject();

            Wielokat wielokat = new Wielokat(punkty);
            wielokat.setRotate(rotacja);
            wielokat.setFill(Color.valueOf(fillColor));

            return wielokat;
        } else {
            throw new ClassNotFoundException("Nie można wczytać obiektu jako Wielokat.");
        }
    }

    /**
     * metoda wczytujaca dane, potrzebne do stworzenia prostokata, z pliku
     * @param in
     * @return Prostokat stworzony z danych zapisanych w pliku
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public Prostokat wczytajProstokat(ObjectInputStream in) throws IOException, ClassNotFoundException {
        Object obj = in.readObject();
        if (obj instanceof Prostokat) {
            double x = in.readDouble();
            double y = in.readDouble();
            double width = in.readDouble();
            double height = in.readDouble();
            double rotacja = in.readDouble();
            String fillColor = (String) in.readObject();

            Prostokat prostokat = new Prostokat(x, y, width, height);
            prostokat.setRotate(rotacja);
            prostokat.setFill(Color.valueOf(fillColor));

            return prostokat;
        } else {
            throw new ClassNotFoundException("Nie można wczytać obiektu jako Prostokat.");
        }
    }

    /**
     * metoda wczytujaca dane, potrzebne do stworzenia okregu, z pliku
     * @param in
     * @return Okrag stworzony z danych zapisanych w pliku
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public Okrag wczytajOkrag(ObjectInputStream in) throws IOException, ClassNotFoundException {
        Object obj = in.readObject();
        if (obj instanceof Okrag) {
            double centerX = in.readDouble();
            double centerY = in.readDouble();
            double radiusX = in.readDouble();
            double radiusY = in.readDouble();
            String fillColor = (String) in.readObject();

            Okrag okrąg = new Okrag(centerX, centerY, radiusX, radiusY);
            okrąg.setFill(Color.valueOf(fillColor));

            return okrąg;
        } else {
            throw new ClassNotFoundException("Nie można wczytać obiektu jako Okrąg.");
        }
    }

    /**
     * klasa odpowiadająca za tworzenie wielokata
     */
    class WielokatTworzenie implements EventHandler<MouseEvent> {
        private double x;
        private double y;
        ArrayList<Double> pol2 = new ArrayList<>();
        Wielokat polygon;
        private boolean created = false;

        @Override
        public void handle(MouseEvent event){
            if(event.getEventType()==MouseEvent.MOUSE_CLICKED && event.getClickCount() == 2 && !created){
                Double[] trasf = pol2.toArray(new Double[0]);
                double[] tab = new double[trasf.length];
                dl = trasf.length;
                for(int i = 0; i < trasf.length; i++){
                    tab[i] = trasf[i].doubleValue();
                }

                polygon = new Wielokat(tab);
                figures.add(polygon);
                gr.getChildren().add(polygon);
                created=true;
                x = event.getX();
                y = event.getY();

            }
            else if (event.getEventType()== MouseEvent.MOUSE_CLICKED){
                x = event.getX();
                y = event.getY();
                if(!created){
                    pol2.add(x);
                    pol2.add(y);
                }
            }
        }
    }

    /**
     * nowa klasa wielokata
     */
    class Wielokat extends Polygon implements Serializable {

        Double[] pointsD;
        private int rotacja = 0;

        /**
         * konstruktor klasy Wielokat
         * @param points tablica punktow, bedacych wierzcholkami wielokata
         */
        public Wielokat(double[] points) {
            super(points);
            pointsD = convertToDouble(points);
            setOnMouseClicked(new Wielokat2EventHandler());
            setOnMouseDragged(new Wielokat2EventHandler());
            setOnScroll(new WielokatScrollHandler());
        }

        private Double[] convertToDouble(double[] points){
            Double[] pointsDH = new Double[dl];
            for(int i=0; i<dl; i++){
                pointsDH[i]=points[i];
            }
            return pointsDH;
        }

        /**
         * metoda zapisujaca Wielokat do pliku
         * @param out
         * @throws IOException
         */
        public void zapiszWielokat (ObjectOutputStream out) throws IOException{
            out.writeObject(this);
            out.writeInt(this.getPoints().size());
            ObservableList <Double> punkty = this.getPoints();
            for(int i = 0; i < punkty.size(); i++){
                out.writeDouble(punkty.get(i));
            }
            out.writeDouble(this.getRotate());
            out.writeObject(this.getFill().toString());
        }

        /**
         * sprawdzamy czy najechano kursorem na wielokat
         * @param x wspolrzedna x kursora
         * @param y wspolrzedna y kursora
         * @return wartosc logiczna odpowiadajaca temu czy kursor znajduje sie na wielokacie
         */
        public boolean isHit(double x, double y) {
            return getBoundsInLocal().contains(x,y);
        }

        /**
         * przesuwanie wielokata wzdluz osi OX
         * @param x wartosc przesuniecia wzdluz osi OX
         */
        public void addX(double x) {
            for(int i=0; i<dl; i=i+2){
                pointsD[i]=pointsD[i]+x;
            }
        }

        /**
         * przesuwanie wielokata wzdluz osi OY
         * @param y wartosc przesuniecia wzdluz osi OY
         */
        public void addY(double y) {
            for(int i=1; i<dl; i=i+2){
                pointsD[i]=pointsD[i]+y;
            }
        }

        /**
         * skalowanie szerokosci wielokata
         * @param w wartosc skalowania wzdloz OX
         */
        public void addWidth(double w) {
            setScaleX(getScaleX()+w);
        }

        /**
         * skalowanie wysokosci wielokata
         * @param h wartosc skalowania wzdluz OY
         */
        public void addHeight(double h) {
            setScaleY(getScaleY()+h);
        }

        class Wielokat2EventHandler implements EventHandler<MouseEvent>{

            Wielokat polygon;
            private double x;
            private double y;

            private void doMove(MouseEvent event) {

                double dx = event.getX() - x;
                double dy = event.getY() - y;

                if (polygon.isHit(x, y)) {
                    addX(dx);
                    addY(dy);
                    polygon.getPoints().setAll(pointsD);
                }
                x += dx;
                y += dy;
            }

            private void zmienKolor(MouseEvent event){
                contextMenu = new ContextMenu();
                changeColorItem = new MenuItem("Zmień kolor");
                contextMenu.getItems().add(changeColorItem);
                if (polygon.isHit(x, y)) {
                    changeColorItem.setOnAction(e -> {
                        ColorPicker colorPicker = new ColorPicker();
                        colorPicker.setOnAction(colorEvent -> {
                            Color selectedColor = colorPicker.getValue();
                            polygon.setFill(selectedColor);
                        });

                        Dialog<String> dialog = new Dialog<String>();
                        dialog.setTitle("Wybierz kolor");
                        dialog.getDialogPane().setContent(colorPicker);
                        ButtonType type = new ButtonType("Ok", ButtonBar.ButtonData.CANCEL_CLOSE);
                        dialog.getDialogPane().getButtonTypes().add(type);
                        dialog.showAndWait();
                    });
                    scene.addEventHandler(ContextMenuEvent.CONTEXT_MENU_REQUESTED, e -> {
                        contextMenu.show(scene.getWindow(), event.getScreenX(), event.getScreenY());
                    });
                }
            }

            @Override
            public void handle(MouseEvent event) {

                polygon = (Wielokat) event.getSource();
                if (event.getEventType()==MouseEvent.MOUSE_CLICKED){
                    x = event.getX();
                    y = event.getY();
                }
                if(event.getEventType() == MouseEvent.MOUSE_CLICKED && event.getButton() == MouseButton.SECONDARY){
                    zmienKolor(event);
                }
                if(event.getEventType()==MouseEvent.MOUSE_CLICKED && event.isShiftDown()){
                    rotacja = rotacja + 30;
                    polygon.setRotate(rotacja);
                }
                if (event.getEventType()==MouseEvent.MOUSE_DRAGGED){
                    doMove(event);
                }

            }
        }

        class WielokatScrollHandler implements EventHandler<ScrollEvent>{

            Wielokat polygon;

            private void doScale(ScrollEvent e) {

                double x = e.getX();
                double y = e.getY();

                if (polygon.isHit(x, y)) {
                    polygon.addWidth(e.getDeltaY()*0.01);
                    polygon.addHeight(e.getDeltaY()*0.01);
                }
            }

            @Override
            public void handle(ScrollEvent event) {

                polygon = (Wielokat) event.getSource();
                if (event.getEventType()==ScrollEvent.SCROLL){
                    doScale(event);
                }
            }
        }
    }

    /**
     * klasa odpowiadająca za tworzenie prostokata
     */
    class ProstokatTworzenie implements EventHandler<MouseEvent> {
        private double x;
        private double y;
        ArrayList<Double> pol3 = new ArrayList<>();
        Prostokat rectangle;
        private boolean created2 = false;
        private int clicks2 = 0;
        @Override
        public void handle(MouseEvent event){
            if(clicks2 == 1){
                Double[] trasf = pol3.toArray(new Double[0]);
                x = event.getX();
                y = event.getY();
                rectangle = new Prostokat(trasf[0], trasf[1], x-trasf[0], y-trasf[1]);
                figures.add(rectangle);
                gr.getChildren().add(rectangle);
                created2=true;
                clicks2++;
            }
            else if (event.getEventType()== MouseEvent.MOUSE_CLICKED){
                x = event.getX();
                y = event.getY();
                if(!created2){
                    pol3.add(x);
                    pol3.add(y);
                    clicks2++;
                }
            }
        }
    }

    /**
     * nowa klasa prostokata
     */
    class Prostokat extends Rectangle implements Serializable {

        private int rotacja = 0;

        /**
         * konstruktor klasy Prostokat
         * @param x wspolrzedna x poczatkowego wierzcholka prostokata
         * @param y wspolrzedna y poczatkowego wierzcholka prostokata
         * @param width szerokosc prostokata
         * @param height wysokosc prostokata
         */
        public Prostokat(double x, double y, double width, double height) {
            super(x, y, width, height);
            setOnMouseClicked(new Prostokat2EventHandler());
            setOnMouseDragged(new Prostokat2EventHandler());
            setOnScroll(new Prostokat2ScrollHandler());
        }

        /**
         * metoda zapisujaca Prostokat do pliku
         * @param out
         * @throws IOException
         */
        public void zapiszProstokat (ObjectOutputStream out) throws IOException{
            out.writeObject(this);
            out.writeDouble(this.getX());
            out.writeDouble(this.getY());
            out.writeDouble(this.getWidth());
            out.writeDouble(this.getHeight());
            out.writeDouble(this.getRotate());
            out.writeObject(this.getFill().toString());
        }

        /**
         * sprawdzanie czy najechano kursorem na prostokat
         * @param x wspolrzedna x kursora
         * @param y wspolrzedna y kursora
         * @return wartosc logiczna odpowiadajaca temu czy kursor znajduje sie na prostokacie
         */
        public boolean isHit(double x, double y) {
            return getBoundsInLocal().contains(x, y);
        }

        /**
         * przesuwanie prostokata wzdluz osi OX
         * @param x przesuniecie wzdluz osi OX
         */
        public void addX(double x) {
            setX(getX() + x);
        }

        /**
         * przesuwanie prostokata wzdluz osi OY
         * @param y przesuniecie wzdluz osi OY
         */
        public void addY(double y) {
            setY(getY() + y);
        }

        /**
         * zwiekszanie szerokosci prostokata
         * @param w wartosc zmiany szerokosci
         */
        public void addWidth(double w) {
            setWidth(getWidth() + w);
        }

        /**
         * zwiekszanie wysokosci prostokata
         * @param h wartosc zmiany wysokosci
         */
        public void addHeight(double h) {
            setHeight(getHeight() + h);
        }

        class Prostokat2EventHandler implements EventHandler<MouseEvent> {

            Prostokat rectangle;
            private double x;
            private double y;
            private void doMove(MouseEvent event) {

                double dx = event.getX() - x;
                double dy = event.getY() - y;

                if (rectangle.isHit(x, y)) {
                    rectangle.addX(dx);
                    rectangle.addY(dy);
                }
                x += dx;
                y += dy;
            }

            private void zmienKolor(MouseEvent event){
                contextMenu = new ContextMenu();
                changeColorItem = new MenuItem("Zmień kolor");
                contextMenu.getItems().add(changeColorItem);
                if (rectangle.isHit(x, y)) {
                    changeColorItem.setOnAction(e -> {
                        ColorPicker colorPicker = new ColorPicker();
                        colorPicker.setOnAction(colorEvent -> {
                            Color selectedColor = colorPicker.getValue();
                            rectangle.setFill(selectedColor);
                        });

                        Dialog<String> dialog = new Dialog<String>();
                        dialog.setTitle("Wybierz kolor");
                        dialog.getDialogPane().setContent(colorPicker);
                        ButtonType type = new ButtonType("Ok", ButtonBar.ButtonData.CANCEL_CLOSE);
                        dialog.getDialogPane().getButtonTypes().add(type);
                        dialog.showAndWait();
                    });
                    scene.addEventHandler(ContextMenuEvent.CONTEXT_MENU_REQUESTED, e -> {
                        contextMenu.show(scene.getWindow(), event.getScreenX(), event.getScreenY());
                    });
                }
            }

            @Override
            public void handle(MouseEvent event) {

                rectangle = (Prostokat) event.getSource();
                if (event.getEventType() == MouseEvent.MOUSE_CLICKED) {
                    x = event.getX();
                    y = event.getY();
                }
                if(event.getEventType() == MouseEvent.MOUSE_CLICKED && event.getButton() == MouseButton.SECONDARY){
                    zmienKolor(event);
                }
                if (event.getEventType() == MouseEvent.MOUSE_CLICKED && event.isShiftDown()) {
                    rotacja = rotacja + 30;
                    rectangle.setRotate(rotacja);
                }
                if (event.getEventType() == MouseEvent.MOUSE_DRAGGED) {
                    doMove(event);
                }

            }
        }

        class Prostokat2ScrollHandler implements EventHandler<ScrollEvent> {

            Prostokat rectangle;

            private void doScale(ScrollEvent e) {

                double x = e.getX();
                double y = e.getY();

                if (rectangle.isHit(x, y)) {
                    rectangle.addWidth(e.getDeltaY() * 0.2);
                    rectangle.addHeight(e.getDeltaY() * 0.2);
                }
            }

            @Override
            public void handle(ScrollEvent event) {

                rectangle = (Prostokat) event.getSource();
                if (event.getEventType() == ScrollEvent.SCROLL) {
                    doScale(event);
                }
            }
        }
    }

    /**
     * klasa odpowiadająca za tworzenie okregu
     */
    class OkragTworzenie implements EventHandler<MouseEvent> {
        private double x;
        private double y;
        private int clicks = 0;
        private boolean created = false;
        Okrag circle;
        ArrayList<Double> pol2 = new ArrayList<>();

        @Override
        public void handle(MouseEvent event) {
            if (clicks == 1) {
                Double[] trasf = pol2.toArray(new Double[0]);
                x = event.getX();
                y = event.getY();
                circle = new Okrag(trasf[0], trasf[1], Math.sqrt((trasf[0] - x) * (trasf[0] - x) + (trasf[1] - y) * (trasf[1] - y)), Math.sqrt((trasf[0] - x) * (trasf[0] - x) + (trasf[1] - y) * (trasf[1] - y)));
                figures.add(circle);
                gr.getChildren().add(circle);
                created = true;
                clicks++;
            } else if (event.getEventType() == MouseEvent.MOUSE_CLICKED) {
                x = event.getX();
                y = event.getY();
                if (!created) {
                    pol2.add(x);
                    pol2.add(y);
                    clicks++;
                }
            }
        }
    }

    /**
     * nowa klasa okregu
     */
    class Okrag extends Ellipse implements Serializable {

        /**
         * konstruktor klasy Okrag
         * @param x wspolrzedna x srodka okregu
         * @param y wspolrzedna y srodka okregu
         * @param width dlugosc promienia
         * @param height dlugosc promienia
         */
            public Okrag(double x, double y, double width, double height) {
                super(x, y, width, height);
                setOnMouseClicked(new Okrag2EventHandler());
                setOnMouseDragged(new Okrag2EventHandler());
                setOnScroll(new OkragScrollHandler());
            }

        /**
         * sprawdzamy czy najechano kursorem na okrag
         * @param x wspolrzedna x kursora
         * @param y wspolrzedna y kursora
         * @return wartosc logiczna odpowiadajaca temu czy kursor znajduje sie na okregu
         */
            public boolean isHit(double x, double y) {
                return getBoundsInLocal().contains(x, y);

            }

        /**
         * przesuwanie wielokata wzdluz osi OX
         * @param x przesuniecie wzdluz osi OX
         */
            public void addX(double x) {
                this.setCenterX(this.getCenterX() + x);
            }

        /**
         * przesuwanie wielokata wzdluz osi OY
         * @param y przesuniecie wzdluz osi OY
         */
            public void addY(double y) {
                this.setCenterY(this.getCenterY() + y);
            }

        /**
         * zwiekszanie promienia okregu
         * @param w zmiana dlugosci promienia
         */
        public void addWidth(double w) {
                this.setRadiusX(this.getRadiusX() + w);
            }

        /**
         * zwiekszanie promienia okregu
         * @param h zmiana dlugosci promienia
         */
            public void addHeight(double h) {
                this.setRadiusY(this.getRadiusY() + h);
            }

        /**
         * metoda zapisujaca Okrag do pliku
         * @param out
         * @throws IOException
         */
            public void zapiszOkrag (ObjectOutputStream out) throws IOException{
                out.writeObject(this);
                out.writeDouble(this.getCenterX());
                out.writeDouble(this.getCenterY());
                out.writeDouble(this.getRadiusX());
                out.writeDouble(this.getRadiusY());
                out.writeObject(this.getFill().toString());
            }

            class OkragScrollHandler implements EventHandler<ScrollEvent> {

                Okrag ellipse;

                private void doScale(ScrollEvent e) {

                    double x = e.getX();
                    double y = e.getY();

                    if (ellipse.isHit(x, y)) {
                        ellipse.addWidth(e.getDeltaY() * 0.2);
                        ellipse.addHeight(e.getDeltaY() * 0.2);
                    }
                }

                @Override
                public void handle(ScrollEvent event) {

                    ellipse = (Okrag) event.getSource();
                    if (event.getEventType() == ScrollEvent.SCROLL) {
                        doScale(event);
                    }
                }
            }

            class Okrag2EventHandler implements EventHandler<MouseEvent> {

                Okrag ellipse;
                private double x;
                private double y;

                private void doMove(MouseEvent event) {

                    double dx = event.getX() - x;
                    double dy = event.getY() - y;

                    if (ellipse.isHit(x, y)) {
                        ellipse.addX(dx);
                        ellipse.addY(dy);
                    }
                    x += dx;
                    y += dy;
                }

                private void zmienKolor(MouseEvent event){
                    contextMenu = new ContextMenu();
                    changeColorItem = new MenuItem("Zmień kolor");
                    contextMenu.getItems().add(changeColorItem);
                    if (ellipse.isHit(x, y)) {
                        changeColorItem.setOnAction(e -> {
                            ColorPicker colorPicker = new ColorPicker();
                            colorPicker.setOnAction(colorEvent -> {
                                Color selectedColor = colorPicker.getValue();
                                ellipse.setFill(selectedColor);
                            });

                            Dialog<String> dialog = new Dialog<String>();
                            dialog.setTitle("Wybierz kolor");
                            dialog.getDialogPane().setContent(colorPicker);
                            ButtonType type = new ButtonType("Ok", ButtonBar.ButtonData.CANCEL_CLOSE);
                            dialog.getDialogPane().getButtonTypes().add(type);
                            dialog.showAndWait();
                        });
                        scene.addEventHandler(ContextMenuEvent.CONTEXT_MENU_REQUESTED, e -> {
                            contextMenu.show(scene.getWindow(), event.getScreenX(), event.getScreenY());
                        });
                    }
                }

                @Override
                public void handle(MouseEvent event) {

                    ellipse = (Okrag) event.getSource();
                    if (event.getEventType() == MouseEvent.MOUSE_CLICKED) {
                        x = event.getX();
                        y = event.getY();
                    }
                    if(event.getEventType() == MouseEvent.MOUSE_CLICKED && event.getButton() == MouseButton.SECONDARY){
                        zmienKolor(event);
                    }
                    if (event.getEventType() == MouseEvent.MOUSE_DRAGGED) {
                        doMove(event);
                    }

                }
            }
        }

    public static void main(String[] args) {
        launch(args);
    }
}