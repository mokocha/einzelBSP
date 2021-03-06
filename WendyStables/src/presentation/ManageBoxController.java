package presentation;

import entity.Box;
import entity.Reservation;
import exception.BoxException;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBoxBuilder;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.log4j.Logger;
import service.BoxService;
import service.BoxServiceImpl;
import service.ReservationService;
import service.ReservationServiceImpl;

import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.ResourceBundle;

public class ManageBoxController implements Initializable {

    private static final Logger logger = Logger.getLogger(ManageBoxController.class);

    @FXML
    private Label image_loaded;
    private String filename = "";
    private String directory = "img/";
    private Image img;
    @FXML
    private javafx.scene.image.ImageView image_field;
    @FXML
    private Box clicked;
    @FXML
    private TableView<Box> tabulka;
    @FXML
    private TextField tf_daily_rate;
    @FXML
    private TextField tf_size;
    @FXML
    private CheckBox ch_window;
    @FXML
    private CheckBox ch_outside;
    @FXML
    private Button b_filter;
    @FXML
    private Button b_create;
    @FXML
    private Button b_update;
    @FXML
    private Button b_delete;
    @FXML
    private Button b_clearall;
    @FXML
    private ComboBox<String> cb_floor;
    private String floor_type;
    @FXML
    private Label e_daily_int;
    @FXML
    private Label e_daily_positive;
    @FXML
    private Label e_size_int;
    @FXML
    private Label e_size_positive;
    @FXML
    private Label e_floor_choose;
    @FXML
    private Label e_window_choose;
    @FXML
    private Label e_outside_choose;
    @FXML
    private Label f_box_failed;
    @FXML
    private Label f_box_created;
    @FXML
    private Label f_box_updated;
    @FXML
    private Label f_box_deleted;
    @FXML
    private Label f_filter_failed;
    @FXML
    private Label f_filter_applied;
    @FXML
    private Label f_filter_criteria;

    @FXML
    public void removeSuccessMessage() {
        f_box_created.setVisible(false);
        f_box_updated.setVisible(false);
        f_box_deleted.setVisible(false);
        f_box_failed.setVisible(false);
        f_filter_criteria.setVisible(false);
        f_filter_applied.setVisible(false);
        f_filter_failed.setVisible(false);
    }

    public void clearPrompts() {
        image_loaded.setVisible(false);
        e_daily_int.setVisible(false);
        e_daily_positive.setVisible(false);
        e_size_int.setVisible(false);
        e_size_positive.setVisible(false);
        e_floor_choose.setVisible(false);
        e_window_choose.setVisible(false);
        e_outside_choose.setVisible(false);
    }

    @FXML
    public void onActionHomePage() {
        MainController.setWindow("Welcome.fxml");
    }

    @FXML
    public void onActionCreateBox() {
        removeSuccessMessage();
        clearPrompts();

        logger.info("OnActionCreateBox clicked");

        Box b = new Box();
        String exception = "";

        try {
            b.setDailyRate(Integer.parseInt(tf_daily_rate.getText()));
            if(Integer.signum(Integer.parseInt(tf_daily_rate.getText())) == -1) e_daily_positive.setVisible(true);
        } catch (Exception e) {
            exception += "0";
        }

        try {
            b.setSize(Integer.parseInt(tf_size.getText()));
            if(Integer.signum(Integer.parseInt(tf_size.getText())) == -1) e_size_positive.setVisible(true);
        } catch (Exception e) {
            exception += "1";
        }

        if(floor_type == null || floor_type.isEmpty() || floor_type.contains("any")) exception += "2";
        else b.setFloor(floor_type);

        if(!ch_window.isSelected() && !ch_outside.isSelected()) {
            exception += "34";
        } else {
            if(ch_window.isSelected()) {
                b.setWindow(true);
                b.setOutside(false);
            }
            if(ch_outside.isSelected()) {
                b.setWindow(false);
                b.setOutside(true);
            }
        }

        b.setPicURL(filename);

        if(exception.contains("0")) e_daily_int.setVisible(true);
        if(exception.contains("1")) e_size_int.setVisible(true);
        if(exception.contains("2")) e_floor_choose.setVisible(true);
        if(exception.contains("3")) e_window_choose.setVisible(true);
        if(exception.contains("4")) e_outside_choose.setVisible(true);

        if(exception.contains("3") && exception.contains("4")) {
            return;
        }

        if(!exception.isEmpty() || e_daily_positive.isVisible() || e_size_positive.isVisible()) return;

        try {
            BoxService bs = BoxServiceImpl.initialize();
            Box x = bs.create(b);
            onActionDisplayAll();
            f_box_created.setText("Stable " + x.getId() + " was created");
            f_box_created.setVisible(true);
        } catch (BoxException be) {
            f_box_failed.setVisible(true);
        }
    }

