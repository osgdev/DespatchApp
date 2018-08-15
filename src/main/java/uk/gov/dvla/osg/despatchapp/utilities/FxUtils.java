package uk.gov.dvla.osg.despatchapp.utilities;

import java.util.concurrent.CountDownLatch;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Paint;
import javafx.util.Duration;

/**
 * Utility Class for manipulating JavaFx nodes.
 */
public class FxUtils {
    static final Logger LOGGER = LogManager.getLogger();
    /**
     * Disable the node by setting its visibility and managed properties to false.
     * @param node the node
     */
    public static void disableNode(Node node) {
        changeNode(node, false);
    }
    
    /**
     * Enable the node by setting its visibility and managed properties to true.
     * @param node the node
     */
    public static void enableNode(Node node) {
        changeNode(node, true);
    }

    /**
     * Set the visibility and managed properties for a Node.
     * @param node the node
     * @param b the boolean
     */
    private static void changeNode(Node node, boolean b) {
        node.setVisible(b);
        node.setManaged(b);
    }

    public static void displayErrorMessage(Label label, String msg) {
        label.setTextFill(Paint.valueOf("Red"));
        displayMessage(label, msg);
    }
    
    public static void displaySuccessMessage(Label label, String msg) {
        label.setTextFill(Paint.valueOf("Green"));
        displayMessage(label, msg);
    }
    
    /**
     * Displays messages for the Print and Excel buttons. Messages are displayed for
     * 3 seconds and then disappear.
     */
    private static void displayMessage(Label label, String msg) {
        label.setText(msg);
        label.setOpacity(1);
        FadeTransition fadeTransition = new FadeTransition(Duration.seconds(3), label);
        fadeTransition.setDelay(Duration.seconds(4));
        fadeTransition.setFromValue(0.99);
        fadeTransition.setToValue(0.0);
        fadeTransition.play();
        // Move focus to read only control to remove the focus highlight from button
        label.requestFocus();
    }

    /**
     * Prevents instantiation of the class.
     */
    private FxUtils() {
        throw new UnsupportedOperationException();
    }

    /**
     * Swap controls by fading out the original and fading in the new control..
     * @param originalNode the original node
     * @param newNode the new node
     */
    public static void swapControls(Node originalNode, Node newNode) {
        disableNode(originalNode);
        enableNode(newNode);
    }
   
    public static boolean listViewIsEmpty(ListView listView) {
        return listView.getSelectionModel().getSelectedIndex() == -1;
    }

    public static boolean deleteKeyPressed(KeyEvent event) {
        return event.getCode() == KeyCode.BACK_SPACE || event.getCode() == KeyCode.DELETE;
    }
    /**
     * Runs the specified {@link Runnable} on the
     * JavaFX application thread and waits for completion.
     *
     * @param action the {@link Runnable} to run
     * @throws NullPointerException if {@code action} is {@code null}
     */
    public static void runAndWait(Runnable action) {
        if (action == null)
            throw new NullPointerException("action");

        // run synchronously on JavaFX thread
        if (Platform.isFxApplicationThread()) {
            action.run();
            return;
        }

        // queue on JavaFX thread and wait for completion
        final CountDownLatch doneLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                action.run();
            } finally {
                doneLatch.countDown();
            }
        });

        try {
            doneLatch.await();
        } catch (InterruptedException e) {
            LOGGER.fatal(e.getMessage());
        }
    }
}
