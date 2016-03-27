package nbpio.project;

import java.awt.BorderLayout;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JComponent;
import org.openide.windows.IOContainer;
import org.openide.windows.IOContainer.CallBacks;
import org.openide.windows.TopComponent;
import static org.openide.windows.TopComponent.PERSISTENCE_NEVER;

public class WizardIOContainerProvider extends TopComponent implements IOContainer.Provider {

    private JComponent ioComp;
    private CallBacks ioCb;

    public WizardIOContainerProvider() {
        setLayout(new BorderLayout());
        setDisplayName("Test");
    }

    @Override
    public int getPersistenceType() {
        return PERSISTENCE_NEVER;
    }

    public void add(JComponent comp, CallBacks cb) {
        if (ioComp != null) {
            remove(ioComp);
            if (ioCb != null) {
                ioCb.closed();
            }
        }
        ioComp = comp;
        ioCb = cb;
        add(comp);
        validate();
    }

    public JComponent getSelected() {
        return ioComp;
    }

    boolean activated;

    public boolean isActivated() {
        return activated;
    }

    @Override
    protected void componentActivated() {
        super.componentActivated();
        activated = true;
        if (ioCb != null) {
            ioCb.activated();
        }
    }

    @Override
    protected void componentDeactivated() {
        super.componentDeactivated();
        activated = false;
        if (ioCb != null) {
            ioCb.deactivated();
        }
    }

    public boolean isCloseable(JComponent comp) {
        return false;
    }

    public void remove(JComponent comp) {
        if (comp == ioComp) {
            ioComp = null;
            ioCb = null;
        }
    }

    public void select(JComponent comp) {
    }

    public void setIcon(JComponent comp, Icon icon) {
    }

    public void setTitle(JComponent comp, String name) {
    }

    public void setToolTipText(JComponent comp, String text) {
    }

    public void setToolbarActions(JComponent comp, Action[] toolbarActions) {
    }
}
