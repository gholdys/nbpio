package nbpio.serialmonitor;

import java.awt.BorderLayout;
import java.util.Properties;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle.Messages;

@ConvertAsProperties(
    dtd = "-//nbpio.serialmonitor//SerialMonitor//EN",
    autostore = false
)
@TopComponent.Description(
    preferredID = "SerialMonitorTopComponent",
    iconBase="nbpio/serialmonitor/serialPort.png", 
    persistenceType = TopComponent.PERSISTENCE_NEVER
)
@TopComponent.Registration(mode = "output", openAtStartup = false)
@ActionID(category = "Tools", id = "nbpio.serialmonitor.SerialMonitorTopComponent")
@ActionReference(path = "Menu/Window/Tools", position = 950)
@TopComponent.OpenActionRegistration(
    displayName = "#CTL_SerialMonitorAction",
    preferredID = "SerialMonitorTopComponent"
)
@Messages({
    "CTL_SerialMonitorAction=Serial Monitor",
    "CTL_SerialMonitorTopComponent=Serial Monitor",
    "LBL_Config=Configuration",
    "HINT_SerialMonitorTopComponent=This is a SerialMonitor window"
})
public final class SerialMonitorTopComponent extends TopComponent {

    
    private static final Logger LOGGER = Logger.getLogger( SerialMonitorTopComponent.class.getName() );
    
    private SerialPortCommunicator communicator;
    private SerialMonitorConfigModel configModel;
    
    public SerialMonitorTopComponent() {        
        configModel = new DefaultSerialMonitorConfigModel();
        initComponents();
    }
    
    private void initComponents() {
        setName(Bundle.CTL_SerialMonitorTopComponent() + " - " + Bundle.LBL_Config());
        setToolTipText(Bundle.HINT_SerialMonitorTopComponent());
        setLayout( new BorderLayout() );
        add( new SerialMonitorConfigPane( configModel, (event) -> handleConnect() ));
    }    

    @Override
    public void componentOpened() {
        // ignore
    }

    @Override
    public void componentClosed() {
        if ( communicator != null ) {
            communicator.disconnect();
        }
    }

    void writeProperties(Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }
    
    
    private void handleConnect() {
        SerialPortConfig config = configModel.getCurrentConfig();
        communicator = new SerialPortCommunicator( config );
        SwingUtilities.invokeLater( () -> {
            removeAll();
            add( new SerialMonitorDisplayPane(communicator, (event) -> handleConfigure()) );
            setName( Bundle.CTL_SerialMonitorTopComponent() + " - " + communicator.getConfig().getPortName() );
            revalidate();
        });
    }

    private void handleConfigure() {
        if ( communicator != null ) {
            communicator.disconnect();
        }
        SwingUtilities.invokeLater( () -> {
            removeAll();
            add( new SerialMonitorConfigPane( configModel, (event) -> handleConnect()) );
            setName(Bundle.CTL_SerialMonitorTopComponent() + " - " + Bundle.LBL_Config());
            revalidate();
        });
    }
}
