/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GUI;

import Threads.CheckThread;
import Entries.ClientHistory;
import Entries.ClientInformation;
import Entries.Server;
import Entries.SetValueAt;
import GUITools.LineWrapCellRenderer;
import GUITools.TableManager;
import Threads.InternetThread;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import javax.swing.text.DefaultFormatter;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.BorderFactory;
import javax.swing.SizeRequirements;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.ParagraphView;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.View;
import static javax.swing.text.View.GoodBreakWeight;
import javax.swing.text.ViewFactory;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.HTMLEditorKit.HTMLFactory;
import javax.swing.text.html.InlineView;
/**
 *
 * @author Den
 */
public class MainWindow extends javax.swing.JFrame {
    /* threads data */
    private Server server;
    public static Boolean systemStarted = false, systemPaused = false, internetPaused = false, serverPaused = false, clientPaused = false;
    private final ArrayList<CheckThread>  runningThreads = new ArrayList<>(0);
    private javax.swing.JLabel[] systemLedIndicators;
    public final static Object systemMonitor = new Object(), internetMonitor = new Object(), serverMonitor = new Object(), clientMonitor = new Object();
    
    private final Integer visitorEnterPeriod = 1; // visitor enter period
        
    private final Integer K = 20;
    private final Integer clientLeavePeriod = visitorEnterPeriod*K; // client leave site period
    
    private final Integer serverPeriod = 0; // server processing period
    
    private final ArrayList<ClientInformation> clientsInformation = new ArrayList<>();
    
    /* GUI */
    public static BlockingQueue<SetValueAt> quarantineSummaryTableBuffer = new ArrayBlockingQueue<>(1000);
    
    /**
     * Creates new form MainWindow
     */
    public MainWindow() {
        initComponents();
        
        setupEventSources();
        setupGUI();
        
        initThreads();
        setSleepTimes();
    }
    
    private void setupEventSources() {
        /* Speed Gain Spinner value change event */
        JComponent comp = this.speedGainSpinner.getEditor();
        JFormattedTextField field = (JFormattedTextField)comp.getComponent(0);
        DefaultFormatter formatter = (DefaultFormatter) field.getFormatter();
        formatter.setCommitsOnValidEdit(true);
        
        /* Server Table value change event */
//       new ServerTableListener(this.serverTable);
    }
    private void setupGUI() {
        // collect system indicator leds
        systemLedIndicators = new javax.swing.JLabel[3];
        systemLedIndicators[0] = this.systemLedIndicator0;
        systemLedIndicators[1] = this.systemLedIndicator1;
        systemLedIndicators[2] = this.systemLedIndicator2;
        
        /* setup led system pause indicators of threads */
        for ( javax.swing.JLabel systemLedIndocator : systemLedIndicators ) {
            systemLedIndocator.setText("•");
            systemLedIndocator.setForeground(Color.red);
        }
        internetLedIndicator.setText("•");
        internetLedIndicator.setForeground(Color.green);
        serverLedIndicator.setText("•");
        serverLedIndicator.setForeground(Color.green);
        clientLedIndicator.setText("•");
        clientLedIndicator.setForeground(Color.green);

        /*setup split planes */
        topHorizontalSplit.setLeftComponent(internetPane);
        topHorizontalSplit.setRightComponent(serverPane);
        bottomHorizontalSplit.setLeftComponent(clientPane);
        bottomHorizontalSplit.setRightComponent(toolPane);
        mainVerticalSplit.setDividerLocation(0.5);
        topHorizontalSplit.setDividerLocation(0.5);
        bottomHorizontalSplit.setDividerLocation(0.5);
        
        clientInformationHorizontalSplit.setDividerLocation(0.71);
        
        bufferSplitPane1.setDividerLocation((int) (this.getPreferredSize().width * 0.8));
        bufferSplitPane2.setDividerLocation((int) (this.getPreferredSize().width * 0.6));
        bufferSplitPane3.setDividerLocation((int) (this.getPreferredSize().width * 0.4));
        bufferSplitPane4.setDividerLocation((int) (this.getPreferredSize().width * 0.2));
        
        serverSplitPane.setDividerLocation(250);
        
        clientsSplitPane.setDividerLocation(0.2);
        /*setup tables */
        clientInformationTable.getColumnModel().getColumn(1).setCellRenderer(new LineWrapCellRenderer() );
        clientInformationTable.getColumnModel().getColumn(0).setMinWidth(150);
        clientInformationTable.getColumnModel().getColumn(0).setMaxWidth(150);
        
        /* set HTML TextPanes */
        clientInformationTextPane.setEditorKit(getHTMLEditorKit());
        
        clientsOnSiteTextPane.setEditorKit(getHTMLEditorKit());
        accountsAvailableTextPane.setEditorKit(getHTMLEditorKit());
        accountsOnlineTextPane.setEditorKit(getHTMLEditorKit());
        dataTransferTextPane.setEditorKit(getHTMLEditorKit());
        clientServerActivitiesTextPane.setEditorKit(getHTMLEditorKit());
        createAccountThreadTextPane.setEditorKit(getHTMLEditorKit());
        loginThreadTextPane.setEditorKit(getHTMLEditorKit());
        logoutThreadTextPane.setEditorKit(getHTMLEditorKit());
        deleteAccountThreadTextPane.setEditorKit(getHTMLEditorKit());
        leaveThreadTextPane.setEditorKit(getHTMLEditorKit());
        /* set buttons' appearence */

    }
     
    private void setSystemLedIncicators(Color color) {
        systemLedIndicator0.setForeground(color);
        clientLedIndicator.setForeground(color);
        systemLedIndicator2.setForeground(color);
    }
    
    private void initThreads() {        
        InternetThread internetThread = new InternetThread(this);
        internetThread.setBasePeriod(visitorEnterPeriod);
        
        server = new Server(this);
        server.setBasePeriod(serverPeriod);
        server.initThreads();
        internetThread.addServer(server);
        
        TableManager quarantineSummaryTableManager = new TableManager(server.getMainWindow().getQuarantineSummaryTable(), quarantineSummaryTableBuffer);
        
        runningThreads.add(internetThread);
        runningThreads.add(server);
        runningThreads.add(quarantineSummaryTableManager);
    }
    
    private void setSleepTimes() {
        runningThreads.forEach((thread)-> {
            thread.setMultiplyIndex((int)this.speedGainSpinner.getValue());
        });
    }
    
    private void startThreads() {
        runningThreads.forEach((thread) -> {
            thread.start();
        });
    }
    
