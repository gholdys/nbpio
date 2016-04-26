package nbpio.project;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;
import org.netbeans.api.project.Project;
import org.openide.util.Exceptions;

public class LibraryManagerPane extends JPanel {

    
    private static final String[] COLUMN_NAMES = {"Name", "Description"};
    
    private Project project;
    private JTextField searchField;
    private JButton searchButton;
    private JTable searchResultsTable;
    
    
    public LibraryManagerPane( Project project ) {
        this.project = project;
        setLayout( new BorderLayout(0,5) );
        setBorder( BorderFactory.createEmptyBorder(5, 5, 5, 5) );
        add( createTopPane(), BorderLayout.NORTH );
        add( createCenterPane(), BorderLayout.CENTER );
    }
    
    private JComponent createTopPane() {
        searchField = new JTextField();
        searchField.addActionListener( this::onSearchCommand );
        
        searchButton = new JButton("Search");
        searchButton.addActionListener( this::onSearchCommand );
        
        JPanel ret = new JPanel( new BorderLayout(5,0) );
        ret.add( new JLabel("Search for:"), BorderLayout.WEST );
        ret.add( searchField, BorderLayout.CENTER );
        ret.add( searchButton, BorderLayout.EAST );
        
        return ret;
    }
    
    private JComponent createCenterPane() {
        searchResultsTable = new JTable( new SearchResultsTableModel() );
        searchResultsTable.setFillsViewportHeight(true);
        
        JScrollPane scrollPane = new JScrollPane(searchResultsTable);
        return scrollPane;
    }
    
    private void onSearchCommand( ActionEvent event ) {
        String searchTerm = searchField.getText();
        try {
            List<LibraryDefinition> libraries = PlatformIO.findMatchingLibraries( searchTerm );
            searchResultsTable.setModel( new SearchResultsTableModel(libraries) );
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }    
    
    private static class SearchResultsTableModel extends AbstractTableModel {

        private final List <LibraryDefinition> libs;

        SearchResultsTableModel() {
            this.libs = new ArrayList<>();
        }
        
        SearchResultsTableModel(List<LibraryDefinition> libs) {
            this.libs = libs;
        }
        
        @Override
        public int getRowCount() {
            return libs.size();
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public String getColumnName(int column) {
            return COLUMN_NAMES[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            LibraryDefinition lib = libs.get(rowIndex);
            if ( lib != null ) {
                switch (columnIndex) {
                    case 0:
                        return lib.getName();
                    case 1:
                        return lib.getDescription();
                    default:
                        return "";
                }
            } else {
                return "";
            }
        }
            
    }
}