    @FXML
    public void onActionClearAll() {
        tf_daily_rate.setText("");
        tf_size.setText("");
        cb_floor.setValue("");
        ch_window.setSelected(false);
        ch_window.setDisable(false);
        ch_outside.setSelected(false);
        ch_outside.setDisable(false);

        removeSuccessMessage();
        clearPrompts();

        b_update.setDisable(true);
        b_delete.setDisable(true);
        filename = "";
    }

    @FXML
    public void selectedWindow() {
        if(ch_outside.isDisabled()) ch_outside.setDisable(false);
        else ch_outside.setDisable(true);
    }

    @FXML
    public void selectedOutside() {
        if(ch_window.isDisabled()) ch_window.setDisable(false);
        else ch_window.setDisable(true);

    }

    @FXML
    public void onActionFilterTable() {
        removeSuccessMessage();
        clearPrompts();

        Box b = new Box();
        String exception = "";

        ObservableList<Box> olist = null;

        if(!tf_daily_rate.getText().isEmpty()) { //if NOT EMPTY
            try {
                if(Integer.signum(Integer.parseInt(tf_daily_rate.getText())) == -1) { //if -INT
                    e_daily_positive.setVisible(true);
                    exception += "0";
                } else {
                    b.setDailyRate(Integer.parseInt(tf_daily_rate.getText())); //add daily rate to find criteria
                }
            } catch (NumberFormatException ne) { //if STRING
                    e_daily_int.setVisible(true);
                    exception += "0";
            }
        } else exception += "0"; //if EMPTY

        if(!tf_size.getText().isEmpty()) { //if field is not empty
            try {
                if(Integer.signum(Integer.parseInt(tf_size.getText())) == -1) {
                    e_size_positive.setVisible(true); //if int is negative, show error
                    exception += "1";
                } else {
                    b.setSize(Integer.parseInt(tf_size.getText())); //add size to find criteria
                }
            } catch (NumberFormatException ne) { //if string entered instead of int, show error
                e_size_int.setVisible(true);
                exception += "1";
            }
        } else exception += "1"; //if EMPTY

        try {
            if(cb_floor.getValue().isEmpty()) exception += "2"; //if no floor type, add to exception string -> decide if any criteria is selected
            else b.setFloor(floor_type);
        } catch (NullPointerException e) {
            exception += "2";
        }

        if(!ch_window.isSelected()) exception += "3";
        if(!ch_outside.isSelected()) exception += "4";

        b.setPicURL("");

        if(exception.contains("01234")) { //displaying alls
            if(clicked == null) {
                f_filter_failed.setVisible(true);
            } else {
                f_filter_criteria.setVisible(true);
            }
            //return;
        } else {
            //if neither window or outside is selected, ignore criterium
            if(exception.contains("3") && exception.contains("4")) {

            } else {
                b.setWindow(ch_window.isSelected());
                b.setOutside(ch_outside.isSelected());
            }
        }

        try {
            olist = BoxServiceImpl.initialize().findBox(b);
            tabulka.setItems(olist);
            if(!f_filter_criteria.isVisible() && !f_filter_failed.isVisible()) f_filter_applied.setVisible(true);
        } catch (BoxException e) {
            logger.info("Exception refreshing table");
            e.printStackTrace();
            f_filter_failed.setVisible(true);
        }
    }

    public void onActionDisplayAll() {
        removeSuccessMessage();
        clearPrompts();

        Box b = new Box();
        ObservableList<Box> olist = null;

        try {
            olist = BoxServiceImpl.initialize().findBox(b);
            tabulka.setItems(olist);
            //f_filter_applied.setVisible(true);
        } catch (BoxException e) {
            logger.info("BoxException refreshing table");
            e.printStackTrace();
            //f_filter_failed.setVisible(true);
        }

    }

