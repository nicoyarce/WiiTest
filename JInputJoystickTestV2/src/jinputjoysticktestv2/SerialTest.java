/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jinputjoysticktestv2;

import dk.thibaut.serial.SerialChannel;
import dk.thibaut.serial.SerialPort;
import dk.thibaut.serial.enums.BaudRate;
import dk.thibaut.serial.enums.DataBits;
import dk.thibaut.serial.enums.Parity;
import dk.thibaut.serial.enums.StopBits;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Nicoyarce
 */
public class SerialTest {
    
    public static void main(String[] args) throws IOException, InterruptedException {
        SerialTest s = new SerialTest();
        SerialPort port = s.iniciarSerial();
        s.enviarSerial("w", port);
    }
    
    public SerialPort iniciarSerial() throws IOException {
        // Get a list of available ports names (COM2, COM4, ...)
        List<String> portsNames = SerialPort.getAvailablePortsNames();        
        // Get a new instance of SerialPort by opening a port.
        SerialPort port = SerialPort.open(portsNames.get(1));
        // Configure the connection
        port.setTimeout(100);
        port.setConfig(BaudRate.B9600, Parity.NONE, StopBits.ONE, DataBits.D8);
        try {
            Thread.sleep(1700);
        } catch (InterruptedException ex) {
            Logger.getLogger(JoystickTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        return port;
    }
    
    public void enviarSerial(String s, SerialPort port) throws IOException, InterruptedException{
        SerialChannel channel = port.getChannel();        
        ByteBuffer buffer;
        buffer = str_to_bb("w", Charset.forName("UTF-8"));
        int c = channel.write(buffer);               
        port.close();
    }
    
    public static ByteBuffer str_to_bb(String msg, Charset charset){
        return ByteBuffer.wrap(msg.getBytes(charset));
    }
}