    public javax.swing.JTable getInternetTable() {
        return this.internetTable;
    }
    public javax.swing.JTable getInternetAmmountTable() {
        return this.internetAmmountTable;
    }
    public Integer getClientLeavePeriod() {
        return this.clientLeavePeriod;
    }
    public ArrayList<CheckThread> getRunningThreads() {
        return this.runningThreads;
    }
    public javax.swing.JTable getServerTable() {
        return this.serverTable;
    }
    public javax.swing.JSpinner getSpeedGainSpinner() {
        return this.speedGainSpinner;
    }
    public javax.swing.JTable getServerAmmountTable() {
        return this.serverAmmountTable;
    }
    public javax.swing.JComboBox getClientComboBox() {
        return this.clientComboBox;
    }
    public Object getInternetMonitor() {
        return internetMonitor;
    }
    public javax.swing.JTable getClientInformationTable() {
        return clientInformationTable;
    }
    public javax.swing.JTextPane getClientInformationTextPane() {
        return clientInformationTextPane;
    }
    public javax.swing.JComboBox getClientInformationComboBox() {
        return this.clientInformationComboBox;
    }
    public ArrayList<ClientInformation> getClientsInformation() {
        return this.clientsInformation;
    }
    public javax.swing.JTable getClientTable() {
        return clientTable;
    }
    public javax.swing.JTable getCreateAccountTable() {
        return createAccountTable;
    }
    public javax.swing.JTable getDeleteAccountTable() {
        return deleteAccountTable;
    }
    public javax.swing.JTable getLoginTable() {
        return loginTable;
    }
    public javax.swing.JTable getLogoutTable() {
        return logoutTable;
    }
    public javax.swing.JTable getLeaveTable() {
        return leaveTable;
    }
    public javax.swing.JTextPane getServerInformationTextPane() {
        return clientServerActivitiesTextPane;
    }
    public javax.swing.JTable getServerSummaryTable() {
        return serverSummaryTable;
    }
    public javax.swing.JTable getQuarantineSummaryTable() {
        return quarantineSummaryTable;
    }
    public javax.swing.JTable getServerErrorsTable() {
        return serverErrorsTable;
    }
    public javax.swing.JTextPane getClientsOnSiteTextPane() {
        return clientsOnSiteTextPane;
    }
    public javax.swing.JTextPane getDataTransferTextPane() {
        return dataTransferTextPane;
    }
    public javax.swing.JTabbedPane getServerTabbedPane() {
        return serverTabbedPane;
    }
    public javax.swing.JTextPane getAccountsAvailableTextPane() {
        return accountsAvailableTextPane;
    }
    public javax.swing.JTextPane getAccountsOnlineTextPane() {
        return accountsOnlineTextPane;
    }
    public javax.swing.JTextPane getClientServerActivitiesTextPane() {
        return clientServerActivitiesTextPane;
    }
    public javax.swing.JTextPane getCreateAccountThreadTextPane() {
        return createAccountThreadTextPane;
    }
    public javax.swing.JTextPane getLoginThreadTextPane() {
        return loginThreadTextPane;
    }
    public javax.swing.JTextPane getLogoutThreadTextPane() {
        return logoutThreadTextPane;
    }
    public javax.swing.JTextPane getDeleteAccountThreadTextPane() {
        return deleteAccountThreadTextPane;
    }
    public javax.swing.JTextPane getLeaveThreadTextPane() {
        return leaveThreadTextPane;
    }
    public javax.swing.JComboBox getClientServerActivitiesComboBox() {
        return clientServerActivitiesComboBox;
    }
    private HTMLEditorKit getHTMLEditorKit() {
        return new HTMLEditorKit(){ 
           @Override 
           public ViewFactory getViewFactory(){ 
 
               return new HTMLFactory(){ 
                   @Override
                   public View create(Element e){ 
                      View v = super.create(e); 
                      if(v instanceof InlineView){ 
                          return new InlineView(e){ 
                              @Override
                              public int getBreakWeight(int axis, float pos, float len) { 
                                  return GoodBreakWeight; 
                              } 
                              @Override
                              public View breakView(int axis, int p0, float pos, float len) { 
                                  if(axis == View.X_AXIS) { 
                                      checkPainter(); 
                                      int p1 = getGlyphPainter().getBoundedPosition(this, p0, pos, len); 
                                      if(p0 == getStartOffset() && p1 == getEndOffset()) { 
                                          return this; 
                                      } 
                                      return createFragment(p0, p1); 
                                  } 
                                  return this; 
                                } 
                            }; 
                      } 
                      else if (v instanceof ParagraphView) { 
                          return new ParagraphView(e) { 
                              @Override
                              protected SizeRequirements calculateMinorAxisRequirements(int axis, SizeRequirements r) { 
                                  if (r == null) { 
                                        r = new SizeRequirements(); 
                                  } 
                                  float pref = layoutPool.getPreferredSpan(axis); 
                                  float min = layoutPool.getMinimumSpan(axis); 
                                  // Don't include insets, Box.getXXXSpan will include them. 
                                    r.minimum = (int)min; 
                                    r.preferred = Math.max(r.minimum, (int) pref); 
                                    r.maximum = Integer.MAX_VALUE; 
                                    r.alignment = 0.5f; 
                                  return r; 
                                } 
 
                            }; 
                        } 
                      return v; 
                    } 
                }; 
            } 
        };
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;
        bindingGroup = new org.jdesktop.beansbinding.BindingGroup();

        jPanel27 = new javax.swing.JPanel();
        jPanel26 = new javax.swing.JPanel();
        topStartSystemButton = new javax.swing.JButton();
        topStartInternetButton = new javax.swing.JButton();
        topStartServerButton = new javax.swing.JButton();
        topStartClientButton = new javax.swing.JButton();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        mainVerticalSplit = new javax.swing.JSplitPane();
        jPanel4 = new javax.swing.JPanel();
        bottomHorizontalSplit = new javax.swing.JSplitPane();
        toolPane = new javax.swing.JPanel();
        toolStartSystemButton = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        toolStartInternetButton = new javax.swing.JButton();
        toolStartClientButton = new javax.swing.JButton();
        toolStartServerButton = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        filler3 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
        speedGainLabel = new javax.swing.JLabel();
        speedGainSpinner = new javax.swing.JSpinner();
        clientPane = new javax.swing.JPanel();
        clientLedIndicator = new javax.swing.JLabel();
        systemLedIndicator1 = new javax.swing.JLabel();
        jPanel7 = new javax.swing.JPanel();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
        clientLabel = new javax.swing.JLabel();
        clientComboBox = new javax.swing.JComboBox<>();
        clientNumberLabel = new javax.swing.JLabel();
        filler2 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 32767));
        clientScroll = new javax.swing.JScrollPane();
        clientTable = new javax.swing.JTable();
        jPanel5 = new javax.swing.JPanel();
        topHorizontalSplit = new javax.swing.JSplitPane();
        serverPane = new javax.swing.JPanel();
        systemLedIndicator2 = new javax.swing.JLabel();
        serverLedIndicator = new javax.swing.JLabel();
        serverLabel = new javax.swing.JLabel();
        serverScroll = new javax.swing.JScrollPane();
        serverTable = new javax.swing.JTable();
        jScrollPane3 = new javax.swing.JScrollPane();
        serverAmmountTable = new javax.swing.JTable();
        internetPane = new javax.swing.JPanel();
        internetLedIndicator = new javax.swing.JLabel();
        systemLedIndicator0 = new javax.swing.JLabel();
        internetLabel = new javax.swing.JLabel();
        internetScroll = new javax.swing.JScrollPane();
        internetTable = new javax.swing.JTable();
        jScrollPane2 = new javax.swing.JScrollPane();
        internetAmmountTable = new javax.swing.JTable();
        jPanel3 = new javax.swing.JPanel();
        jPanel24 = new javax.swing.JPanel();
        serverSplitPane = new javax.swing.JSplitPane();
        jPanel29 = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        serverSummaryTable = new javax.swing.JTable();
        jPanel32 = new javax.swing.JPanel();
        jLabel15 = new javax.swing.JLabel();
        filler32 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
        jScrollPane13 = new javax.swing.JScrollPane();
        serverErrorsTable = new javax.swing.JTable();
        jPanel43 = new javax.swing.JPanel();
        jLabel18 = new javax.swing.JLabel();
        filler37 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
        filler38 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 32767));
        jPanel28 = new javax.swing.JPanel();
        jPanel30 = new javax.swing.JPanel();
        jLabel13 = new javax.swing.JLabel();
        filler27 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
        filler28 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
        serverTabbedPane = new javax.swing.JTabbedPane();
        clientsOnSitePanel = new javax.swing.JPanel();
        jScrollPane14 = new javax.swing.JScrollPane();
        clientsOnSiteTextPane = new javax.swing.JTextPane();
        AccountsAvailablePanel = new javax.swing.JPanel();
        jScrollPane16 = new javax.swing.JScrollPane();
        accountsAvailableTextPane = new javax.swing.JTextPane();
        accountsOnlinePanel = new javax.swing.JPanel();
        jScrollPane17 = new javax.swing.JScrollPane();
        accountsOnlineTextPane = new javax.swing.JTextPane();
        dataTransferPanel = new javax.swing.JPanel();
        jScrollPane15 = new javax.swing.JScrollPane();
        dataTransferTextPane = new javax.swing.JTextPane();
        clientServerActivityPanel = new javax.swing.JPanel();
        jPanel25 = new javax.swing.JPanel();
        filler22 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
        jLabel11 = new javax.swing.JLabel();
        filler23 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
        filler24 = new javax.swing.Box.Filler(new java.awt.Dimension(50, 0), new java.awt.Dimension(50, 0), new java.awt.Dimension(100, 32767));
        jLabel12 = new javax.swing.JLabel();
        clientServerActivitiesComboBox = new javax.swing.JComboBox<>();
        jScrollPane11 = new javax.swing.JScrollPane();
        clientServerActivitiesTextPane = new javax.swing.JTextPane();
        jPanel33 = new javax.swing.JPanel();
        jScrollPane18 = new javax.swing.JScrollPane();
        createAccountThreadTextPane = new javax.swing.JTextPane();
        jPanel34 = new javax.swing.JPanel();
        jScrollPane23 = new javax.swing.JScrollPane();
        loginThreadTextPane = new javax.swing.JTextPane();
        jPanel35 = new javax.swing.JPanel();
        jScrollPane20 = new javax.swing.JScrollPane();
        logoutThreadTextPane = new javax.swing.JTextPane();
        jPanel36 = new javax.swing.JPanel();
        jScrollPane21 = new javax.swing.JScrollPane();
        deleteAccountThreadTextPane = new javax.swing.JTextPane();
        jPanel37 = new javax.swing.JPanel();
        jScrollPane22 = new javax.swing.JScrollPane();
        leaveThreadTextPane = new javax.swing.JTextPane();
        jPanel31 = new javax.swing.JPanel();
        filler29 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
        jLabel14 = new javax.swing.JLabel();
        filler30 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
        jPanel6 = new javax.swing.JPanel();
        jPanel41 = new javax.swing.JPanel();
        clientsSplitPane = new javax.swing.JSplitPane();
        jPanel39 = new javax.swing.JPanel();
        jPanel40 = new javax.swing.JPanel();
        jLabel16 = new javax.swing.JLabel();
        filler34 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
        jScrollPane12 = new javax.swing.JScrollPane();
        quarantineSummaryTable = new javax.swing.JTable();
        filler31 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 32767));
        jPanel38 = new javax.swing.JPanel();
        jPanel8 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        clientInformationComboBox = new javax.swing.JComboBox<>();
        jLabel3 = new javax.swing.JLabel();
        filler12 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
        filler13 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
        jPanel9 = new javax.swing.JPanel();
        clientInformationHorizontalSplit = new javax.swing.JSplitPane();
        jPanel11 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        clientInformationTextPane = new javax.swing.JTextPane();
        jPanel10 = new javax.swing.JPanel();
        jScrollPane6 = new javax.swing.JScrollPane();
        clientInformationTable = new javax.swing.JTable();
        jLabel4 = new javax.swing.JLabel();
        filler10 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
        filler11 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
        jPanel42 = new javax.swing.JPanel();
        filler35 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
        jLabel17 = new javax.swing.JLabel();
        filler36 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
        jPanel12 = new javax.swing.JPanel();
        jPanel15 = new javax.swing.JPanel();
        filler18 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
        filler19 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
        jLabel9 = new javax.swing.JLabel();
        bufferSplitPane1 = new javax.swing.JSplitPane();
        bufferSplitPane2 = new javax.swing.JSplitPane();
        jPanel21 = new javax.swing.JPanel();
        jPanel17 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        filler16 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
        filler17 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
        jScrollPane9 = new javax.swing.JScrollPane();
        deleteAccountTable = new javax.swing.JTable();
        bufferSplitPane3 = new javax.swing.JSplitPane();
        bufferSplitPane4 = new javax.swing.JSplitPane();
        jPanel18 = new javax.swing.JPanel();
        jPanel14 = new javax.swing.JPanel();
        filler5 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
        jLabel5 = new javax.swing.JLabel();
        filler6 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
        jScrollPane4 = new javax.swing.JScrollPane();
        createAccountTable = new javax.swing.JTable();
        jPanel19 = new javax.swing.JPanel();
        jPanel13 = new javax.swing.JPanel();
        filler7 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
        jLabel6 = new javax.swing.JLabel();
        filler8 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
        jScrollPane7 = new javax.swing.JScrollPane();
        loginTable = new javax.swing.JTable();
        jPanel20 = new javax.swing.JPanel();
        jPanel16 = new javax.swing.JPanel();
        filler14 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
        jLabel7 = new javax.swing.JLabel();
        filler15 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
        jScrollPane8 = new javax.swing.JScrollPane();
        logoutTable = new javax.swing.JTable();
        jPanel22 = new javax.swing.JPanel();
        jScrollPane10 = new javax.swing.JScrollPane();
        leaveTable = new javax.swing.JTable();
        jPanel23 = new javax.swing.JPanel();
        filler20 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
        jLabel10 = new javax.swing.JLabel();
        filler21 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
        filler26 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
        filler25 = new javax.swing.Box.Filler(new java.awt.Dimension(20, 0), new java.awt.Dimension(20, 0), new java.awt.Dimension(20, 32767));

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Client-Server Application");
        setBackground(new java.awt.Color(245, 215, 188));
        setBounds(new Rectangle(0,0, (int)Toolkit.getDefaultToolkit().getScreenSize().getWidth(), (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight()));
        setLocation(new java.awt.Point(0, 0));
        setMaximumSize(Toolkit.getDefaultToolkit().getScreenSize());
        setMinimumSize(new java.awt.Dimension(400, 400));
        setName("mainWindiw"); // NOI18N
        setPreferredSize(new Dimension((int)Toolkit.getDefaultToolkit().getScreenSize().getWidth(), (int)Toolkit.getDefaultToolkit().getScreenSize().getHeight() - Toolkit.getDefaultToolkit().getScreenInsets(getGraphicsConfiguration()).bottom)
        );
        setSize(Toolkit.getDefaultToolkit().getScreenSize()
        );
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jPanel27.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 2, 2, 2, new java.awt.Color(42, 119, 158)));
        jPanel27.setMinimumSize(new java.awt.Dimension(300, 35));
        jPanel27.setPreferredSize(new java.awt.Dimension(4100, 25));
        jPanel27.setLayout(new java.awt.GridBagLayout());

        jPanel26.setMinimumSize(new java.awt.Dimension(290, 30));
        jPanel26.setPreferredSize(new java.awt.Dimension(4000, 17));
        jPanel26.setLayout(new java.awt.GridBagLayout());

        topStartSystemButton.setForeground(new java.awt.Color(255, 0, 0));
        topStartSystemButton.setText("System");
        topStartSystemButton.setMaximumSize(new java.awt.Dimension(67999, 23999));
        topStartSystemButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                topStartSystemButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        jPanel26.add(topStartSystemButton, gridBagConstraints);

        topStartInternetButton.setForeground(new java.awt.Color(0, 255, 0));
        topStartInternetButton.setText("Internet");
        topStartInternetButton.setMaximumSize(new java.awt.Dimension(67999, 23999));
        topStartInternetButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                topStartInternetButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        jPanel26.add(topStartInternetButton, gridBagConstraints);

        topStartServerButton.setForeground(new java.awt.Color(0, 255, 0));
        topStartServerButton.setText("Server");
        topStartServerButton.setMaximumSize(new java.awt.Dimension(67999, 23999));
        topStartServerButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                topStartServerButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        jPanel26.add(topStartServerButton, gridBagConstraints);

        topStartClientButton.setForeground(new java.awt.Color(0, 255, 0));
        topStartClientButton.setText("Client");
        topStartClientButton.setMaximumSize(new java.awt.Dimension(67999, 23999));
        topStartClientButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                topStartClientButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        jPanel26.add(topStartClientButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        jPanel27.add(jPanel26, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 2, 2);
        getContentPane().add(jPanel27, gridBagConstraints);

        jTabbedPane1.setBorder(javax.swing.BorderFactory.createMatteBorder(2, 2, 2, 2, new java.awt.Color(42, 119, 158)));

        jPanel1.setLayout(new java.awt.GridBagLayout());

        mainVerticalSplit.setDividerLocation(150);
        mainVerticalSplit.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        jPanel4.setLayout(new javax.swing.BoxLayout(jPanel4, javax.swing.BoxLayout.LINE_AXIS));

        bottomHorizontalSplit.setDividerLocation(250);

        toolPane.setBorder(javax.swing.BorderFactory.createMatteBorder(2, 2, 2, 2, new java.awt.Color(57, 57, 57)));
        toolPane.setLayout(new java.awt.GridBagLayout());

        toolStartSystemButton.setText("Start System");
        toolStartSystemButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toolStartSystemButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        toolPane.add(toolStartSystemButton, gridBagConstraints);

        jLabel1.setText("TOOLS");
        jLabel1.setPreferredSize(new java.awt.Dimension(31, 30));

        org.jdesktop.beansbinding.Binding binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, internetLabel, org.jdesktop.beansbinding.ELProperty.create("${font}"), jLabel1, org.jdesktop.beansbinding.BeanProperty.create("font"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, internetLabel, org.jdesktop.beansbinding.ELProperty.create("${horizontalAlignment}"), jLabel1, org.jdesktop.beansbinding.BeanProperty.create("horizontalAlignment"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, internetLabel, org.jdesktop.beansbinding.ELProperty.create("${minimumSize}"), jLabel1, org.jdesktop.beansbinding.BeanProperty.create("minimumSize"));
        bindingGroup.addBinding(binding);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        toolPane.add(jLabel1, gridBagConstraints);

        toolStartInternetButton.setText("Pause Internet Thread");
        toolStartInternetButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toolStartInternetButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        toolPane.add(toolStartInternetButton, gridBagConstraints);

        toolStartClientButton.setText("Pause Client Threads");
        toolStartClientButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toolStartClientButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        toolPane.add(toolStartClientButton, gridBagConstraints);

        toolStartServerButton.setText("Pause Server Threads");
        toolStartServerButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toolStartServerButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        toolPane.add(toolStartServerButton, gridBagConstraints);

        jPanel2.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        jPanel2.add(filler3, gridBagConstraints);

        speedGainLabel.setText("Speed decrease");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
        jPanel2.add(speedGainLabel, gridBagConstraints);

        speedGainSpinner.setMinimumSize(new java.awt.Dimension(100, 20));
        speedGainSpinner.setPreferredSize(new java.awt.Dimension(100, 20));
        speedGainSpinner.setValue(100);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanel2.add(speedGainSpinner, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        toolPane.add(jPanel2, gridBagConstraints);

        bottomHorizontalSplit.setLeftComponent(toolPane);

        clientPane.setBorder(javax.swing.BorderFactory.createMatteBorder(2, 2, 2, 2, new java.awt.Color(57, 57, 57)));
        clientPane.setLayout(new java.awt.GridBagLayout());

        clientLedIndicator.setMaximumSize(new java.awt.Dimension(10, 10));
        clientLedIndicator.setMinimumSize(new java.awt.Dimension(10, 10));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 40, 0, 0);
        clientPane.add(clientLedIndicator, gridBagConstraints);

        systemLedIndicator1.setMaximumSize(new java.awt.Dimension(10, 10));
        systemLedIndicator1.setMinimumSize(new java.awt.Dimension(10, 10));
        systemLedIndicator1.setPreferredSize(new java.awt.Dimension(10, 10));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 0, 0);
        clientPane.add(systemLedIndicator1, gridBagConstraints);

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, internetLabel, org.jdesktop.beansbinding.ELProperty.create("${border}"), jPanel7, org.jdesktop.beansbinding.BeanProperty.create("border"));
        bindingGroup.addBinding(binding);

        jPanel7.setLayout(new java.awt.GridBagLayout());
        jPanel7.add(filler1, new java.awt.GridBagConstraints());

        clientLabel.setText("CLIENT");

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, internetLabel, org.jdesktop.beansbinding.ELProperty.create("${font}"), clientLabel, org.jdesktop.beansbinding.BeanProperty.create("font"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, internetLabel, org.jdesktop.beansbinding.ELProperty.create("${minimumSize}"), clientLabel, org.jdesktop.beansbinding.BeanProperty.create("minimumSize"));
        bindingGroup.addBinding(binding);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        jPanel7.add(clientLabel, gridBagConstraints);

        clientComboBox.setMaximumRowCount(10);
        clientComboBox.setAutoscrolls(true);
        clientComboBox.setMinimumSize(new java.awt.Dimension(120, 30));
        clientComboBox.setPreferredSize(new java.awt.Dimension(120, 30));
        clientComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clientComboBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 10);
        jPanel7.add(clientComboBox, gridBagConstraints);

        clientNumberLabel.setText("(NUMBER)");

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, internetLabel, org.jdesktop.beansbinding.ELProperty.create("${font}"), clientNumberLabel, org.jdesktop.beansbinding.BeanProperty.create("font"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, internetLabel, org.jdesktop.beansbinding.ELProperty.create("${minimumSize}"), clientNumberLabel, org.jdesktop.beansbinding.BeanProperty.create("minimumSize"));
        bindingGroup.addBinding(binding);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE;
        jPanel7.add(clientNumberLabel, gridBagConstraints);
        jPanel7.add(filler2, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_START;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        clientPane.add(jPanel7, gridBagConstraints);

        clientScroll.setBorder(null);
        clientScroll.setEnabled(false);
        clientScroll.setFocusable(false);

        clientTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Action", "Time", "Result"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Object.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        clientTable.setEnabled(false);
        clientTable.setFocusable(false);
        clientTable.setRowSelectionAllowed(false);
        clientTable.getTableHeader().setReorderingAllowed(false);
        clientScroll.setViewportView(clientTable);
        if (clientTable.getColumnModel().getColumnCount() > 0) {
            clientTable.getColumnModel().getColumn(0).setResizable(false);
            clientTable.getColumnModel().getColumn(1).setResizable(false);
            clientTable.getColumnModel().getColumn(2).setResizable(false);
        }
        DefaultTableCellRenderer clientHeaderRenderer = (DefaultTableCellRenderer)clientTable.getTableHeader().getDefaultRenderer();
        clientHeaderRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        DefaultTableCellRenderer clientTableRenderer = new DefaultTableCellRenderer();
        clientTableRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        clientTable.getColumnModel().getColumn(1).setCellRenderer(clientTableRenderer);
        clientTable.getColumnModel().getColumn(2).setCellRenderer(clientTableRenderer);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        clientPane.add(clientScroll, gridBagConstraints);

        bottomHorizontalSplit.setRightComponent(clientPane);

        jPanel4.add(bottomHorizontalSplit);

        mainVerticalSplit.setRightComponent(jPanel4);

        jPanel5.setLayout(new javax.swing.BoxLayout(jPanel5, javax.swing.BoxLayout.LINE_AXIS));

        topHorizontalSplit.setDividerLocation(250);

        serverPane.setBorder(javax.swing.BorderFactory.createMatteBorder(2, 2, 2, 2, new java.awt.Color(57, 57, 57)));
        serverPane.setLayout(new java.awt.GridBagLayout());

        systemLedIndicator2.setMaximumSize(new java.awt.Dimension(10, 10));
        systemLedIndicator2.setMinimumSize(new java.awt.Dimension(10, 10));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 0, 0);
        serverPane.add(systemLedIndicator2, gridBagConstraints);

        serverLedIndicator.setMaximumSize(new java.awt.Dimension(10, 10));
        serverLedIndicator.setMinimumSize(new java.awt.Dimension(10, 10));
        serverLedIndicator.setPreferredSize(new java.awt.Dimension(10, 10));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 40, 0, 0);
        serverPane.add(serverLedIndicator, gridBagConstraints);

        serverLabel.setText("SERVER");
        serverLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        serverLabel.setMaximumSize(new java.awt.Dimension(52, 24));
        serverLabel.setPreferredSize(new java.awt.Dimension(52, 24));

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, internetLabel, org.jdesktop.beansbinding.ELProperty.create("${font}"), serverLabel, org.jdesktop.beansbinding.BeanProperty.create("font"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, internetLabel, org.jdesktop.beansbinding.ELProperty.create("${horizontalAlignment}"), serverLabel, org.jdesktop.beansbinding.BeanProperty.create("horizontalAlignment"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, internetLabel, org.jdesktop.beansbinding.ELProperty.create("${border}"), serverLabel, org.jdesktop.beansbinding.BeanProperty.create("border"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, internetLabel, org.jdesktop.beansbinding.ELProperty.create("${minimumSize}"), serverLabel, org.jdesktop.beansbinding.BeanProperty.create("minimumSize"));
        bindingGroup.addBinding(binding);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.ABOVE_BASELINE;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        serverPane.add(serverLabel, gridBagConstraints);

        serverScroll.setBorder(null);
        serverScroll.setEnabled(false);
        serverScroll.setFocusable(false);
        serverScroll.setMinimumSize(new java.awt.Dimension(0, 0));

        serverTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Request", "Response"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        serverTable.setEnabled(false);
        serverTable.setFocusable(false);
        serverTable.setRowSelectionAllowed(false);
        serverTable.getTableHeader().setReorderingAllowed(false);
        serverScroll.setViewportView(serverTable);
        if (serverTable.getColumnModel().getColumnCount() > 0) {
            serverTable.getColumnModel().getColumn(0).setResizable(false);
            serverTable.getColumnModel().getColumn(1).setResizable(false);
        }

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        serverPane.add(serverScroll, gridBagConstraints);

        jScrollPane3.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane3.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        jScrollPane3.setMinimumSize(new java.awt.Dimension(6, 37));

        serverAmmountTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {"Ammount of Clients on the site",  new Integer(0)},
                {"Ammount of Clients which  left the site",  new Integer(0)}
            },
            new String [] {
                "Title 1", "Title 2"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Integer.class
            };
            boolean[] canEdit = new boolean [] {
                true, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        serverAmmountTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_NEXT_COLUMN);
        serverAmmountTable.setEnabled(false);
        serverAmmountTable.setFocusable(false);
        serverAmmountTable.setGridColor(new java.awt.Color(204, 204, 204));
        serverAmmountTable.setIntercellSpacing(new java.awt.Dimension(0, 0));
        serverAmmountTable.setMaximumSize(new java.awt.Dimension(2147483647, 10000));
        serverAmmountTable.setMinimumSize(new java.awt.Dimension(30, 0));
        serverAmmountTable.setPreferredSize(new java.awt.Dimension(150, 30));
        serverAmmountTable.setRowSelectionAllowed(false);
        serverAmmountTable.getTableHeader().setReorderingAllowed(false);
        jScrollPane3.setViewportView(serverAmmountTable);
        if (serverAmmountTable.getColumnModel().getColumnCount() > 0) {
            serverAmmountTable.getColumnModel().getColumn(0).setResizable(false);
            serverAmmountTable.getColumnModel().getColumn(1).setResizable(false);
        }
        serverAmmountTable.getColumnModel().getColumn(1).setMaxWidth(75);
        serverAmmountTable.setShowHorizontalLines(true);
        serverAmmountTable.setShowVerticalLines(true);
        serverAmmountTable.setTableHeader(null);

        DefaultTableCellRenderer serverCol1 = new DefaultTableCellRenderer();
        serverCol1.setHorizontalAlignment(SwingConstants.CENTER);
        serverAmmountTable.getColumnModel().getColumn(1).setCellRenderer(serverCol1);

        DefaultTableCellRenderer serverCol0 = new DefaultTableCellRenderer();
        serverCol0.setHorizontalAlignment(SwingConstants.LEFT);
        serverAmmountTable.getColumnModel().getColumn(0).setCellRenderer(serverCol0);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        serverPane.add(jScrollPane3, gridBagConstraints);

        topHorizontalSplit.setLeftComponent(serverPane);

        internetPane.setBorder(javax.swing.BorderFactory.createMatteBorder(2, 2, 2, 2, new java.awt.Color(57, 57, 57)));
        internetPane.setLayout(new java.awt.GridBagLayout());

        internetLedIndicator.setMaximumSize(new java.awt.Dimension(10, 10));
        internetLedIndicator.setMinimumSize(new java.awt.Dimension(10, 10));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 40, 0, 0);
        internetPane.add(internetLedIndicator, gridBagConstraints);

        systemLedIndicator0.setMaximumSize(new java.awt.Dimension(10, 10));
        systemLedIndicator0.setMinimumSize(new java.awt.Dimension(10, 10));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 0, 0);
        internetPane.add(systemLedIndicator0, gridBagConstraints);

        internetLabel.setFont(new java.awt.Font("VeriBest BC", 0, 18)); // NOI18N
        internetLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        internetLabel.setText("INTERNET");
        internetLabel.setToolTipText("");
        internetLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        internetLabel.setAlignmentY(0.0F);
        internetLabel.setAutoscrolls(true);
        internetLabel.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        internetLabel.setMaximumSize(new java.awt.Dimension(65, 24));
        internetLabel.setMinimumSize(new java.awt.Dimension(45, 24));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.ABOVE_BASELINE;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        internetPane.add(internetLabel, gridBagConstraints);

        internetScroll.setBorder(null);
        internetScroll.setAlignmentX(0.0F);
        internetScroll.setAlignmentY(0.0F);
        internetScroll.setMinimumSize(new java.awt.Dimension(0, 0));

        internetTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Wanted to visit", "Entered"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        internetTable.setAlignmentX(0.0F);
        internetTable.setAlignmentY(0.0F);
        internetTable.setEnabled(false);
        internetTable.setFocusable(false);
        internetTable.setRowSelectionAllowed(false);
        internetTable.getTableHeader().setReorderingAllowed(false);
        internetScroll.setViewportView(internetTable);
        DefaultTableCellRenderer internetHeaderRenderer = (DefaultTableCellRenderer)internetTable.getTableHeader().getDefaultRenderer();
        internetHeaderRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        DefaultTableCellRenderer internetCol1 = new DefaultTableCellRenderer();
        internetCol1.setHorizontalAlignment(SwingConstants.CENTER);
        internetTable.getColumnModel().getColumn(1).setCellRenderer(internetCol1);

        DefaultTableCellRenderer internetCol0 = new DefaultTableCellRenderer();
        internetCol1.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 5));
        internetTable.getColumnModel().getColumn(1).setCellRenderer(internetCol1);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        internetPane.add(internetScroll, gridBagConstraints);

        jScrollPane2.setBorder(null);
        jScrollPane2.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane2.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        jScrollPane2.setFocusable(false);
        jScrollPane2.setMaximumSize(new java.awt.Dimension(32767, 50));
        jScrollPane2.setMinimumSize(new java.awt.Dimension(6, 48));
        jScrollPane2.setName(""); // NOI18N
        jScrollPane2.setPreferredSize(new java.awt.Dimension(6, 48));
        jScrollPane2.setWheelScrollingEnabled(false);

        internetAmmountTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {"Ammount of Visitors which tryed to enter the site",  new Integer(0)},
                {"Ammount of Visitors entered the site",  new Integer(0)},
                {"Ammount of Clients blocked",  new Integer(0)}
            },
            new String [] {
                "Title 1", "Title 2"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Integer.class
            };
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        internetAmmountTable.setAlignmentX(0.0F);
        internetAmmountTable.setAlignmentY(0.0F);
        internetAmmountTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_NEXT_COLUMN);
        internetAmmountTable.setEnabled(false);
        internetAmmountTable.setFocusable(false);
        internetAmmountTable.setGridColor(new java.awt.Color(204, 204, 204));
        internetAmmountTable.setIntercellSpacing(new java.awt.Dimension(0, 0));
        internetAmmountTable.setMaximumSize(new java.awt.Dimension(2147483647, 0));
        internetAmmountTable.setMinimumSize(new java.awt.Dimension(30, 0));
        internetAmmountTable.setName(""); // NOI18N
        internetAmmountTable.setPreferredSize(new java.awt.Dimension(150, 170));
        internetAmmountTable.setRowSelectionAllowed(false);
        internetAmmountTable.getTableHeader().setResizingAllowed(false);
        internetAmmountTable.getTableHeader().setReorderingAllowed(false);
        jScrollPane2.setViewportView(internetAmmountTable);
        if (internetAmmountTable.getColumnModel().getColumnCount() > 0) {
            internetAmmountTable.getColumnModel().getColumn(0).setResizable(false);
            internetAmmountTable.getColumnModel().getColumn(1).setResizable(false);
        }
        internetAmmountTable.getColumnModel().getColumn(1).setMaxWidth(75);
        internetAmmountTable.setShowHorizontalLines(true);
        internetAmmountTable.setShowVerticalLines(true);
        internetAmmountTable.setTableHeader(null);

        DefaultTableCellRenderer col1 = new DefaultTableCellRenderer();
        col1.setHorizontalAlignment(SwingConstants.CENTER);
        internetAmmountTable.getColumnModel().getColumn(1).setCellRenderer(col1);

        DefaultTableCellRenderer col0 = new DefaultTableCellRenderer();
        col0.setHorizontalAlignment(SwingConstants.LEFT);
        internetAmmountTable.getColumnModel().getColumn(0).setCellRenderer(col0);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.2;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        internetPane.add(jScrollPane2, gridBagConstraints);

        topHorizontalSplit.setRightComponent(internetPane);

        jPanel5.add(topHorizontalSplit);

        mainVerticalSplit.setLeftComponent(jPanel5);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel1.add(mainVerticalSplit, gridBagConstraints);

        jTabbedPane1.addTab("Network monitor", jPanel1);

        jPanel3.setLayout(new java.awt.GridBagLayout());

        jPanel24.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 2, 2, 2, new java.awt.Color(57, 57, 57)));
        jPanel24.setLayout(new java.awt.GridBagLayout());

        jPanel29.setBorder(javax.swing.BorderFactory.createMatteBorder(2, 2, 2, 2, new java.awt.Color(104, 104, 104)));
        jPanel29.setMinimumSize(new java.awt.Dimension(0, 100));
        jPanel29.setPreferredSize(new java.awt.Dimension(200, 100));
        jPanel29.setLayout(new java.awt.GridBagLayout());

        jScrollPane5.setBorder(javax.swing.BorderFactory.createMatteBorder(2, 2, 2, 2, new java.awt.Color(104, 104, 104)));
        jScrollPane5.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane5.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        jScrollPane5.setMinimumSize(new java.awt.Dimension(10, 168));
        jScrollPane5.setName(""); // NOI18N
        jScrollPane5.setPreferredSize(new java.awt.Dimension(10, 164));
        jScrollPane5.setRequestFocusEnabled(false);

        serverSummaryTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {"Server state", "ON"},
                {"Clients entered", "0"},
                {"Clients on site", "0"},
                {"Clients left site", "0"},
                {"Accounts created", "0"},
                {"Accounts available", "0"},
                {"Accounts deleted", "0"},
                {"Accounts logged in", "0"},
                {"Accounts online", "0"},
                {"Accounts logged out", "0"}
            },
            new String [] {
                "Title 1", "Title 2"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        serverSummaryTable.setEnabled(false);
        serverSummaryTable.setFocusable(false);
        serverSummaryTable.setMinimumSize(new java.awt.Dimension(0, 0));
        serverSummaryTable.setPreferredSize(new java.awt.Dimension(1000, 1000));
        serverSummaryTable.setRowSelectionAllowed(false);
        serverSummaryTable.setShowHorizontalLines(false);
        serverSummaryTable.setShowVerticalLines(false);
        jScrollPane5.setViewportView(serverSummaryTable);
        serverSummaryTable.getColumnModel().getColumn(0).setMaxWidth(130);
        serverSummaryTable.getColumnModel().getColumn(0).setMinWidth(130);

        serverSummaryTable.setShowVerticalLines(true);
        serverSummaryTable.setShowHorizontalLines(true);
        serverSummaryTable.setTableHeader(null);

        DefaultTableCellRenderer serverTableCell = new DefaultTableCellRenderer();
        serverTableCell.setHorizontalAlignment(SwingConstants.CENTER);
        serverSummaryTable.getColumnModel().getColumn(1).setCellRenderer(serverTableCell);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        jPanel29.add(jScrollPane5, gridBagConstraints);

        jPanel32.setBorder(javax.swing.BorderFactory.createMatteBorder(2, 2, 0, 2, new java.awt.Color(104, 104, 104)));
        jPanel32.setPreferredSize(new java.awt.Dimension(61, 26));
        jPanel32.setLayout(new java.awt.GridBagLayout());

        jLabel15.setText("Summary:");
        jLabel15.setMaximumSize(new java.awt.Dimension(9999948, 199994));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(5, 4, 5, 5);
        jPanel32.add(jLabel15, gridBagConstraints);

        filler32.setBackground(new java.awt.Color(153, 153, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 1.0;
        jPanel32.add(filler32, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        jPanel29.add(jPanel32, gridBagConstraints);

        jScrollPane13.setBorder(javax.swing.BorderFactory.createMatteBorder(2, 2, 2, 2, new java.awt.Color(104, 104, 104)));
        jScrollPane13.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane13.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        jScrollPane13.setMinimumSize(new java.awt.Dimension(10, 51));
        jScrollPane13.setName(""); // NOI18N
        jScrollPane13.setPreferredSize(new java.awt.Dimension(10, 51));

        serverErrorsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {"Used accounts",  new Integer(0)},
                {"Missing login to log in",  new Integer(0)},
                {"Wrong auth data",  new Integer(0)}
            },
            new String [] {
                "Title 1", "Title 2"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Integer.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        serverErrorsTable.setEnabled(false);
        serverErrorsTable.setFocusable(false);
        serverErrorsTable.setRowSelectionAllowed(false);
        jScrollPane13.setViewportView(serverErrorsTable);
        serverErrorsTable.getColumnModel().getColumn(0).setMaxWidth(130);
        serverErrorsTable.getColumnModel().getColumn(0).setMinWidth(130);

        serverErrorsTable.setShowVerticalLines(true);
        serverErrorsTable.setShowHorizontalLines(true);
        serverErrorsTable.setTableHeader(null);

        DefaultTableCellRenderer serverErrorsTableCell = new DefaultTableCellRenderer();
        serverErrorsTableCell.setHorizontalAlignment(SwingConstants.CENTER);
        serverErrorsTable.getColumnModel().getColumn(1).setCellRenderer(serverErrorsTableCell);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        jPanel29.add(jScrollPane13, gridBagConstraints);

        jPanel43.setBorder(javax.swing.BorderFactory.createMatteBorder(2, 2, 0, 2, new java.awt.Color(104, 104, 104)));
        jPanel43.setLayout(new java.awt.GridBagLayout());

        jLabel18.setText("Server errors:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(5, 4, 5, 5);
        jPanel43.add(jLabel18, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 1.0;
        jPanel43.add(filler37, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        jPanel29.add(jPanel43, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.weighty = 1.0;
        jPanel29.add(filler38, gridBagConstraints);

        serverSplitPane.setLeftComponent(jPanel29);

        jPanel28.setBorder(javax.swing.BorderFactory.createMatteBorder(2, 2, 2, 2, new java.awt.Color(104, 104, 104)));
        jPanel28.setLayout(new java.awt.GridBagLayout());

        jPanel30.setBorder(javax.swing.BorderFactory.createMatteBorder(2, 2, 2, 2, new java.awt.Color(104, 104, 104)));
        jPanel30.setLayout(new java.awt.GridBagLayout());

        jLabel13.setFont(new java.awt.Font("VeriBest Gerber 0", 0, 18)); // NOI18N
        jLabel13.setText("Supervisor");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 6);
        jPanel30.add(jLabel13, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE;
        gridBagConstraints.weightx = 1.0;
        jPanel30.add(filler27, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE;
        gridBagConstraints.weightx = 1.0;
        jPanel30.add(filler28, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel28.add(jPanel30, gridBagConstraints);

        serverTabbedPane.setBorder(javax.swing.BorderFactory.createMatteBorder(2, 2, 2, 2, new java.awt.Color(42, 119, 158)));
        serverTabbedPane.setTabPlacement(javax.swing.JTabbedPane.LEFT);
        serverTabbedPane.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                serverTabbedPaneStateChanged(evt);
            }
        });

        clientsOnSitePanel.setLayout(new java.awt.GridBagLayout());

        jScrollPane14.setBorder(null);

        clientsOnSiteTextPane.setEditable(false);
        clientsOnSiteTextPane.setBorder(null);
        jScrollPane14.setViewportView(clientsOnSiteTextPane);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        clientsOnSitePanel.add(jScrollPane14, gridBagConstraints);

        serverTabbedPane.addTab("Clients on site", clientsOnSitePanel);

        AccountsAvailablePanel.setLayout(new java.awt.GridBagLayout());

        jScrollPane16.setBorder(null);

        accountsAvailableTextPane.setEditable(false);
        accountsAvailableTextPane.setBorder(null);
        jScrollPane16.setViewportView(accountsAvailableTextPane);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        AccountsAvailablePanel.add(jScrollPane16, gridBagConstraints);

        serverTabbedPane.addTab("Accounts available", AccountsAvailablePanel);

        accountsOnlinePanel.setLayout(new java.awt.GridBagLayout());

        jScrollPane17.setBorder(null);

        accountsOnlineTextPane.setEditable(false);
        jScrollPane17.setViewportView(accountsOnlineTextPane);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        accountsOnlinePanel.add(jScrollPane17, gridBagConstraints);

        serverTabbedPane.addTab("Accounts online", accountsOnlinePanel);

        dataTransferPanel.setLayout(new java.awt.GridBagLayout());

        jScrollPane15.setBorder(null);
        jScrollPane15.setForeground(new java.awt.Color(9, 165, 4));

        dataTransferTextPane.setEditable(false);
        dataTransferTextPane.setBorder(null);
        dataTransferTextPane.setContentType("text/plane"); // NOI18N
        dataTransferTextPane.setForeground(new java.awt.Color(9, 165, 4));
        dataTransferTextPane.setToolTipText("");
        dataTransferTextPane.setFocusable(false);
        jScrollPane15.setViewportView(dataTransferTextPane);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        dataTransferPanel.add(jScrollPane15, gridBagConstraints);

        serverTabbedPane.addTab("Data transfer", dataTransferPanel);

        clientServerActivityPanel.setLayout(new java.awt.GridBagLayout());

        jPanel25.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 1.0;
        jPanel25.add(filler22, gridBagConstraints);

        jLabel11.setText("Server");
        jLabel11.setMinimumSize(new java.awt.Dimension(48, 25));

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, serverLabel, org.jdesktop.beansbinding.ELProperty.create("${font}"), jLabel11, org.jdesktop.beansbinding.BeanProperty.create("font"));
        bindingGroup.addBinding(binding);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE;
        jPanel25.add(jLabel11, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 1.0;
        jPanel25.add(filler23, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        jPanel25.add(filler24, gridBagConstraints);

        jLabel12.setText("CLIENT");

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, serverLabel, org.jdesktop.beansbinding.ELProperty.create("${font}"), jLabel12, org.jdesktop.beansbinding.BeanProperty.create("font"));
        bindingGroup.addBinding(binding);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 4);
        jPanel25.add(jLabel12, gridBagConstraints);

        clientServerActivitiesComboBox.setMinimumSize(new java.awt.Dimension(120, 30));
        clientServerActivitiesComboBox.setPreferredSize(new java.awt.Dimension(120, 30));
        clientServerActivitiesComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clientServerActivitiesComboBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel25.add(clientServerActivitiesComboBox, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        clientServerActivityPanel.add(jPanel25, gridBagConstraints);

        jScrollPane11.setBorder(null);

        clientServerActivitiesTextPane.setEditable(false);
        clientServerActivitiesTextPane.setBorder(null);
        jScrollPane11.setViewportView(clientServerActivitiesTextPane);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        clientServerActivityPanel.add(jScrollPane11, gridBagConstraints);

        serverTabbedPane.addTab("Client/Server activities", clientServerActivityPanel);

        jPanel33.setLayout(new java.awt.GridBagLayout());

        jScrollPane18.setBorder(null);

        createAccountThreadTextPane.setEditable(false);
        createAccountThreadTextPane.setBorder(null);
        jScrollPane18.setViewportView(createAccountThreadTextPane);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel33.add(jScrollPane18, gridBagConstraints);

        serverTabbedPane.addTab("CreateAccountThread", jPanel33);

        jPanel34.setLayout(new java.awt.GridBagLayout());

        jScrollPane23.setBorder(null);

        loginThreadTextPane.setEditable(false);
        loginThreadTextPane.setBorder(null);
        jScrollPane23.setViewportView(loginThreadTextPane);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel34.add(jScrollPane23, gridBagConstraints);

        serverTabbedPane.addTab("LoginThread", jPanel34);

        jPanel35.setLayout(new java.awt.GridBagLayout());

        jScrollPane20.setBorder(null);

        logoutThreadTextPane.setEditable(false);
        logoutThreadTextPane.setBorder(null);
        jScrollPane20.setViewportView(logoutThreadTextPane);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel35.add(jScrollPane20, gridBagConstraints);

        serverTabbedPane.addTab("LogoutThread", jPanel35);

        jPanel36.setLayout(new java.awt.GridBagLayout());

        jScrollPane21.setBorder(null);

        deleteAccountThreadTextPane.setEditable(false);
        deleteAccountThreadTextPane.setBorder(null);
        jScrollPane21.setViewportView(deleteAccountThreadTextPane);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel36.add(jScrollPane21, gridBagConstraints);

        serverTabbedPane.addTab("DeleteAccountThread", jPanel36);

        jPanel37.setLayout(new java.awt.GridBagLayout());

        jScrollPane22.setBorder(null);

        leaveThreadTextPane.setEditable(false);
        leaveThreadTextPane.setBorder(null);
        jScrollPane22.setViewportView(leaveThreadTextPane);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel37.add(jScrollPane22, gridBagConstraints);

        serverTabbedPane.addTab("LeaveThread", jPanel37);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel28.add(serverTabbedPane, gridBagConstraints);

        serverSplitPane.setRightComponent(jPanel28);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel24.add(serverSplitPane, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        jPanel3.add(jPanel24, gridBagConstraints);

        jPanel31.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 2, 0, new java.awt.Color(57, 57, 57)));
        jPanel31.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 1.0;
        jPanel31.add(filler29, gridBagConstraints);

        jLabel14.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel14.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/Server.png"))); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel31.add(jLabel14, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 1.0;
        jPanel31.add(filler30, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        jPanel3.add(jPanel31, gridBagConstraints);

        jTabbedPane1.addTab("Server", jPanel3);

        jPanel6.setLayout(new java.awt.GridBagLayout());

        jPanel41.setBorder(javax.swing.BorderFactory.createMatteBorder(2, 2, 2, 2, new java.awt.Color(57, 57, 57)));
        jPanel41.setLayout(new java.awt.GridBagLayout());

        jPanel39.setBorder(javax.swing.BorderFactory.createMatteBorder(2, 2, 2, 2, new java.awt.Color(104, 104, 104)));
        jPanel39.setMinimumSize(new java.awt.Dimension(0, 100));
        jPanel39.setName(""); // NOI18N
        jPanel39.setLayout(new java.awt.GridBagLayout());

        jPanel40.setLayout(new java.awt.GridBagLayout());

        jLabel16.setText("Quarantine summary:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanel40.add(jLabel16, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 1.0;
        jPanel40.add(filler34, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        jPanel39.add(jPanel40, gridBagConstraints);

        jScrollPane12.setBorder(javax.swing.BorderFactory.createMatteBorder(2, 2, 2, 2, new java.awt.Color(104, 104, 104)));
        jScrollPane12.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane12.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        jScrollPane12.setMinimumSize(new java.awt.Dimension(10, 82));
        jScrollPane12.setPreferredSize(new java.awt.Dimension(10, 82));

        quarantineSummaryTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {"Accounts incomed",  new Integer(0)},
                {"LexerCheck errors",  new Integer(0)},
                {"GrammarCheck errors",  new Integer(0)},
                {"SemanticsCheck errors",  new Integer(0)},
                {"Accounts passed",  new Integer(0)}
            },
            new String [] {
                "Title 1", "Title 2"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Integer.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        quarantineSummaryTable.setEnabled(false);
        quarantineSummaryTable.setFocusable(false);
        quarantineSummaryTable.setRowSelectionAllowed(false);
        jScrollPane12.setViewportView(quarantineSummaryTable);
        quarantineSummaryTable.getColumnModel().getColumn(0).setMaxWidth(160);
        quarantineSummaryTable.getColumnModel().getColumn(0).setMinWidth(160);

        quarantineSummaryTable.setShowVerticalLines(true);
        quarantineSummaryTable.setShowHorizontalLines(true);
        quarantineSummaryTable.setTableHeader(null);

        DefaultTableCellRenderer clientTableCell = new DefaultTableCellRenderer();
        clientTableCell.setHorizontalAlignment(SwingConstants.CENTER);
        quarantineSummaryTable.getColumnModel().getColumn(1).setCellRenderer(clientTableCell);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        jPanel39.add(jScrollPane12, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.weighty = 1.0;
        jPanel39.add(filler31, gridBagConstraints);

        clientsSplitPane.setLeftComponent(jPanel39);

        jPanel38.setLayout(new java.awt.GridBagLayout());

        jPanel8.setBorder(javax.swing.BorderFactory.createMatteBorder(2, 2, 2, 2, new java.awt.Color(104, 104, 104)));
        jPanel8.setLayout(new java.awt.GridBagLayout());

        jLabel2.setText("CLIENT");
        jLabel2.setMaximumSize(new java.awt.Dimension(47, 20));
        jLabel2.setMinimumSize(new java.awt.Dimension(45, 24));

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, serverLabel, org.jdesktop.beansbinding.ELProperty.create("${font}"), jLabel2, org.jdesktop.beansbinding.BeanProperty.create("font"));
        bindingGroup.addBinding(binding);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        jPanel8.add(jLabel2, gridBagConstraints);

        clientInformationComboBox.setPreferredSize(new java.awt.Dimension(120, 20));

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, clientComboBox, org.jdesktop.beansbinding.ELProperty.create("${minimumSize}"), clientInformationComboBox, org.jdesktop.beansbinding.BeanProperty.create("minimumSize"));
        bindingGroup.addBinding(binding);

        clientInformationComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clientInformationComboBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel8.add(clientInformationComboBox, gridBagConstraints);

        jLabel3.setText("(NUMBER)");
        jLabel3.setMinimumSize(new java.awt.Dimension(45, 24));

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, serverLabel, org.jdesktop.beansbinding.ELProperty.create("${font}"), jLabel3, org.jdesktop.beansbinding.BeanProperty.create("font"));
        bindingGroup.addBinding(binding);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        jPanel8.add(jLabel3, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 1.0;
        jPanel8.add(filler12, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 1.0;
        jPanel8.add(filler13, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanel38.add(jPanel8, gridBagConstraints);

        jPanel9.setBorder(javax.swing.BorderFactory.createMatteBorder(2, 2, 2, 2, new java.awt.Color(104, 104, 104)));
        jPanel9.setLayout(new javax.swing.BoxLayout(jPanel9, javax.swing.BoxLayout.LINE_AXIS));

        clientInformationHorizontalSplit.setBorder(null);
        clientInformationHorizontalSplit.setDividerLocation(199);
        clientInformationHorizontalSplit.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        jPanel11.setLayout(new java.awt.GridBagLayout());

        clientInformationTextPane.setEditable(false);
        clientInformationTextPane.setBorder(javax.swing.BorderFactory.createMatteBorder(2, 2, 2, 2, new java.awt.Color(101, 101, 101)));
        clientInformationTextPane.setContentType("text/html"); // NOI18N
        jScrollPane1.setViewportView(clientInformationTextPane);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel11.add(jScrollPane1, gridBagConstraints);

        clientInformationHorizontalSplit.setLeftComponent(jPanel11);

        jPanel10.setMaximumSize(new java.awt.Dimension(100, 2147483647));
        jPanel10.setMinimumSize(new java.awt.Dimension(100, 27));
        jPanel10.setLayout(new java.awt.GridBagLayout());

        jScrollPane6.setBorder(null);
        jScrollPane6.setMaximumSize(new java.awt.Dimension(100, 32767));
        jScrollPane6.setMinimumSize(new java.awt.Dimension(100, 27));
        jScrollPane6.setPreferredSize(new java.awt.Dimension(100, 357));

        clientInformationTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {"clientID", null},
                {"presonalData", null},
                {"enterTime", null},
                {"lifeTime (ms)", null},
                {"dhClient", null},
                {"leaveTime", null}
            },
            new String [] {
                "Parameter", "Value"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                true, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        clientInformationTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_LAST_COLUMN);
        clientInformationTable.setEnabled(false);
        clientInformationTable.setFocusable(false);
        clientInformationTable.setMaximumSize(new java.awt.Dimension(60, 80));
        clientInformationTable.setRowSelectionAllowed(false);
        clientInformationTable.getTableHeader().setReorderingAllowed(false);
        jScrollPane6.setViewportView(clientInformationTable);
        if (clientInformationTable.getColumnModel().getColumnCount() > 0) {
            clientInformationTable.getColumnModel().getColumn(0).setResizable(false);
            clientInformationTable.getColumnModel().getColumn(1).setResizable(false);
        }
        clientInformationTable.setShowHorizontalLines(true);
        clientInformationTable.setShowVerticalLines(true);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel10.add(jScrollPane6, gridBagConstraints);

        jLabel4.setText("Primary client information");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel10.add(jLabel4, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel10.add(filler10, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 1.0;
        jPanel10.add(filler11, gridBagConstraints);

        clientInformationHorizontalSplit.setRightComponent(jPanel10);

        jPanel9.add(clientInformationHorizontalSplit);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        jPanel38.add(jPanel9, gridBagConstraints);

        clientsSplitPane.setRightComponent(jPanel38);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel41.add(clientsSplitPane, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        jPanel6.add(jPanel41, gridBagConstraints);

        jPanel42.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 1.0;
        jPanel42.add(filler35, gridBagConstraints);

        jLabel17.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/Clients.png"))); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 0, 4);
        jPanel42.add(jLabel17, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 1.0;
        jPanel42.add(filler36, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        jPanel6.add(jPanel42, gridBagConstraints);

        jTabbedPane1.addTab("Clients", jPanel6);

        jPanel12.setLayout(new java.awt.GridBagLayout());

        jPanel15.setBorder(javax.swing.BorderFactory.createMatteBorder(2, 2, 2, 2, new java.awt.Color(57, 57, 57)));
        jPanel15.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 1.0;
        jPanel15.add(filler18, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 1.0;
        jPanel15.add(filler19, gridBagConstraints);

        jLabel9.setText("SERVER THREAD BUFFERS");
        jLabel9.setMinimumSize(new java.awt.Dimension(45, 30));

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, serverLabel, org.jdesktop.beansbinding.ELProperty.create("${font}"), jLabel9, org.jdesktop.beansbinding.BeanProperty.create("font"));
        bindingGroup.addBinding(binding);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 3;
        gridBagConstraints.ipady = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        jPanel15.add(jLabel9, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 10);
        jPanel12.add(jPanel15, gridBagConstraints);

        jPanel21.setBorder(javax.swing.BorderFactory.createMatteBorder(2, 2, 2, 2, new java.awt.Color(57, 57, 57)));
        jPanel21.setLayout(new java.awt.GridBagLayout());

        jPanel17.setBorder(javax.swing.BorderFactory.createMatteBorder(2, 2, 2, 2, new java.awt.Color(101, 101, 101)));
        jPanel17.setLayout(new java.awt.GridBagLayout());

        jLabel8.setFont(new java.awt.Font("Swis721 Lt BT", 1, 11)); // NOI18N
        jLabel8.setText("DELETE ACCCOUNT");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel17.add(jLabel8, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 1.0;
        jPanel17.add(filler16, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 1.0;
        jPanel17.add(filler17, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel21.add(jPanel17, gridBagConstraints);

        deleteAccountTable.setBorder(javax.swing.BorderFactory.createMatteBorder(2, 2, 2, 2, new java.awt.Color(101, 101, 101)));
        deleteAccountTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "PUT", "TAKE"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        deleteAccountTable.setEnabled(false);
        deleteAccountTable.getTableHeader().setReorderingAllowed(false);
        jScrollPane9.setViewportView(deleteAccountTable);
        DefaultTableCellRenderer deleteAccountTableRenderer = (DefaultTableCellRenderer)createAccountTable.getTableHeader().getDefaultRenderer();
        deleteAccountTableRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        deleteAccountTable.getTableHeader().setDefaultRenderer(deleteAccountTableRenderer);
        deleteAccountTable.setShowHorizontalLines(true);
        deleteAccountTable.setShowVerticalLines(true);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel21.add(jScrollPane9, gridBagConstraints);

        bufferSplitPane2.setRightComponent(jPanel21);

        jPanel18.setBorder(javax.swing.BorderFactory.createMatteBorder(2, 2, 2, 2, new java.awt.Color(57, 57, 57)));
        jPanel18.setLayout(new java.awt.GridBagLayout());

        jPanel14.setBorder(javax.swing.BorderFactory.createMatteBorder(2, 2, 2, 2, new java.awt.Color(101, 101, 101)));
        jPanel14.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 1.0;
        jPanel14.add(filler5, gridBagConstraints);

        jLabel5.setFont(new java.awt.Font("Swis721 Lt BT", 1, 11)); // NOI18N
        jLabel5.setText("CREATE ACCOUNT");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel14.add(jLabel5, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 1.0;
        jPanel14.add(filler6, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel18.add(jPanel14, gridBagConstraints);

        createAccountTable.setBorder(javax.swing.BorderFactory.createMatteBorder(2, 2, 2, 2, new java.awt.Color(101, 101, 101)));
        createAccountTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "PUT", "TAKE"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                true, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        createAccountTable.setEnabled(false);
        createAccountTable.getTableHeader().setReorderingAllowed(false);
        jScrollPane4.setViewportView(createAccountTable);
        DefaultTableCellRenderer createAccountTableRenderer = (DefaultTableCellRenderer)createAccountTable.getTableHeader().getDefaultRenderer();
        createAccountTableRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        createAccountTable.getTableHeader().setDefaultRenderer(createAccountTableRenderer);
        createAccountTable.setShowHorizontalLines(true);
        createAccountTable.setShowVerticalLines(true);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel18.add(jScrollPane4, gridBagConstraints);

        bufferSplitPane4.setLeftComponent(jPanel18);

        jPanel19.setBorder(javax.swing.BorderFactory.createMatteBorder(2, 2, 2, 2, new java.awt.Color(57, 57, 57)));
        jPanel19.setLayout(new java.awt.GridBagLayout());

        jPanel13.setBorder(javax.swing.BorderFactory.createMatteBorder(2, 2, 2, 2, new java.awt.Color(101, 101, 101)));
        jPanel13.setMinimumSize(new java.awt.Dimension(42, 28));
        jPanel13.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 1.0;
        jPanel13.add(filler7, gridBagConstraints);

        jLabel6.setFont(new java.awt.Font("Swis721 Lt BT", 1, 11)); // NOI18N
        jLabel6.setText("LOGIN");
        jPanel13.add(jLabel6, new java.awt.GridBagConstraints());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 1.0;
        jPanel13.add(filler8, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel19.add(jPanel13, gridBagConstraints);

        loginTable.setBorder(javax.swing.BorderFactory.createMatteBorder(2, 2, 2, 2, new java.awt.Color(101, 101, 101)));
        loginTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "PUT", "TAKE"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        loginTable.setEnabled(false);
        loginTable.getTableHeader().setReorderingAllowed(false);
        jScrollPane7.setViewportView(loginTable);
        DefaultTableCellRenderer loginTableRenderer = (DefaultTableCellRenderer)createAccountTable.getTableHeader().getDefaultRenderer();
        loginTableRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        loginTable.getTableHeader().setDefaultRenderer(loginTableRenderer);
        loginTable.setShowHorizontalLines(true);
        loginTable.setShowVerticalLines(true);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel19.add(jScrollPane7, gridBagConstraints);

        bufferSplitPane4.setRightComponent(jPanel19);

        bufferSplitPane3.setLeftComponent(bufferSplitPane4);

        jPanel20.setBorder(javax.swing.BorderFactory.createMatteBorder(2, 2, 2, 2, new java.awt.Color(57, 57, 57)));
        jPanel20.setLayout(new java.awt.GridBagLayout());

        jPanel16.setBorder(javax.swing.BorderFactory.createMatteBorder(2, 2, 2, 2, new java.awt.Color(101, 101, 101)));
        jPanel16.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 1.0;
        jPanel16.add(filler14, gridBagConstraints);

        jLabel7.setFont(new java.awt.Font("Swis721 Lt BT", 1, 11)); // NOI18N
        jLabel7.setText("LOGOUT");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel16.add(jLabel7, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 1.0;
        jPanel16.add(filler15, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel20.add(jPanel16, gridBagConstraints);

        logoutTable.setBorder(javax.swing.BorderFactory.createMatteBorder(2, 2, 2, 2, new java.awt.Color(101, 101, 101)));
        logoutTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "PUT", "TAKE"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        logoutTable.setEnabled(false);
        logoutTable.getTableHeader().setReorderingAllowed(false);
        jScrollPane8.setViewportView(logoutTable);
        DefaultTableCellRenderer logoutTableRenderer = (DefaultTableCellRenderer)createAccountTable.getTableHeader().getDefaultRenderer();
        logoutTableRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        logoutTable.getTableHeader().setDefaultRenderer(logoutTableRenderer);
        logoutTable.setShowHorizontalLines(true);
        logoutTable.setShowVerticalLines(true);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel20.add(jScrollPane8, gridBagConstraints);

        bufferSplitPane3.setRightComponent(jPanel20);

        bufferSplitPane2.setLeftComponent(bufferSplitPane3);

        bufferSplitPane1.setLeftComponent(bufferSplitPane2);

        jPanel22.setBorder(javax.swing.BorderFactory.createMatteBorder(2, 2, 2, 2, new java.awt.Color(57, 57, 57)));
        jPanel22.setLayout(new java.awt.GridBagLayout());

        leaveTable.setBorder(javax.swing.BorderFactory.createMatteBorder(2, 2, 2, 2, new java.awt.Color(101, 101, 101)));
        leaveTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "PUT", "TAKE"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        leaveTable.setEnabled(false);
        leaveTable.getTableHeader().setReorderingAllowed(false);
        jScrollPane10.setViewportView(leaveTable);
        leaveTable.setShowHorizontalLines(true);
        leaveTable.setShowVerticalLines(true);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel22.add(jScrollPane10, gridBagConstraints);

        jPanel23.setBorder(javax.swing.BorderFactory.createMatteBorder(2, 2, 2, 2, new java.awt.Color(101, 101, 101)));
        jPanel23.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 1.0;
        jPanel23.add(filler20, gridBagConstraints);

        jLabel10.setFont(new java.awt.Font("Swis721 Lt BT", 1, 11)); // NOI18N
        jLabel10.setText("LEAVE");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel23.add(jLabel10, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 1.0;
        jPanel23.add(filler21, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel22.add(jPanel23, gridBagConstraints);

        bufferSplitPane1.setRightComponent(jPanel22);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel12.add(bufferSplitPane1, gridBagConstraints);

        jTabbedPane1.addTab("Buffers", jPanel12);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 5, 5);
        getContentPane().add(jTabbedPane1, gridBagConstraints);
        jTabbedPane1.getAccessibleContext().setAccessibleName("Main pane");
        jTabbedPane1.getAccessibleContext().setAccessibleDescription("");

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        getContentPane().add(filler26, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        getContentPane().add(filler25, gridBagConstraints);

        bindingGroup.bind();

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void toolStartSystemButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_toolStartSystemButtonActionPerformed
        // TODO add your handling code here:
        if ( ! systemStarted ) {
            systemStarted = true;
            toolStartSystemButton.setText("Pause System");
            topStartSystemButton.setForeground(Color.green);
            
            for ( javax.swing.JLabel systemLedIndicator : systemLedIndicators ) {
                systemLedIndicator.setForeground(Color.green);
            }
            this.startThreads();
        } else {
            if ( systemPaused ) {
                toolStartSystemButton.setText("Pause System");
                topStartSystemButton.setForeground(Color.green);
                synchronized (systemMonitor){
                    systemMonitor.notifyAll();
                }
                for ( javax.swing.JLabel systemLedIndicator : systemLedIndicators ) {
                    systemLedIndicator.setForeground(Color.green);
                }
            } else {
                toolStartSystemButton.setText("Resume System");
                topStartSystemButton.setForeground(Color.red);
                
                for ( javax.swing.JLabel systemLedIndicator : systemLedIndicators ) {
                    systemLedIndicator.setForeground(Color.red);
                }
            }
            systemPaused = !systemPaused;
        }
    }//GEN-LAST:event_toolStartSystemButtonActionPerformed

    private void clientComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clientComboBoxActionPerformed
        // TODO add your handling code here:
        if ( clientComboBox.getSelectedIndex() < 0 ) {
            return;   
        }
        ClientHistory clientHistory = server.getClientHistory(clientComboBox.getSelectedIndex());
        DefaultTableModel clientTableModel = ((DefaultTableModel)clientTable.getModel());
        clientTableModel.setRowCount(0);
        for ( int i = 0, size = clientHistory.ammountOfActions(); i < size ; i++ ) {
            clientTableModel.addRow(new Object[] {clientHistory.getClientActions().get(i).getAction(),clientHistory.getClientActions().get(i).getActionTime(),clientHistory.getClientActions().get(i).getActionResult()});
        }
    }//GEN-LAST:event_clientComboBoxActionPerformed

    private void clientInformationComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clientInformationComboBoxActionPerformed
        // TODO add your handling code here:
        if ( clientInformationComboBox.getSelectedIndex() < 0 ) {
            return;   
        }
        ClientInformation clientInformation = clientsInformation.get(clientInformationComboBox.getSelectedIndex());
        clientInformationTable.setValueAt(clientInformation.getClientID(), 0, 1);
        clientInformationTable.setValueAt(clientInformation.getPersonalData(), 1, 1);
        clientInformationTable.setValueAt(clientInformation.getEnterTime(), 2, 1);
        clientInformationTable.setValueAt(clientInformation.getLifeTime(), 3, 1);
        clientInformationTable.setValueAt(clientInformation.getDHClient(), 4, 1);
        clientInformationTable.setValueAt(clientInformation.getLeaveTime(), 5, 1);
        
        clientInformationTextPane.setText(clientInformation.getMainHistory());
        updateRowHeights();
    }//GEN-LAST:event_clientInformationComboBoxActionPerformed

    private void topStartSystemButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_topStartSystemButtonActionPerformed
        // TODO add your handling code here:
        if ( ! systemStarted ) {
            systemStarted = true;
            toolStartSystemButton.setText("Pause System");
            topStartSystemButton.setForeground(Color.green);
            
            for ( javax.swing.JLabel systemLedIndicator : systemLedIndicators ) {
                systemLedIndicator.setForeground(Color.green);
            }
            this.startThreads();
        } else {
            if ( systemPaused ) {
                toolStartSystemButton.setText("Pause System");
                topStartSystemButton.setForeground(Color.green);
                synchronized (systemMonitor){
                    systemMonitor.notifyAll();
                }
                for ( javax.swing.JLabel systemLedIndicator : systemLedIndicators ) {
                    systemLedIndicator.setForeground(Color.green);
                }
            } else {
                toolStartSystemButton.setText("Resume System");
                topStartSystemButton.setForeground(Color.red);
                
                for ( javax.swing.JLabel systemLedIndicator : systemLedIndicators ) {
                    systemLedIndicator.setForeground(Color.red);
                }
            }
            systemPaused = !systemPaused;
        }
    }//GEN-LAST:event_topStartSystemButtonActionPerformed

    private void toolStartInternetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_toolStartInternetButtonActionPerformed
        // TODO add your handling code here:
       
        if ( internetPaused ) {
            toolStartInternetButton.setText("Pause Internet Thread");
            topStartInternetButton.setForeground(Color.green);
            internetLedIndicator.setForeground(Color.green);
            synchronized (internetMonitor){
                internetMonitor.notifyAll();
            }
        } else {
            toolStartInternetButton.setText("Resume Internet Thread");
            topStartInternetButton.setForeground(Color.red);
            internetLedIndicator.setForeground(Color.red);
        }
        internetPaused = !internetPaused;
    }//GEN-LAST:event_toolStartInternetButtonActionPerformed

    private void topStartInternetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_topStartInternetButtonActionPerformed
        // TODO add your handling code here:
        if ( internetPaused ) {
            toolStartInternetButton.setText("Pause Internet Thread");
            topStartInternetButton.setForeground(Color.green);
            internetLedIndicator.setForeground(Color.green);
            synchronized (internetMonitor){
                internetMonitor.notifyAll();
            }
        } else {
            toolStartInternetButton.setText("Resume Internet Thread");
            topStartInternetButton.setForeground(Color.red);
            internetLedIndicator.setForeground(Color.red);
        }
        internetPaused = !internetPaused;
    }//GEN-LAST:event_topStartInternetButtonActionPerformed

    private void toolStartServerButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_toolStartServerButtonActionPerformed
        // TODO add your handling code here:
        if ( serverPaused ) {
            toolStartServerButton.setText("Pause Server Threads");
            topStartServerButton.setForeground(Color.green);
            serverLedIndicator.setForeground(Color.green);
            try {
                Server.serverSummaryTableBuffer.put(new SetValueAt(TableManager.ActionType.PUT, "ON", 0, 1));
            } catch (InterruptedException e) {}
            synchronized (serverMonitor){
                serverMonitor.notifyAll();
            }
        } else {
            toolStartServerButton.setText("Resume Server Threads");
            topStartServerButton.setForeground(Color.red);
            serverLedIndicator.setForeground(Color.red);
            try {
                Server.serverSummaryTableBuffer.put(new SetValueAt(TableManager.ActionType.PUT, "OFF", 0, 1));
            } catch (InterruptedException e) {}
        }
        serverPaused = !serverPaused;
    }//GEN-LAST:event_toolStartServerButtonActionPerformed

    private void topStartServerButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_topStartServerButtonActionPerformed
        // TODO add your handling code here:
        if ( serverPaused ) {
            toolStartServerButton.setText("Pause Server Threads");
            topStartServerButton.setForeground(Color.green);
            serverLedIndicator.setForeground(Color.green);
            try {
                Server.serverSummaryTableBuffer.put(new SetValueAt(TableManager.ActionType.PUT, "ON", 0, 1));
            } catch (InterruptedException e) {}
            synchronized (serverMonitor){
                serverMonitor.notifyAll();
            }
        } else {
            toolStartServerButton.setText("Resume Server Threads");
            topStartServerButton.setForeground(Color.red);
            serverLedIndicator.setForeground(Color.red);
            try {
                Server.serverSummaryTableBuffer.put(new SetValueAt(TableManager.ActionType.PUT, "OFF", 0, 1));
            } catch (InterruptedException e) {}
        }
        serverPaused = !serverPaused;
    }//GEN-LAST:event_topStartServerButtonActionPerformed

    private void toolStartClientButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_toolStartClientButtonActionPerformed
        // TODO add your handling code here:
        if ( clientPaused ) {
            toolStartClientButton.setText("Pause Client Threads");
            topStartClientButton.setForeground(Color.green);
            clientLedIndicator.setForeground(Color.green);
            synchronized (clientMonitor){
                clientMonitor.notifyAll();
            }
        } else {
            toolStartClientButton.setText("Resume Client Threads");
            topStartClientButton.setForeground(Color.red);
            clientLedIndicator.setForeground(Color.red);
        }
        clientPaused = !clientPaused;
    }//GEN-LAST:event_toolStartClientButtonActionPerformed

    private void topStartClientButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_topStartClientButtonActionPerformed
        // TODO add your handling code here:
        if ( clientPaused ) {
            toolStartClientButton.setText("Pause Client Threads");
            topStartClientButton.setForeground(Color.green);
            clientLedIndicator.setForeground(Color.green);
            synchronized (clientMonitor){
                clientMonitor.notifyAll();
            }
        } else {
            toolStartClientButton.setText("Resume Client Threads");
            topStartClientButton.setForeground(Color.red);
            clientLedIndicator.setForeground(Color.red);
        }
        clientPaused = !clientPaused;
    }//GEN-LAST:event_topStartClientButtonActionPerformed

    private void serverTabbedPaneStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_serverTabbedPaneStateChanged
        // TODO add your handling code here:
        if ( server == null) {
            return;
        }
        switch ( serverTabbedPane.getSelectedIndex() ) {
            case 0: // clients on site
                this.clientsOnSiteTextPane.setText(server.getClientsOnSiteInformation().getClientsOnSite());
                break;
            case 1: // accounts available
                this.accountsAvailableTextPane.setText(server.getAccountsAvailableInformation().getAccountsAvailable());
                break;
            case 2: // accounts online
                this.accountsOnlineTextPane.setText(server.getAccountsOnlineInformation().getAccountsOnline());
                break;
            case 3: // data transfer
                this.dataTransferTextPane.setText("");
                try {
                    Style black = dataTransferTextPane.getStyledDocument().addStyle("StyleName", null);
                    StyleConstants.setBackground(black, Color.black);
                    StyleConstants.setForeground(black, new Color(17,107,24));
                    dataTransferTextPane.getStyledDocument().insertString(0, server.getDataTransferInformation().getDataTransfer(), black);
                } catch( BadLocationException ex) {}
                break;
            case 4: // client-server activity
                this.clientServerActivitiesTextPane.setText(server.getClientServerActivitiesInformation().getClientServerActivities(this.clientServerActivitiesComboBox.getSelectedIndex() + 1));
                break;
            case 5: // create account thread
                this.createAccountThreadTextPane.setText(server.getCreateAccountThreadInformation().getInformation());
                break;
            case 6: // login account thread
                this.loginThreadTextPane.setText(server.getLoginThreadInformation().getInformation());
                break;
            case 7: // logout account thread
                this.logoutThreadTextPane.setText(server.getLogoutThreadInformation().getInformation());
                break;
            case 8: // delete account thread
                this.deleteAccountThreadTextPane.setText(server.getDeleteAccountThreadInformation().getInformation());
                break;
            case 9: // leave thread;
                this.leaveThreadTextPane.setText(server.getLeaveThreadInformation().getInformation());
                break;
            default:
                break;
        }
    }//GEN-LAST:event_serverTabbedPaneStateChanged

    private void clientServerActivitiesComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clientServerActivitiesComboBoxActionPerformed
        // TODO add your handling code here:
        if ( ! this.clientServerActivitiesComboBox.isVisible() ) {
            return;
        }
        this.clientServerActivitiesTextPane.setText(server.getClientServerActivitiesInformation().getClientServerActivities(this.clientServerActivitiesComboBox.getSelectedIndex() + 1));
    }//GEN-LAST:event_clientServerActivitiesComboBoxActionPerformed

    private void updateRowHeights()
    {
        for (int row = 0; row < clientInformationTable.getRowCount(); row++)
        {
            int rowHeight = 30;

            for (int column = 0; column < clientInformationTable.getColumnCount(); column++)
            {
                Component comp = clientInformationTable.prepareRenderer(clientInformationTable.getCellRenderer(row, column), row, column);
                rowHeight = Math.max(rowHeight, comp.getPreferredSize().height);
            }
            if(rowHeight != clientInformationTable.getRowHeight(row)) {
                clientInformationTable.setRowHeight(row, rowHeight);
            }
        }
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        SwingUtilities.invokeLater(() -> {
            MainWindow mainWindow = new MainWindow();
            mainWindow.setVisible(true);
//            mainWindow.setupGUI();
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel AccountsAvailablePanel;
    private javax.swing.JTextPane accountsAvailableTextPane;
    private javax.swing.JPanel accountsOnlinePanel;
    private javax.swing.JTextPane accountsOnlineTextPane;
    private javax.swing.JSplitPane bottomHorizontalSplit;
    private javax.swing.JSplitPane bufferSplitPane1;
    private javax.swing.JSplitPane bufferSplitPane2;
    private javax.swing.JSplitPane bufferSplitPane3;
    private javax.swing.JSplitPane bufferSplitPane4;
    private javax.swing.JComboBox<String> clientComboBox;
    private javax.swing.JComboBox<String> clientInformationComboBox;
    private javax.swing.JSplitPane clientInformationHorizontalSplit;
    private javax.swing.JTable clientInformationTable;
    private javax.swing.JTextPane clientInformationTextPane;
    private javax.swing.JLabel clientLabel;
    private javax.swing.JLabel clientLedIndicator;
    private javax.swing.JLabel clientNumberLabel;
    private javax.swing.JPanel clientPane;
    private javax.swing.JScrollPane clientScroll;
    private javax.swing.JComboBox<String> clientServerActivitiesComboBox;
    private javax.swing.JTextPane clientServerActivitiesTextPane;
    private javax.swing.JPanel clientServerActivityPanel;
    private javax.swing.JTable clientTable;
    private javax.swing.JPanel clientsOnSitePanel;
    private javax.swing.JTextPane clientsOnSiteTextPane;
    private javax.swing.JSplitPane clientsSplitPane;
    private javax.swing.JTable createAccountTable;
    private javax.swing.JTextPane createAccountThreadTextPane;
    private javax.swing.JPanel dataTransferPanel;
    private javax.swing.JTextPane dataTransferTextPane;
    private javax.swing.JTable deleteAccountTable;
    private javax.swing.JTextPane deleteAccountThreadTextPane;
    private javax.swing.Box.Filler filler1;
    private javax.swing.Box.Filler filler10;
    private javax.swing.Box.Filler filler11;
    private javax.swing.Box.Filler filler12;
    private javax.swing.Box.Filler filler13;
    private javax.swing.Box.Filler filler14;
    private javax.swing.Box.Filler filler15;
    private javax.swing.Box.Filler filler16;
    private javax.swing.Box.Filler filler17;
    private javax.swing.Box.Filler filler18;
    private javax.swing.Box.Filler filler19;
    private javax.swing.Box.Filler filler2;
    private javax.swing.Box.Filler filler20;
    private javax.swing.Box.Filler filler21;
    private javax.swing.Box.Filler filler22;
    private javax.swing.Box.Filler filler23;
    private javax.swing.Box.Filler filler24;
    private javax.swing.Box.Filler filler25;
    private javax.swing.Box.Filler filler26;
    private javax.swing.Box.Filler filler27;
    private javax.swing.Box.Filler filler28;
    private javax.swing.Box.Filler filler29;
    private javax.swing.Box.Filler filler3;
    private javax.swing.Box.Filler filler30;
    private javax.swing.Box.Filler filler31;
    private javax.swing.Box.Filler filler32;
    private javax.swing.Box.Filler filler34;
    private javax.swing.Box.Filler filler35;
    private javax.swing.Box.Filler filler36;
    private javax.swing.Box.Filler filler37;
    private javax.swing.Box.Filler filler38;
    private javax.swing.Box.Filler filler5;
    private javax.swing.Box.Filler filler6;
    private javax.swing.Box.Filler filler7;
    private javax.swing.Box.Filler filler8;
    private javax.swing.JTable internetAmmountTable;
    private javax.swing.JLabel internetLabel;
    private javax.swing.JLabel internetLedIndicator;
    private javax.swing.JPanel internetPane;
    private javax.swing.JScrollPane internetScroll;
    private javax.swing.JTable internetTable;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel15;
    private javax.swing.JPanel jPanel16;
    private javax.swing.JPanel jPanel17;
    private javax.swing.JPanel jPanel18;
    private javax.swing.JPanel jPanel19;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel20;
    private javax.swing.JPanel jPanel21;
    private javax.swing.JPanel jPanel22;
    private javax.swing.JPanel jPanel23;
    private javax.swing.JPanel jPanel24;
    private javax.swing.JPanel jPanel25;
    private javax.swing.JPanel jPanel26;
    private javax.swing.JPanel jPanel27;
    private javax.swing.JPanel jPanel28;
    private javax.swing.JPanel jPanel29;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel30;
    private javax.swing.JPanel jPanel31;
    private javax.swing.JPanel jPanel32;
    private javax.swing.JPanel jPanel33;
    private javax.swing.JPanel jPanel34;
    private javax.swing.JPanel jPanel35;
    private javax.swing.JPanel jPanel36;
    private javax.swing.JPanel jPanel37;
    private javax.swing.JPanel jPanel38;
    private javax.swing.JPanel jPanel39;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel40;
    private javax.swing.JPanel jPanel41;
    private javax.swing.JPanel jPanel42;
    private javax.swing.JPanel jPanel43;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane10;
    private javax.swing.JScrollPane jScrollPane11;
    private javax.swing.JScrollPane jScrollPane12;
    private javax.swing.JScrollPane jScrollPane13;
    private javax.swing.JScrollPane jScrollPane14;
    private javax.swing.JScrollPane jScrollPane15;
    private javax.swing.JScrollPane jScrollPane16;
    private javax.swing.JScrollPane jScrollPane17;
    private javax.swing.JScrollPane jScrollPane18;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane20;
    private javax.swing.JScrollPane jScrollPane21;
    private javax.swing.JScrollPane jScrollPane22;
    private javax.swing.JScrollPane jScrollPane23;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JScrollPane jScrollPane9;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTable leaveTable;
    private javax.swing.JTextPane leaveThreadTextPane;
    private javax.swing.JTable loginTable;
    private javax.swing.JTextPane loginThreadTextPane;
    private javax.swing.JTable logoutTable;
    private javax.swing.JTextPane logoutThreadTextPane;
    private javax.swing.JSplitPane mainVerticalSplit;
    private javax.swing.JTable quarantineSummaryTable;
    private javax.swing.JTable serverAmmountTable;
    private javax.swing.JTable serverErrorsTable;
    private javax.swing.JLabel serverLabel;
    private javax.swing.JLabel serverLedIndicator;
    private javax.swing.JPanel serverPane;
    private javax.swing.JScrollPane serverScroll;
    private javax.swing.JSplitPane serverSplitPane;
    private javax.swing.JTable serverSummaryTable;
    private javax.swing.JTabbedPane serverTabbedPane;
    private javax.swing.JTable serverTable;
    private javax.swing.JLabel speedGainLabel;
    private javax.swing.JSpinner speedGainSpinner;
    private javax.swing.JLabel systemLedIndicator0;
    private javax.swing.JLabel systemLedIndicator1;
    private javax.swing.JLabel systemLedIndicator2;
    private javax.swing.JPanel toolPane;
    private javax.swing.JButton toolStartClientButton;
    private javax.swing.JButton toolStartInternetButton;
    private javax.swing.JButton toolStartServerButton;
    private javax.swing.JButton toolStartSystemButton;
    private javax.swing.JSplitPane topHorizontalSplit;
    private javax.swing.JButton topStartClientButton;
    private javax.swing.JButton topStartInternetButton;
    private javax.swing.JButton topStartServerButton;
    private javax.swing.JButton topStartSystemButton;
    private org.jdesktop.beansbinding.BindingGroup bindingGroup;
    // End of variables declaration//GEN-END:variables
}