    @FXML
    public void onActionUpdateBox() {
        removeSuccessMessage();
        clearPrompts();

        Box b = clicked;

        String exception = "";

        try {
            b.setDailyRate(Integer.parseInt(tf_daily_rate.getText()));
            if(Integer.signum(Integer.parseInt(tf_daily_rate.getText())) == -1) e_daily_positive.setVisible(true);
        } catch (Exception e) {
            exception += "0";
        }

        try {
            b.setSize(Integer.parseInt(tf_size.getText()));
            if(Integer.signum(Integer.parseInt(tf_size.getText())) == -1) e_size_positive.setVisible(true);
        } catch (Exception e) {
            exception += "1";
        }

        if(floor_type == null || floor_type.isEmpty() || floor_type.contains("any")) exception += "2";
        else b.setFloor(floor_type);

        if(!ch_window.isSelected() && !ch_outside.isSelected()) {
            exception += "34";
        } else {
            if(ch_window.isSelected()) {
                b.setWindow(true);
                b.setOutside(false);
            }
            if(ch_outside.isSelected()) {
                b.setWindow(false);
                b.setOutside(true);
            }
        }

        if(!filename.trim().isEmpty() && filename != null) b.setPicURL(filename);
        else b.setPicURL("");

        if(exception.contains("0")) e_daily_int.setVisible(true);
        if(exception.contains("1")) e_size_int.setVisible(true);
        if(exception.contains("2")) e_floor_choose.setVisible(true);
        if(exception.contains("3")) e_window_choose.setVisible(true);
        if(exception.contains("4")) e_outside_choose.setVisible(true);

        if(exception.contains("3") && exception.contains("4")) {
            return;
        }

        if(!exception.isEmpty() || e_daily_positive.isVisible() || e_size_positive.isVisible()) return;

        try {
            BoxService bs = BoxServiceImpl.initialize();
            bs.update(b);
            onActionDisplayAll();
            f_box_updated.setVisible(true);
        } catch (BoxException be) {
            f_box_failed.setVisible(true);
        }

    }

    public boolean boxReserved(Box box) {

        Reservation r = new Reservation();
        r.setBoxID(box.getId());
        ReservationService res = new ReservationServiceImpl().initialize();

        return res.findActiveBox(r);
    }

    @FXML
    public void onActionDeleteBox() {
        logger.info("onActionDeleteBox clicked");
        removeSuccessMessage();
        clearPrompts();

        Box b = clicked;

        if(!boxReserved(b)) {
            popUp(b);

            onActionDisplayAll();
        } else {
            final Stage dialogStage = new Stage();

            Button ok = new Button("Ok");
            ok.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    dialogStage.close();
                }
            });

            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.setScene(new Scene(VBoxBuilder.create().
                    children(new Text("Box is reserved and cannot be deleted\n\n"), ok).
                    alignment(Pos.CENTER).padding(new Insets(25)).build()));
            dialogStage.showAndWait();

        }


//        onActionFilterTable();
//        f_filter_applied.setVisible(false);
    }

    public void popUp(Box b) {
        final Box box = b;
        final Stage popUp = new Stage();

        Button delete = new Button("Delete");
        delete.setLayoutX(30);
        delete.setLayoutY(150);
        delete.setCancelButton(true);
        delete.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                final Stage dialogStage = new Stage();

                Button ok = new Button("Ok");
                ok.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        dialogStage.close();
                    }
                });

                dialogStage.initModality(Modality.WINDOW_MODAL);
                dialogStage.setScene(new Scene(VBoxBuilder.create().
                        children(new Text("Box was deleted\n\n"), ok).
                        alignment(Pos.CENTER).padding(new Insets(25)).build()));
                dialogStage.showAndWait();

                // delete box
                BoxService bs = BoxServiceImpl.initialize();

                try {
                    bs.delete(box);
                } catch (BoxException be) {
                    logger.info("boxDAO exception caught");
                    be.printStackTrace();
                }
