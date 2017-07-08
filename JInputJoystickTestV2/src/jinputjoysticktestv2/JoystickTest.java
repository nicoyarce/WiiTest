package jinputjoysticktestv2;

import dk.thibaut.serial.SerialChannel;
import dk.thibaut.serial.SerialPort;
import dk.thibaut.serial.enums.BaudRate;
import dk.thibaut.serial.enums.DataBits;
import dk.thibaut.serial.enums.Parity;
import dk.thibaut.serial.enums.StopBits;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JToggleButton;
import net.java.games.input.Component;
import net.java.games.input.Component.Identifier;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;

/**
 *
 * Joystick Test with JInput
 *
 *
 * @author TheUzo007 http://theuzo007.wordpress.com
 *
 * Created 22 Oct 2013
 *
 */
public class JoystickTest {

    SerialPort port;

    public static void main(String args[]) {
        //JInputJoystickTest jinputJoystickTest = new JInputJoystickTest();
        // Writes (into console) informations of all controllers that are found.
        //jinputJoystickTest.getAllControllersInfo();
        // In loop writes (into console) all joystick components and its current values.
        //jinputJoystickTest.pollControllerAndItsComponents(Controller.Type.STICK);
        //jinputJoystickTest.pollControllerAndItsComponents(Controller.Type.GAMEPAD);

        new JoystickTest();
    }

    final JFrameWindow window;
    private ArrayList<Controller> foundControllers;

    public JoystickTest() {
        window = new JFrameWindow();

        foundControllers = new ArrayList<>();
        searchForControllers();

        // If at least one controller was found we start showing controller data on window.
        if (!foundControllers.isEmpty()) {
            startShowingControllerData();
        } else {
            window.addControllerName("No controller found!");
        }
    }

    /**
     * Search (and save) for controllers of type Controller.Type.STICK,
     * Controller.Type.GAMEPAD, Controller.Type.WHEEL and
     * Controller.Type.FINGERSTICK.
     */
    private void searchForControllers() {
        Controller[] controllers = ControllerEnvironment.getDefaultEnvironment().getControllers();

        for (int i = 0; i < controllers.length; i++) {
            Controller controller = controllers[i];

            if (controller.getType() == Controller.Type.STICK
                    || controller.getType() == Controller.Type.GAMEPAD
                    || controller.getType() == Controller.Type.WHEEL
                    || controller.getType() == Controller.Type.FINGERSTICK) {
                // Add new controller to the list of all controllers.
                foundControllers.add(controller);

                // Add new controller to the list on the window.
                window.addControllerName(controller.getName() + " - " + controller.getType().toString() + " type");
            }
        }
    }

    /**
     * Starts showing controller data on the window.
     */
    private void startShowingControllerData() {
        try {
            port = iniciarSerial();
        } catch (IOException ex) {
            Logger.getLogger(JoystickTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        while (true) {
            // Currently selected controller.
            int selectedControllerIndex = window.getSelectedControllerName();
            Controller controller = foundControllers.get(selectedControllerIndex);

            // Pull controller for current data, and break while loop if controller is disconnected.
            if (!controller.poll()) {
                window.showControllerDisconnected();
                break;
            }

            // X axis and Y axis
            int xAxisPercentage = 0;
            int yAxisPercentage = 0;
            // JPanel for other axes.
            JPanel axesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 25, 2));
            axesPanel.setBounds(0, 0, 200, 190);

            // JPanel for controller buttons
            JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 1, 1));
            buttonsPanel.setBounds(6, 19, 246, 110);

