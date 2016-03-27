package nbpio;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;

import java.awt.Component;
import java.awt.Dimension;
import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.ListCellRenderer;
import javax.swing.SwingWorker;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

public class NbpioWizardSetupStep implements WizardDescriptor.Panel, WizardDescriptor.ValidatingPanel {

    
    public static final Logger LOGGER = Logger.getLogger( NbpioWizardSetupStep.class.getName() );    
    public static final String PROP_PROJECT_NAME = "projectName";
    
    private final Set<ChangeListener> listeners = new HashSet<>(1); // or can use ChangeSupport in NB 6.0
    
    private WizardDescriptor wizardDescriptor;
    
    private final ContentPanel contentPanel;
    private final JButton browseButton;
    private final JLabel createdFolderLabel;
    private final JTextField createdFolderTextField;
    private final JLabel projectLocationLabel;
    private final JTextField projectLocationTextField;
    private final JLabel projectNameLabel;
    private final JTextField projectNameTextField;
    private final JLabel platformLabel;
    private final JComboBox<String> platformCombo;
    private final JLabel boardLabel;
    private final JComboBox<Object> boardCombo;
    private Map<String,List<BoardDefinition>> boardLookup;
    
    public NbpioWizardSetupStep() {
        
        projectNameLabel = new JLabel();
        projectNameTextField = new JTextField();
        projectLocationLabel = new JLabel();
        projectLocationTextField = new JTextField();
        browseButton = new JButton();
        createdFolderLabel = new JLabel();
        createdFolderTextField = new JTextField();
        
        platformLabel = new JLabel();        
        platformCombo = new JComboBox<>( new String[] {"Loading..."} );
        platformCombo.setEnabled( false );
        platformCombo.setSelectedIndex(0);
        platformCombo.addItemListener( (evt) -> rebuildBoardCombo() );
        
        boardLabel = new JLabel();
        boardCombo = new JComboBox<Object>( new String[] {"Select Platform"} ) {
            @Override
            public ListCellRenderer<Object> getRenderer() {
                final ListCellRenderer r = super.getRenderer();
                if ( r != null ) {
                    return new ListCellRenderer<Object>() {
                        @Override
                        public Component getListCellRendererComponent(JList<? extends Object> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                            String text = ( value instanceof BoardDefinition ) ? ((BoardDefinition) value).getName() : value.toString();
                            return r.getListCellRendererComponent(list, text, index, isSelected, cellHasFocus);
                        }
                    };
                } else {
                    return null;
                }
            }
        };
        boardCombo.setEnabled( false );
        boardCombo.setSelectedIndex(0);
        
        Box.Filler filler = new Box.Filler(new Dimension(0, 5), new Dimension(0, 5), new Dimension(0, 5));
        
        projectNameLabel.setLabelFor( projectNameTextField );
        Mnemonics.setLocalizedText(projectNameLabel, NbBundle.getMessage(NbpioWizardSetupStep.class, "NbpioWizardSetupStep.projectNameLabel.text") ); // NOI18N

        projectLocationLabel.setLabelFor( projectLocationTextField );
        Mnemonics.setLocalizedText(projectLocationLabel, NbBundle.getMessage(NbpioWizardSetupStep.class, "NbpioWizardSetupStep.projectLocationLabel.text") ); // NOI18N

        Mnemonics.setLocalizedText(browseButton, NbBundle.getMessage(NbpioWizardSetupStep.class, "NbpioWizardSetupStep.browseButton.text") ); // NOI18N
        browseButton.addActionListener( this::browseButtonActionPerformed );

        createdFolderLabel.setLabelFor(createdFolderTextField);
        Mnemonics.setLocalizedText(createdFolderLabel, NbBundle.getMessage(NbpioWizardSetupStep.class, "NbpioWizardSetupStep.createdFolderLabel.text")); // NOI18N

        platformLabel.setLabelFor( platformCombo );
        Mnemonics.setLocalizedText(platformLabel, NbBundle.getMessage(NbpioWizardSetupStep.class, "NbpioWizardSetupStep.platformLabel.text") ); // NOI18N
        
        boardLabel.setLabelFor( boardCombo );
        Mnemonics.setLocalizedText(boardLabel, NbBundle.getMessage(NbpioWizardSetupStep.class, "NbpioWizardSetupStep.boardLabel.text") ); // NOI18N
        
        createdFolderTextField.setEditable(false);
        
        contentPanel = new ContentPanel();        
        GroupLayout layout = new GroupLayout( contentPanel );
        contentPanel.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(projectNameLabel)
                        .addComponent(platformLabel)
                        .addComponent(boardLabel)
                        .addComponent(projectLocationLabel)
                        .addComponent(createdFolderLabel)
                    )
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(projectNameTextField, GroupLayout.Alignment.TRAILING)
                        .addComponent(platformCombo, GroupLayout.Alignment.TRAILING)
                        .addComponent(boardCombo, GroupLayout.Alignment.TRAILING)
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(projectLocationTextField, PREFERRED_SIZE, DEFAULT_SIZE, DEFAULT_SIZE)
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(browseButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE))
                        .addComponent(createdFolderTextField)
                    )
                    .addContainerGap())
                .addGroup(layout.createSequentialGroup()
                    .addGap(550, 550, 550)
                    .addComponent(filler, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
                    .addContainerGap(DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(projectNameLabel)
                        .addComponent(projectNameTextField, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE))
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                        .addComponent(platformLabel)
                        .addComponent(platformCombo, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE))
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                        .addComponent(boardLabel)
                        .addComponent(boardCombo, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE))
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(projectLocationLabel)
                        .addComponent(projectLocationTextField, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
                        .addComponent(browseButton))
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(createdFolderLabel)
                        .addComponent(createdFolderTextField, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE))
                    .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                    .addComponent(filler, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE))
        );
        
        DocumentListener documentListener = new DocumentListener() {
            
            @Override
            public void changedUpdate(DocumentEvent e) {
                updateTexts(e);
                if (projectNameTextField.getDocument() == e.getDocument()) {
                    contentPanel.firePropertyChange(PROP_PROJECT_NAME, null, projectNameTextField.getText());
                }
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                updateTexts(e);
                if (projectNameTextField.getDocument() == e.getDocument()) {
                    contentPanel.firePropertyChange(PROP_PROJECT_NAME, null, projectNameTextField.getText());
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateTexts(e);
                if (projectNameTextField.getDocument() == e.getDocument()) {
                    contentPanel.firePropertyChange(PROP_PROJECT_NAME, null, projectNameTextField.getText());
                }
            }

        };
        
        projectNameTextField.getDocument().addDocumentListener(documentListener);
        projectLocationTextField.getDocument().addDocumentListener(documentListener);
        
        contentPanel.setName(NbBundle.getMessage(NbpioWizardSetupStep.class, "Step_Setup_Project"));
        
        startLoadingPlatformData();
    }

    @Override
    public Component getComponent() {        
        return contentPanel;
    }

    @Override
    public HelpCtx getHelp() {
        return new HelpCtx(NbpioWizardSetupStep.class.getName());
    }

    @Override
    public boolean isValid() {
        return valid(wizardDescriptor);
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
        wizardDescriptor = (WizardDescriptor) settings;
        
        File projectLocation = (File) wizardDescriptor.getProperty("projdir");
        if (projectLocation == null || projectLocation.getParentFile() == null || !projectLocation.getParentFile().isDirectory()) {
            projectLocation = ProjectChooser.getProjectsFolder();
        } else {
            projectLocation = projectLocation.getParentFile();
        }
        this.projectLocationTextField.setText(projectLocation.getAbsolutePath());

        String projectName = (String) wizardDescriptor.getProperty("name");
        if (projectName == null) {
            projectName = "NewProject";
        }
        this.projectNameTextField.setText(projectName);
        this.projectNameTextField.selectAll();
    }

    @Override
    public void storeSettings(Object settings) {
        WizardDescriptor d  = (WizardDescriptor) settings;
        String name = projectNameTextField.getText().trim();
        String folder = createdFolderTextField.getText().trim();
        String board = ((BoardDefinition) boardCombo.getSelectedItem()).getType();
        d.putProperty("projdir", new File(folder));
        d.putProperty("name", name);
        d.putProperty("board", board);
    }

    @Override
    public void validate() throws WizardValidationException {
        // do nothing
    }
    
    public String getProjectName() {
        return projectNameTextField.getText();
    }
    
    
    // ************************************************
    // *************** PRIVATE METHODS ****************
    // ************************************************
    private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(null);
        chooser.setDialogTitle("Select Project Location");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        String path = this.projectLocationTextField.getText();
        if (path.length() > 0) {
            File f = new File(path);
            if (f.exists()) {
                chooser.setSelectedFile(f);
            }
        }
        if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(contentPanel)) {
            File projectDir = chooser.getSelectedFile();
            projectLocationTextField.setText(FileUtil.normalizeFile(projectDir).getAbsolutePath());
        }
        fireChangeEvent();
    }

    private boolean valid(WizardDescriptor wizardDescriptor) {

        if (projectNameTextField.getText().length() == 0) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, "Project Name is not a valid folder name.");
            return false; // Display name not specified
        }
        File f = FileUtil.normalizeFile(new File(projectLocationTextField.getText()).getAbsoluteFile());
        if (!f.isDirectory()) {
            String message = "Project Folder is not a valid path.";
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, message);
            return false;
        }
        final File destFolder = FileUtil.normalizeFile(new File(createdFolderTextField.getText()).getAbsoluteFile());

        File projLoc = destFolder;
        while (projLoc != null && !projLoc.exists()) {
            projLoc = projLoc.getParentFile();
        }
        if (projLoc == null || !projLoc.canWrite()) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, "Project Folder cannot be created.");
            return false;
        }

        if (FileUtil.toFileObject(projLoc) == null) {
            String message = "Project Folder is not a valid path.";
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, message);
            return false;
        }

        File[] kids = destFolder.listFiles();
        if (destFolder.exists() && kids != null && kids.length > 0) {
            // Folder exists and is not empty
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, "Project Folder already exists and is not empty.");
            return false;
        }        
        
        Object board = boardCombo.getSelectedItem();
        if ( !(board instanceof BoardDefinition) ) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, "No board selected");
            return false;
        }
        wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, "");
        
        return true;
    }

    private void updateTexts(DocumentEvent e) {

        Document doc = e.getDocument();

        if (doc == projectNameTextField.getDocument() || doc == projectLocationTextField.getDocument()) {
            // Change in the project name

            String projectName = projectNameTextField.getText();
            String projectFolder = projectLocationTextField.getText();

            //if (projectFolder.trim().length() == 0 || projectFolder.equals(oldName)) {
            createdFolderTextField.setText(projectFolder + File.separatorChar + projectName);
            //}

        }
        fireChangeEvent(); // Notify that the panel changed
    }

    private void startLoadingPlatformData() {
        new SwingWorker<Map<String,List<BoardDefinition>>,List<BoardDefinition>>() {

            @Override
            protected Map<String, List<BoardDefinition>> doInBackground() throws Exception {
                return PlatformIO.createBoardsLookup();
            }
            
            @Override
            protected void done() {
                try {
                    boardLookup = get();
                    rebuildPlatformCombo();
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (ExecutionException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }.execute();
    }

    private void rebuildPlatformCombo() {
        String[] platformNames = boardLookup.keySet().toArray( new String[boardLookup.size()] );
        Arrays.sort( platformNames );
        platformCombo.setModel( new DefaultComboBoxModel<>(platformNames) );
        platformCombo.setEnabled(true);
        rebuildBoardCombo();
    }
    
    private void rebuildBoardCombo() {
        String selectedPlatform = platformCombo.getSelectedItem() != null ? platformCombo.getSelectedItem().toString() : null;
        
        if ( selectedPlatform != null ) {
            List<BoardDefinition> boards = boardLookup.get(selectedPlatform);
            boards.sort( ( o1, o2 ) -> o1.getName().compareTo(o2.getName()) );
            int preselectedIndex = 0;
            for ( int i=0; i<boards.size(); i++ ) {
                if ( "uno".equalsIgnoreCase( boards.get(i).getType() ) ) {
                    preselectedIndex = i;
                    break;
                }
            }
            boardCombo.setModel( new DefaultComboBoxModel<>( boards.toArray( new BoardDefinition[boards.size()]) ) );            
            boardCombo.setSelectedIndex(preselectedIndex);
            boardCombo.setEnabled( true );
        } else {
            boardCombo.setModel(new DefaultComboBoxModel<>( new String[] {NbBundle.getMessage(NbpioWizardSetupStep.class, "NbpioWizardSetupStep.boardCombo.text")} ) );
            boardCombo.setEnabled( false );
        }
        fireChangeEvent(); 
    }
    
    private final class ContentPanel extends JPanel {
        
        @Override
        public void addNotify() {
            super.addNotify();
            // Make sure that focus is on project name text field:
            projectNameTextField.requestFocus();
        }

        @Override
        public void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
            super.firePropertyChange(propertyName, oldValue, newValue);
        }
        
    }
    
}