//              f_box_deleted.setVisible(true);

                popUp.close();
            }
        });

        Button cancel = new Button("Cancel");
        cancel.setLayoutX(300.0);
        cancel.setLayoutY(150);
        cancel.setDefaultButton(true);
        cancel.setCancelButton(true);
        cancel.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                popUp.close();
            }
        });

        Label window = new Label("Do you really want to delete this box?");
        window.setLayoutX(50.0);
        window.setLayoutY(50.0);
        window.setMaxHeight(200.0);
        window.setMaxWidth(400.0);

        AnchorPane popUpLayout = new AnchorPane();
        popUpLayout.getChildren().add(window);

        popUpLayout.getChildren().add(delete);
        popUpLayout.getChildren().add(cancel);

        Scene popUpScene = new Scene(popUpLayout, 400.0, 200);

        popUp.setTitle("Alert");
        popUp.setScene(popUpScene);

        popUp.showAndWait();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        b_filter.setDisable(false);
        b_create.setDisable(false);
        b_update.setDisable(true);
        b_delete.setDisable(true);

        cb_floor.getItems().addAll("Sawdust","Straw","any");
        cb_floor.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                if (cb_floor.getValue() != null) floor_type = cb_floor.getValue();
            }
        });

        Box b = new Box();

        BoxService bx = BoxServiceImpl.initialize();

        try {
            ObservableList<Box> ob = bx.findBox(b);
            tabulka.setItems(ob);
        } catch (BoxException be) {
            logger.info("BoxDAO find exception caught");
            be.printStackTrace();
        }
        initializeTable();

        setIMG("default.jpeg");
    }

    public void initializeTable() {
        TableColumn<Box, Integer> id = new TableColumn<Box,Integer>("#");
        id.setMinWidth(7);
        id.setCellValueFactory(new PropertyValueFactory<Box, Integer>("id"));

        TableColumn<Box, Integer> dailyrate = new TableColumn<Box,Integer>("Daily Rate");
        dailyrate.setMinWidth(90);
        dailyrate.setCellValueFactory(new PropertyValueFactory<Box, Integer>("dailyRate"));

        TableColumn<Box, String> picURL = new TableColumn<Box,String>("Picture");
        picURL.setMinWidth(90);
        picURL.setCellValueFactory(new PropertyValueFactory<Box, String>("picURL"));

        TableColumn<Box, Integer> size = new TableColumn<Box,Integer>("size");
        size.setMinWidth(55);
        size.setCellValueFactory(new PropertyValueFactory<Box, Integer>("size"));

        TableColumn<Box, String> floor = new TableColumn<Box,String>("floor");
        floor.setMinWidth(60);
        floor.setCellValueFactory(new PropertyValueFactory<Box, String>("floor"));

        TableColumn<Box, Integer> window = new TableColumn<Box,Integer>("window");
        window.setMinWidth(60);
        window.setCellValueFactory(new PropertyValueFactory<Box, Integer>("window"));

        TableColumn<Box, Integer> outside = new TableColumn<Box,Integer>("outside");
        outside.setMinWidth(60);
        outside.setCellValueFactory(new PropertyValueFactory<Box, Integer>("outside"));

        tabulka.getColumns().addAll(id,dailyrate,size,floor,window,outside,picURL);
    }

    @FXML
    public void mouseClick(MouseEvent arg0) {
        clicked = tabulka.getSelectionModel().getSelectedItem();

        if(clicked != null) {
            tf_daily_rate.setText("" + clicked.getDailyRate());
            cb_floor.setValue(clicked.getFloor());

            if(clicked.isWindow()) {
                ch_window.setSelected(true);
                ch_window.setDisable(false);
                ch_outside.setSelected(false);
                ch_outside.setDisable(true);
            }

            if(clicked.isOutside()) {
                ch_outside.setSelected(true);
                ch_outside.setDisable(false);
                ch_window.setSelected(false);
                ch_window.setDisable(true);
            }

            tf_size.setText("" + clicked.getSize());

            //logger.info(clicked.getPicURL());

            if(clicked.getPicURL() != null && !clicked.getPicURL().trim().isEmpty() && !clicked.getPicURL().equals("")) setIMG("" + clicked.getPicURL());
            else setIMG("default.jpeg");

        }

        b_filter.setDisable(false);
        b_create.setDisable(false);
        b_update.setDisable(false);
        b_delete.setDisable(false);
    }

    public void imgPicker() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load Image");
        fileChooser.getExtensionFilters().addAll(
                //new FileChooser.ExtensionFilter("All Images", "*.*"),
                new FileChooser.ExtensionFilter("JPG", "*.jpg"),
                new FileChooser.ExtensionFilter("JPEG", "*.jpeg"),
                new FileChooser.ExtensionFilter("PNG", "*.png"));

        File file = fileChooser.showOpenDialog(null);

        if(file != null) {
            String pathToImage = file.toString();

            logger.info("Image loaded : " + pathToImage);

            copyImg(pathToImage);
        }
    }


    //  http://stackoverflow.com/questions/15336312/copy-an-image-to-another-location-with-different-size
    public void copyImg(String pathToImage) {

        InputStream sourceFile = null;

        File fileToCopy = new File(pathToImage);

        try {
            sourceFile = new FileInputStream(fileToCopy);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }
//TODO change width & height & check if IMAGE
        int width=600, height=400; /* set the width and height here */
        BufferedImage inputImage = null;

        try {
            inputImage = ImageIO.read(sourceFile);
        } catch (IOException e1) {
            e1.printStackTrace();
            return;
        }

        BufferedImage outputImage=new BufferedImage(width, height,
                BufferedImage.TYPE_INT_RGB);

        Graphics2D g=outputImage.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC);

        g.clearRect(0, 0, width, height);

        g.drawImage(inputImage, 0, 0, width, height, null);

        g.dispose();

        filename = pathToImage.substring(pathToImage.lastIndexOf("/")+1);

        //File output = new File(directory + filename);
        File output = new File("src/presentation/img/" + filename);
                ImageOutputStream ios;
        try {
            ios = new FileImageOutputStream(output);
            ImageIO.write(outputImage, "jpg", ios);

            ios.close();
            logger.info(" copied to img/" +filename);
            image_loaded.setText(filename);
            image_loaded.setVisible(true);
            setIMG(filename);


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setIMG(String s) {
        img = new Image(getClass().getResourceAsStream(directory+s));
        image_field.setImage(img);
    }
}
