package nbpio.project;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.LinkedHashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.templates.TemplateRegistration;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;

@TemplateRegistration(folder = "Project/PlatformIO", position = Integer.MAX_VALUE, displayName = "#nbpio_displayName", description = "NbpioDescription.html", iconBase = "nbpio/project/nbpio.png")
@Messages("nbpio_displayName=PlatformIO Project")
public class NbpioWizardIterator implements WizardDescriptor.InstantiatingIterator {

    
    private static final Logger LOGGER = Logger.getLogger( NbpioWizardIterator.class.getName() );
    
    private int index;
    private WizardDescriptor.Panel[] panels;
    private WizardDescriptor wizard;
    

    public static NbpioWizardIterator createIterator() {
        return new NbpioWizardIterator();
    }

    private NbpioWizardIterator() {
    }
    
    private WizardDescriptor.Panel[] createPanels() {
        return new WizardDescriptor.Panel[]{ 
            new NbpioWizardSetupStep(),
            new NbpioWizardFinalStep()
        };
    }

    private String[] createSteps() {
        return new String[]{
            NbBundle.getMessage(NbpioWizardIterator.class, "Step_Setup_Project"),
            NbBundle.getMessage(NbpioWizardIterator.class, "Step_Run_PlatformIO")                
        };        
    }

    @Override
    public Set<FileObject> instantiate() throws IOException {
        Set<FileObject> resultSet = new LinkedHashSet<>();
        File projectRootDir = FileUtil.normalizeFile((File) wizard.getProperty("projdir"));
        projectRootDir.mkdirs();
        
        FileObject projectRootFO = FileUtil.toFileObject(projectRootDir);
        File mainFile = PlatformIO.addSourceFileToProject( projectRootDir, getClass().getResourceAsStream("main.cpp"), "main.cpp" );
        PlatformIO.addPrivateConfigFileToProject( projectRootDir, getClass().getResourceAsStream("configurations.xml"), "configurations.xml" );
        
        resultSet.add( projectRootFO );
        resultSet.add( FileUtil.toFileObject(mainFile) );

        File parent = projectRootDir.getParentFile();
        if (parent != null && parent.exists()) {
            ProjectChooser.setProjectsFolder(parent);
        }

        return resultSet;
    }

    @Override
    public void initialize(WizardDescriptor wiz) {
        this.wizard = wiz;
        index = 0;
        panels = createPanels();
        // Make sure list of steps is accurate.
        String[] steps = createSteps();
        for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent();
            if (steps[i] == null) {
                // Default step name to component name of panel.
                // Mainly useful for getting the name of the target
                // chooser to appear in the list of steps.
                steps[i] = c.getName();
            }
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                jc.putClientProperty( WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, new Integer(i));
                jc.putClientProperty( WizardDescriptor.PROP_CONTENT_DATA, steps);
            }
        }
    }

    @Override
    public void uninitialize(WizardDescriptor wiz) {
        this.wizard.putProperty("projdir", null);
        this.wizard.putProperty("name", null);
        this.wizard = null;
        panels = null;
    }

    @Override
    public String name() {
        return MessageFormat.format("{0} of {1}", new Object[]{new Integer(index + 1), new Integer(panels.length)});
    }

    @Override
    public boolean hasNext() {
        return index < panels.length - 1;
    }

    @Override
    public boolean hasPrevious() {
        return index > 0;
    }

    @Override
    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
    }

    @Override
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }

    @Override
    public WizardDescriptor.Panel current() {
        return panels[index];
    }

    // If nothing unusual changes in the middle of the wizard, simply:
    @Override
    public final void addChangeListener(ChangeListener l) {
    }

    @Override
    public final void removeChangeListener(ChangeListener l) {
    }

}
