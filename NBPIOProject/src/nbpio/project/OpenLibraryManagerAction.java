package nbpio.project;

import static javax.swing.Action.NAME;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPanel;
import org.netbeans.api.project.Project;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.awt.DynamicMenuContent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

@ActionID(
    category = "Project",
    id = "nbpio.project.OpenLibraryManagerAction"
)
@ActionRegistration(
    iconBase = "nbpio/project/nbpio.png",
    displayName = "#OpenLibraryManagerAction.name"
)
@ActionReference(path="Projects/Actions")

public final class OpenLibraryManagerAction extends AbstractAction implements ContextAwareAction {

    public @Override void actionPerformed(ActionEvent e) {assert false;}
    
    public @Override Action createContextAwareInstance(Lookup context) {
        return new ContextAction(context);
    }
    
    private static final class ContextAction extends AbstractAction {
        
        private final Project p;
        
        public ContextAction(Lookup context) {
            p = context.lookup(Project.class);
            FileObject iniFile = p.getProjectDirectory().getFileObject("platformio.ini");
            setEnabled( iniFile != null );
            putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
            putValue(NAME, NbBundle.getMessage(OpenLibraryManagerAction.class, "OpenLibraryManagerAction.name"));
        }
        
        public @Override void actionPerformed(ActionEvent e) {
            String msg = "Project location: " + FileUtil.getFileDisplayName(p.getProjectDirectory());
            JPanel form = new LibraryManagerPane(p);
            form.setPreferredSize( new Dimension(600, 600));            
            DialogDescriptor dd = new DialogDescriptor(form, msg);
            dd.setOptions( new String[] {"Import Selected", "Cancel"} );
            Object result = DialogDisplayer.getDefault().notify(dd);
            if (result != NotifyDescriptor.OK_OPTION) {
                return;
            }
        }
    }
}