            // Go trough all components of the controller.
            Component[] components = controller.getComponents();
            for (int i = 0; i < components.length; i++) {
                Component component = components[i];
                Identifier componentIdentifier = component.getIdentifier();

                // Buttons
                //if(component.getName().contains("Button")){ // If the language is not english, this won't work.
                if (componentIdentifier.getName().matches("^[0-9]*$")) { // If the component identifier name contains only numbers, then this is a button.
                    // Is button pressed?
                    boolean isItPressed = true;
                    if (component.getPollData() == 0.0f) {
                        isItPressed = false;
                    }

                    // Button index
                    String buttonIndex;
                    buttonIndex = component.getIdentifier().toString();
                    // Create and add new button to panel.
                    JToggleButton aToggleButton = new JToggleButton(buttonIndex, isItPressed);
                    aToggleButton.setPreferredSize(new Dimension(48, 25));
                    aToggleButton.setEnabled(false);
                    buttonsPanel.add(aToggleButton);

                    // We know that this component was button so we can skip to next component.
                    continue;
                }

                // Hat switch
                if (componentIdentifier == Component.Identifier.Axis.POV) {
                    float hatSwitchPosition = component.getPollData();
                    window.setHatSwitch(hatSwitchPosition);

                    // We know that this component was hat switch so we can skip to next component.
                    continue;
                }

                // Axes
                if (component.isAnalog()) {
                    float axisValue = component.getPollData();
                    int axisValueInPercentage = getAxisValueInPercentage(axisValue);

                    // X axis
                    if (componentIdentifier == Component.Identifier.Axis.X) {
                        xAxisPercentage = axisValueInPercentage;
                        continue; // Go to next component.
                    }
                    // Y axis
                    if (componentIdentifier == Component.Identifier.Axis.Y) {
                        yAxisPercentage = axisValueInPercentage;
                        continue; // Go to next component.
                    }

                    // Other axis
                    JLabel progressBarLabel = new JLabel(component.getName());
                    JProgressBar progressBar = new JProgressBar(0, 100);
                    progressBar.setValue(axisValueInPercentage);
                    axesPanel.add(progressBarLabel);
                    axesPanel.add(progressBar);
                }
            }

            // Now that we go trough all controller components,
            // we add butons panel to window,
            window.setControllerButtons(buttonsPanel);
            // set x and y axes,
            window.setXYAxis(xAxisPercentage, yAxisPercentage);
            // add other axes panel to window.
            window.addAxisPanel(axesPanel);

            float b1 = components[2].getPollData();
            float b2 = components[3].getPollData();
            float bHOME = components[8].getPollData();
            
            try {
                comprobarBotones(xAxisPercentage, yAxisPercentage, b1, b2, bHOME);
            } catch (IOException | InterruptedException ex) {
                Logger.getLogger(JoystickTest.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            // We have to give processor some rest.
            try {
                Thread.sleep(25);
            } catch (InterruptedException ex) {
                Logger.getLogger(JoystickTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Given value of axis in percentage. Percentages increases from left/top to
     * right/bottom. If idle (in center) returns 50, if joystick axis is pushed
     * to the left/top edge returns 0 and if it's pushed to the right/bottom
     * returns 100.
     *
     * @return value of axis in percentage.
     */
    public int getAxisValueInPercentage(float axisValue) {
        return (int) (((2 - (1 - axisValue)) * 100) / 2);
    }

    public void comprobarBotones(int x, int y, float b1, float b2, float bHOME) throws IOException, InterruptedException {
        if (b1 == 1) {
            enviarSerial("s");
            //System.out.println("envia s");
            Thread.sleep(150);
            enviarSerial("y");
        }
        if (b2 == 1) {
            enviarSerial("w");
            //System.out.println("envia w");
            Thread.sleep(150);
            enviarSerial("y");
        }        
        if (x == 100 && b2 == 1) {
            enviarSerial("d");
            //System.out.println("envia d");
            Thread.sleep(150);
            enviarSerial("y");
        }
        if (x == 0 && b2 == 1) {
            enviarSerial("a");
            //System.out.println("envia a");
            Thread.sleep(150);
            enviarSerial("y");
        }        
        if (x == 0 && b1 == 1) {
            enviarSerial("z");
            //System.out.println("envia z");
            Thread.sleep(150);
            enviarSerial("y");
        }
        if (x == 100 && b1 == 1) {
            enviarSerial("c");
            //System.out.println("envia c");
            Thread.sleep(150);
            enviarSerial("y");
        }        
        if (bHOME == 1){
            enviarSerial("y");
            //System.out.println("envia y");
        }
    }

    public SerialPort iniciarSerial() throws IOException {
        // Get a list of available ports names (COM2, COM4, ...)
        List<String> portsNames = SerialPort.getAvailablePortsNames();
        // Get a new instance of SerialPort by opening a port.
        SerialPort p = SerialPort.open(portsNames.get(1));
        try {            
            // Configure the connection
            p.setTimeout(100);
            p.setConfig(BaudRate.B115200, Parity.NONE, StopBits.ONE, DataBits.D8);
        } catch (IOException ex) {
            System.err.println(ex);
        }
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ex) {
            System.err.println(ex);
        }
        return p;
    }

    public void enviarSerial(String s) {
        try {
            SerialChannel channel = port.getChannel();
            ByteBuffer buffer = str_to_bb(s, Charset.forName("UTF-8"));
            int c = channel.write(buffer);
            //port.close();
        } catch (IOException ex) {
            System.err.println(ex);
        }
    }

    public static ByteBuffer str_to_bb(String msg, Charset charset) {
        return ByteBuffer.wrap(msg.getBytes(charset));
    }
}
