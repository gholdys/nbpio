package nbpio.project;

import java.awt.Component;
import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.execution.NativeExecutionDescriptor;
import org.netbeans.modules.nativeexecution.api.execution.NativeExecutionService;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.IOContainer;
import org.openide.windows.IOProvider;

public class NbpioWizardFinalStep implements WizardDescriptor.Panel, WizardDescriptor.ValidatingPanel, WizardDescriptor.FinishablePanel {

    
    private static final Logger LOGGER = Logger.getLogger( NbpioWizardFinalStep.class.getName() );
    private static final RequestProcessor RP = new RequestProcessor("Terminal Action RP", 100); // NOI18N
    
    private final Set<ChangeListener> listeners = new HashSet<>(1); // or can use ChangeSupport in NB 6.0
    private WizardIOContainerProvider topComponent;
    private volatile int platformIOExitCode = -1;
    
    
    public NbpioWizardFinalStep() {
        topComponent = new WizardIOContainerProvider();
        topComponent.setName(NbBundle.getMessage(NbpioWizardSetupStep.class, "Step_Run_PlatformIO"));
    }

    @Override
    public Component getComponent() {
        return topComponent;
    }

    @Override
    public HelpCtx getHelp() {
        return new HelpCtx(NbpioWizardFinalStep.class.getName());
    }

    @Override
    public boolean isValid() {
        return platformIOExitCode == 0;
    }

    @Override
    public final void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }

    @Override
    public final void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }

    protected final void fireChangeEvent() {
        Set<ChangeListener> ls;
        synchronized (listeners) {
            ls = new HashSet<>(listeners);
        }
        ChangeEvent ev = new ChangeEvent(this);
        for (ChangeListener l : ls) {
            l.stateChanged(ev);
        }
    }

    @Override
    public void readSettings(Object settings) {
        WizardDescriptor descriptor  = (WizardDescriptor) settings;
        File projectDir = (File) descriptor.getProperty("projdir");
        String board = (String) descriptor.getProperty("board");
        ExecutionEnvironment env = ExecutionEnvironmentFactory.getLocal();
        if (env != null) {
            runPlatformIO(env, projectDir, board);
        } else {
            descriptor.putProperty(WizardDescriptor.PROP_ERROR_NOTIFICATION, "Failed to run PlatformIO");
        }
    }

    @Override
    public void storeSettings(Object settings) {
        // do nothing
    }

    @Override
    public boolean isFinishPanel() {
        return true;
    }

    @Override
    public void validate() throws WizardValidationException {
        // do nothing
    }
    
    private void runPlatformIO(final ExecutionEnvironment env, final File projectDir, final String board) {
      
        final IOContainer ioContainer = IOContainer.create(topComponent);
        final IOProvider term = IOProvider.get("Terminal"); // NOI18N
        
        if (term != null) {

            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    if (SwingUtilities.isEventDispatchThread()) {
                        ioContainer.requestActive();
                    } else {
                        doWork();
                    }
                }

                private void doWork() {
//                    final HostInfo hostInfo;
//                    try {
//                        hostInfo = HostInfoUtils.getHostInfo(env);
//                        boolean isSupported = PtySupport.isSupportedFor(env);
//                        if (!isSupported) {
//                            String message;
//                            if (hostInfo.getOSFamily() == HostInfo.OSFamily.WINDOWS) {
//                                message = NbBundle.getMessage(NbpioWizardFinalStep.class, "LocalTerminalNotSupported.error.nocygwin"); // NOI18N
//                            } else {
//                                message = NbBundle.getMessage(NbpioWizardFinalStep.class, "LocalTerminalNotSupported.error"); // NOI18N
//                            }
//                            NotifyDescriptor nd = new NotifyDescriptor.Message(message, NotifyDescriptor.INFORMATION_MESSAGE);
//                            DialogDisplayer.getDefault().notify(nd);
//                            return;
//                        }
//                    } catch (IOException ex) {
//                        Exceptions.printStackTrace(ex);
//                        return;
//                    } catch (ConnectionManager.CancellationException ex) {
//                        Exceptions.printStackTrace(ex);
//                        return;
//                    }

                    try {
                        NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(env);
                        npb.addNativeProcessListener( new ChangeListener() {
                            @Override
                            public void stateChanged(ChangeEvent e) {
                                LOGGER.info("Native process: " + e);
                            }
                        });
                        
                        if (projectDir != null) {
                            if ( !projectDir.exists() ) {
                                projectDir.mkdirs();
                            }
                            npb.setWorkingDirectory(projectDir.getAbsolutePath());
                        }
                        npb.setExecutable("platformio");
                        npb.setArguments("-f", "-c", "netbeans", "init", "--ide", "netbeans", "-b", board);
                        
                        NativeExecutionDescriptor descr = new NativeExecutionDescriptor().inputOutput( term.getIO("#1", new Action[0], ioContainer) );
                        
                        NativeExecutionService es = NativeExecutionService.newService(npb, descr, "#2"); // NOI18N
                        platformIOExitCode = es.run().get();
                        
                        fireChangeEvent();
                    } catch (CancellationException | InterruptedException | ExecutionException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            };

            RP.post(runnable);

        }

    }

}
