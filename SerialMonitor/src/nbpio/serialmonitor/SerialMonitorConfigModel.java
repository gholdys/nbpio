package nbpio.serialmonitor;

public interface SerialMonitorConfigModel {
    
    String[] getAvailablePortNames();    
    String[] getAvailableBaudRates();
    String getDefaultBaudRate();
    String[] getAvailableFlowControl();
    String getDefaultFlowControl();
    String[] getAvailableDataBits();
    String getDefaultDataBits();
    String[] getAvailableStopBits();
    String getDefaultStopBits();
    String[] getAvailableParities();
    String getDefaultParity();
    void setCurrentPortName( String value );
    String getCurrentPortName();
    void setCurrentBaudRate( String value );
    String getCurrentBaudRate();
    void setCurrentFlowControl( String value );
    String getCurrentFlowControl();
    void setCurrentDataBits( String value );
    String getCurrentDataBits();
    void setCurrentStopBits( String value );
    String getCurrentStopBits();
    void setCurrentParity( String value );
    String getCurrentParity();
    SerialPortConfig getCurrentConfig();
    
}
