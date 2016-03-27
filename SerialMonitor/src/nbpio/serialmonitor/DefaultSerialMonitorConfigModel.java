package nbpio.serialmonitor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import purejavacomm.CommPortIdentifier;
import purejavacomm.SerialPort;

public class DefaultSerialMonitorConfigModel implements SerialMonitorConfigModel {
    
    private static final String PORT_OWNER_NAME = DefaultSerialMonitorConfigModel.class.getName();
    
    private String portName;
    private String baudRate;
    private String flowControl;
    private String dataBits;
    private String stopBits;
    private String parity;

    public DefaultSerialMonitorConfigModel() {
        // empty constructor
    }
            
    @Override
    public String[] getAvailablePortNames() {
        Enumeration e = CommPortIdentifier.getPortIdentifiers();
        
        List <String> portNames = new ArrayList<>();
        while (e.hasMoreElements()) {
            CommPortIdentifier portid = (CommPortIdentifier) e.nextElement();
            SerialPort port = null;
            try {
                port = (SerialPort) portid.open(PORT_OWNER_NAME, 10);
                portNames.add( port.getName() );
            } catch (Exception ex) {
                // ignore
            } finally {
                if ( port != null ) {
                    port.close();
                    port = null;
                }
            }
        }
        
        return portNames.toArray( new String[portNames.size()] );
    }

    @Override
    public String[] getAvailableBaudRates() {
        return new String[] {"300", "600", "1200", "2400", "4800", "9600", "14400", "19200", "28800", "38400", "57600", "115200"};
    }

    @Override
    public String[] getAvailableFlowControl() {
        return new String[] { "NONE", "RTSCTS IN", "RTSCTS OUT", "XONXOFF IN", "XONXOFF OUT" };
    }    

    @Override
    public String[] getAvailableDataBits() {
        return new String[] {"5", "6", "7", "8"};
    }

    @Override
    public String[] getAvailableStopBits() {
        return new String[] {"1", "2"};
    }

    @Override
    public String[] getAvailableParities() {
        return new String[] {"NONE", "ODD", "EVEN", "MARK", "SPACE"};
    }

    @Override
    public String getDefaultBaudRate() {
        return "9600";
    }

    @Override
    public String getDefaultFlowControl() {
        return "NONE";
    }

    @Override
    public String getDefaultDataBits() {
        return "8";
    }

    @Override
    public String getDefaultStopBits() {
        return "1";
    }

    @Override
    public String getDefaultParity() {
        return "NONE";
    }

    @Override
    public void setCurrentPortName(String value) {
        portName = value;
    }

    @Override
    public String getCurrentPortName() {
        return portName != null ? portName : getAvailablePortNames()[0];
    }

    @Override
    public void setCurrentBaudRate(String value) {
        baudRate = value;
    }

    @Override
    public String getCurrentBaudRate() {
        return baudRate != null ? baudRate : getDefaultBaudRate();
    }

    @Override
    public void setCurrentFlowControl(String value) {
        flowControl = value;
    }

    @Override
    public String getCurrentFlowControl() {        
        return flowControl != null ? flowControl : getDefaultFlowControl();
    }

    @Override
    public void setCurrentDataBits(String value) {
        dataBits = value;
    }

    @Override
    public String getCurrentDataBits() {
        return dataBits != null ? dataBits : getDefaultDataBits();
    }

    @Override
    public void setCurrentStopBits(String value) {
        stopBits = value;
    }

    @Override
    public String getCurrentStopBits() {
        return stopBits != null ? stopBits : getDefaultStopBits();
    }

    @Override
    public void setCurrentParity(String value) {
        parity = value;
    }

    @Override
    public String getCurrentParity() {
        return parity != null ? parity : getDefaultParity();
    }

    @Override
    public SerialPortConfig getCurrentConfig() {
        return new SerialPortConfig.Builder()
            .portName( getCurrentPortName() )
            .baudRate( Integer.parseInt( getCurrentBaudRate() ) )
            .flowControl( parseFlowControl( getCurrentFlowControl() ) )
            .dataBits( Integer.parseInt( getCurrentDataBits() ) )
            .stopBits( parseStopBits( getCurrentStopBits() ) )
            .parity( parseParity( getCurrentParity() ) )
            .build();
    }
    
    private int parseFlowControl( String value ) {
        int index = Arrays.asList( getAvailableFlowControl() ).indexOf( value );
        switch (index) {
            case 0:
                return SerialPort.FLOWCONTROL_NONE;
            case 1:
                return SerialPort.FLOWCONTROL_RTSCTS_IN;
            case 2:
                return SerialPort.FLOWCONTROL_RTSCTS_OUT;
            case 3:
                return SerialPort.FLOWCONTROL_XONXOFF_IN;
            case 4:
                return SerialPort.FLOWCONTROL_XONXOFF_OUT;
            default:
                throw new IllegalArgumentException("Unknown flow control value: " + value);
        }
    }
    
    private int parseStopBits( String value ) {
        int index = Arrays.asList( getAvailableStopBits()).indexOf( value );
        switch (index) {
            case 0:
                return SerialPort.STOPBITS_1;
            case 1:
                return SerialPort.STOPBITS_2;
            case 2:
                return SerialPort.STOPBITS_1_5;
            default:
                throw new IllegalArgumentException("Unknown stop bits value: " + value);
        }
    }
    
    private int parseParity( String value ) {
        return Arrays.asList( getAvailableParities() ).indexOf( value );
    }
    
}
