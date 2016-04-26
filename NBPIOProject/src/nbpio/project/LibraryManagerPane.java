package nbpio.project;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import org.jdesktop.swingx.JXTable;
import org.netbeans.api.project.Project;

public class LibraryManagerPane extends JPanel {

    
    private static final String[] COLUMN_NAMES = {"Name", "Description"};
    
    private Project project;
    private JTextField searchField;
    private JButton searchButton;
    private JXTable searchResultsTable;
    private JProgressBar searchProgressBar;
    private JPanel centerPane;
    
    
    public LibraryManagerPane( Project project ) {
        this.project = project;
        setLayout( new BorderLayout(0,5) );
        setBorder( BorderFactory.createEmptyBorder(5, 5, 5, 5) );
        add( createTopPane(), BorderLayout.NORTH );
        add( createCenterPane(), BorderLayout.CENTER );
        setPreferredSize( new Dimension(700, 500) );
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
        searchResultsTable = new JXTable( new SearchResultsTableModel() );
        searchResultsTable.setFillsViewportHeight(true);        
        JScrollPane p1 = new JScrollPane( searchResultsTable );
        
        searchProgressBar = new JProgressBar(0, 100);
        searchProgressBar.setStringPainted(true);        
        JPanel p2 = new JPanel();
        p2.setLayout( new BoxLayout(p2, BoxLayout.PAGE_AXIS) );
        p2.add( Box.createGlue() );
        p2.add( new JLabel("Searching...") );
        p2.add( Box.createRigidArea( new Dimension(0,10) ) );        
        p2.add( searchProgressBar );
        p2.add( Box.createGlue() );
        p2.setBorder( BorderFactory.createEmptyBorder(0, 100, 0, 100) );
        
        centerPane = new JPanel( new CardLayout() );
        centerPane.add( p1, "1" );
        centerPane.add( p2, "2" );
        
        return centerPane;
    }
    
    private void onSearchCommand( ActionEvent event ) {
        final String searchTerm = searchField.getText();
                
        ((CardLayout) centerPane.getLayout()).last( centerPane );
        
        final List <LibraryDefinition> libraries = new ArrayList<>();
        PlatformIO.startLibrarySearch(searchTerm, (r) -> {
            libraries.addAll( r.getResults() );
            
            if ( r.isComplete() ) {
                SwingUtilities.invokeLater(() -> {
                    searchResultsTable.setModel( new SearchResultsTableModel(libraries) );
                    searchResultsTable.packAll();
                    ((CardLayout) centerPane.getLayout()).first( centerPane );
                });
            } else {
                SwingUtilities.invokeLater(() -> {
                    int percentValue = Math.round(100 * r.getProgress());
                    searchProgressBar.setValue( percentValue );
                    searchProgressBar.setString( percentValue + "%" );
                });
            }
        });
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
