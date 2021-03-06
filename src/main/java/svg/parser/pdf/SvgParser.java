package svg.parser.pdf;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripperByArea;
import org.bson.Document;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class SvgParser {
    public static String JSON_OUTPUT;
    public static String NEXT_COUNT;
    public static int DAYS_CALC;
    public static int DAYS_ALL_CALC;
    public static int DAY_ALL_CONS;
    public static int NIGHT_ALL_CONS;
    public static int PRV_CONSUME_D;
    public static int PRV_CONSUME_N;
    public static int NOW_CONSUME_D;
    public static int NOW_CONSUME_N;
    public static int PRJ_CONSUME_D;
    public static int PRJ_CONSUME_N;
    public static JComboBox cbEPC;
    public static JComboBox cbLixi;
    public static JComboBox cbKeys;
    public static JTextField txtAmtResult;
    public static JTextField txtDCons;
    public static JTextField txtNCons;
    public static JDatePickerImpl datePicker;
    public static JLabel lblInvoice;
    public static JLabel lblLixi;
    public static JLabel lblEnanti;
    public static JLabel lblDCons;
    public static JLabel lblNCons;
    public static JLabel lblNextCount;
    public static JButton btnCalc;
    public static JTextArea txtCALC = new JTextArea();
    private SvgParser() {
        //utility class and should not be constructed.
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            usage();
        } else {
            File file = new File(args[0]);
            if (args[0].toUpperCase().endsWith(".PDF")){
                pdfExtract(args);
            } else if (args[0].toUpperCase().equals("APP0")){
                PPCpdfParse();
            } else if (args[0].toUpperCase().equals("APP1")){
                PPCApp();
            } else if (file.isDirectory()) {
                PPCprfParseFolder(args);
            } else {
                usage();
            }

        }
    }

    private static void usage(){
        System.out.println("arg[0]: .PDF filename or folder of PDF's or APP to open application arg[1]: INSERTMONGO/INSERTORA to be inserted");
    }

    private static void pdfExtract(String[] args) throws IOException {
        pdfReadFields(args[0]);
        if (args[1].toUpperCase().equals("INSERTMONGO")) {
            putPPCinMongo();
        } else if (args[1].toUpperCase().equals("INSERTORA")){
            putPPCinOracle();
        }
    }

    private static void PPCprfParseFolder(String[] args) throws IOException {
        final File folder = new File(args[0]);
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isFile()) {
                if (fileEntry.getName().toUpperCase().endsWith(".PDF")) {
                    String[] argsnew = new String[2];
                    argsnew[0]=fileEntry.getPath();
                    argsnew[1]=args[1];
                    pdfExtract(argsnew);
                }
            }
        }

    }

    private static void pdfReadFields(String filename) throws IOException {
        Rectangle rectInvoiceType = new Rectangle(450, 20, 180, 20);
        String invoicetype = get_rect_char(filename, 0, rectInvoiceType).trim();

        Rectangle rectCustName = new Rectangle(275, 95, 250, 15);
        String custname = get_rect_char(filename, 0, rectCustName).trim();

        Rectangle rectCustAddress1 = new Rectangle(275, 110, 250, 15);
        String custaddress1 = get_rect_char(filename, 0, rectCustAddress1).trim();

        Rectangle rectCustAddress2 = new Rectangle(275, 125, 165, 15);
        String custaddress2 = get_rect_char(filename, 0, rectCustAddress2);

        Rectangle rectEPC = new Rectangle(10, 170, 275, 20);
        String epc = get_rect_char(filename, 0, rectEPC);

        Rectangle rectnextCnt = new Rectangle(450, 200, 180, 15);
        String nextcnt = get_rect_char(filename, 0, rectnextCnt);

        String nextcntrd = nextcnt.substring(6,10)+"-"+nextcnt.substring(3,5)+"-"+nextcnt.substring(0,2)+"T"+"00:00:00.000Z";


        Rectangle rectAdr1 = new Rectangle(275, 200, 180, 15);
        String adr1 = get_rect_char(filename, 0, rectAdr1);

        Rectangle rectAdr2 = new Rectangle(275, 215, 180, 15);
        String adr2 = get_rect_char(filename, 0, rectAdr2);

        Rectangle rectAmt = new Rectangle(10, 220, 120, 20);
        String amt = get_rect_char(filename, 0, rectAmt);

        Rectangle rectLixi = new Rectangle(130, 220, 100, 20);
        String lixi = get_rect_char(filename, 0, rectLixi).trim();
        String lixiord = lixi.substring(6,10)+"-"+lixi.substring(3,5)+"-"+lixi.substring(0,2)+"T"+"00:00:00.000Z";

        Rectangle rectConsFrom = new Rectangle(135, 260, 70, 15);
        String consFrom = get_rect_char(filename, 0, rectConsFrom).replace(".","").trim();

        Rectangle rectContractNumber = new Rectangle(170, 245, 130, 15);
        String contractNumber = get_rect_char(filename, 0, rectContractNumber).replace(".", "").trim();

        Rectangle rectConsTo = new Rectangle(225, 260, 70, 15);
        String consTo = get_rect_char(filename, 0, rectConsTo).replace(".", "").trim();

        Rectangle rectDays = new Rectangle(170, 275, 130, 15);
        String days = get_rect_char(filename, 0, rectDays).replace(".", "").trim();

        Rectangle rectConsume = new Rectangle(170, 290, 130, 15);
        String consume = get_rect_char(filename, 0, rectConsume).replace(".","").trim();

        Rectangle rectIssueDate = new Rectangle(170, 305, 130, 15);
        String issuedate = get_rect_char(filename, 0, rectIssueDate).replace(".","").trim();

        Rectangle rectAccNumber = new Rectangle(170, 315, 130, 15);
        String accNumber = get_rect_char(filename, 0, rectAccNumber).replace(".","").trim();

        Rectangle rectCustomer = new Rectangle(170, 340, 130, 15);
        String customer = get_rect_char(filename, 0, rectCustomer).replace(".","").trim();

        Rectangle rectInvoice = new Rectangle(170, 355, 130, 15);
        String invoice = get_rect_char(filename, 0, rectInvoice).replace(".","").trim();

        Rectangle rectTIN = new Rectangle(170, 365, 130, 15);
        String tin = get_rect_char(filename, 0, rectTIN).replace(".","").trim();

        Rectangle rectPPCAmt = new Rectangle(500, 310, 130, 15);
        String ppcAmt = get_rect_char(filename, 0, rectPPCAmt).replace(".","").trim();

        Rectangle rectRegulatedAmt = new Rectangle(500, 325, 130, 15);
        String regulatedAmt = get_rect_char(filename, 0, rectRegulatedAmt).replace(".","").trim();

        Rectangle rectEnanti = new Rectangle(500, 340, 130, 15);
        String enanti = get_rect_char(filename, 0, rectEnanti).replace(".","").trim();

        Rectangle rectMiscAmt = new Rectangle(500, 355, 130, 15);
        String miscAmt = get_rect_char(filename, 0, rectMiscAmt).replace(".","").trim();

        Rectangle rectVATAmt = new Rectangle(500, 370, 130, 15);
        String vatAmt = get_rect_char(filename, 0, rectVATAmt).replace(".","").trim();

        Rectangle rectERTAmt = new Rectangle(500, 390, 130, 15);
        String ertAmt = get_rect_char(filename, 0, rectERTAmt).replace(".","").trim();

        Rectangle rectUnpayedAmt = new Rectangle(500, 410, 130, 15);
        String unpayedAmt = get_rect_char(filename, 0, rectUnpayedAmt).replace(".","").trim();

        Rectangle rectKWHDayLast = new Rectangle(120, 90, 35, 10);
        String KWHDayLast = get_rect_char(filename, 1, rectKWHDayLast).replace(".","").trim();

        Rectangle rectKWHDayBefore = new Rectangle(155, 90, 35, 10);
        String KWHDayBefore = get_rect_char(filename, 1, rectKWHDayBefore).replace(".","").trim();

        Rectangle rectKWHDayCons = new Rectangle(190, 90, 35, 10);
        String KWHDayCons = get_rect_char(filename, 1, rectKWHDayCons).replace(".","").trim();

        Rectangle rectKWHNightLast = new Rectangle(120, 100, 35, 10);
        String KWHNightLast = get_rect_char(filename, 1, rectKWHNightLast).replace(".","").trim();

        Rectangle rectKWHNightBefore = new Rectangle(155, 100, 35, 10);
        String KWHNightBefore = get_rect_char(filename, 1, rectKWHNightBefore).replace(".","").trim();

        Rectangle rectKWHNightCons = new Rectangle(190, 100, 35, 10);
        String KWHNightCons = get_rect_char(filename, 1, rectKWHNightCons).replace(".","").trim();

        Rectangle rectKWHDayFix = new Rectangle(300, 70, 300, 10);
        String KWHDayFix = get_rect_char(filename, 1, rectKWHDayFix).replace(".","").trim();

        Rectangle rectKWHDayDtls = new Rectangle(300, 80, 300, 10);
        String KWHDayDtls = get_rect_char(filename, 1, rectKWHDayDtls).replace(".","").trim();

        Rectangle rectKWHnightFix = new Rectangle(300, 90, 300, 10);
        String KWHnightFix = get_rect_char(filename, 1, rectKWHnightFix).replace(".","").trim();
        if (KWHnightFix.contains(":")){KWHnightFix="";}

        Rectangle rectKWHnightDtls = new Rectangle(300, 100, 300, 10);
        String KWHnightDtls = get_rect_char(filename, 1, rectKWHnightDtls).replace(".","").trim();

        JSON_OUTPUT = "{"+"\n" +
                      "\"invoicetype\":\""+invoicetype+"\",\n" +
                      "\"custname\":\""+custname+"\",\n" +
                      "\"custaddress\":\""+custaddress1 + " " + custaddress2+"\",\n" +
                      "\"epc\":\""+epc+"\",\n" +
                      "\"building\":\""+adr1 + " " + adr2+"\",\n" +
                      "\"nextcnt\": \""+nextcnt+"\",\n" +
                      "\"nextcntrd\": \""+nextcntrd+"\",\n" +
                      "\"amt\":\""+amt+"\",\n" +
                      "\"lixi\":\""+lixi+"\",\n" +
                      "\"lixiord\":\""+lixiord+"\",\n" +
                      "\"contractNumber\":\""+contractNumber+"\",\n" +
                      "\"consPeriod\":\""+consFrom +" - " + consTo+"\",\n" +
                      "\"days\":\""+days+"\",\n" +
                      "\"consume\":\""+consume+"\",\n" +
                      "\"issuedate\":\""+issuedate+"\",\n" +
                      "\"accNumber\":\""+accNumber+"\",\n" +
                      "\"customer\":\""+customer+"\",\n" +
                      "\"invoice\":\""+invoice+"\",\n" +
                      "\"tin\":\""+tin+"\",\n" +
                      "\"ppcAmt\":\""+ppcAmt+"\",\n" +
                      "\"regulatedAmt\":\""+regulatedAmt+"\",\n" +
                      "\"enanti\":\""+enanti+"\",\n" +
                      "\"miscAmt\":\""+miscAmt+"\",\n" +
                      "\"vatAmt\":\""+vatAmt+"\",\n" +
                      "\"ertAmt\":\""+ertAmt+"\",\n" +
                      "\"unpayedAmt\":\""+unpayedAmt+"\",\n" +
                      "\"KWHDayLast\":\""+KWHDayLast+"\",\n" +
                      "\"KWHDayBefore\":\""+KWHDayBefore+"\",\n" +
                      "\"KWHDayCons\":\""+KWHDayCons+"\",\n" +
                      "\"KWHNightLast\":\""+KWHNightLast+"\",\n" +
                      "\"KWHNightBefore\":\""+KWHNightBefore+"\",\n" +
                      "\"KWHNightCons\":\""+KWHNightCons+"\",\n" +
                "\"KWHDayFix\":\""+KWHDayFix+"\",\n" +
                "\"KWHDayDtls\":\""+KWHDayDtls+"\",\n" +
                "\"KWHnightFix\":\""+KWHnightFix+"\",\n" +
                "\"KWHnightDtls\":\""+KWHnightDtls+"\"\n" +
                      "}";
        System.out.println(JSON_OUTPUT);
    }



    private static String get_rect_char(String load, int pageNum, Rectangle rect) throws IOException {
        PDDocument document = null;
        try {
            document = PDDocument.load(new File(load));
            PDFTextStripperByArea stripper = new PDFTextStripperByArea();
            stripper.setSortByPosition(true);
            stripper.addRegion("class1", rect);
            PDPage firstPage = document.getPage(pageNum);
            stripper.extractRegions(firstPage);

            return stripper.getTextForRegion("class1").trim();
        } finally {
            if (document != null) {
                document.close();
            }
        }
    }

    private static void PPCpdfParse() {
        JFrame frm = new JFrame("Select P.P.C. (�.�.�.) Invoice (pdf)");
        JPanel pnl = new JPanel();
        pnl.setLayout(null);
        final JTextArea txtFile = new JTextArea();
        txtFile.setSize(500,40);
        txtFile.setLocation(10,10);
        txtFile.setEnabled(false);
        txtFile.setFont(txtFile.getFont().deriveFont(Font.BOLD));
        txtFile.setDisabledTextColor(Color.black);
        pnl.add(txtFile);
        final JTextArea txtExtract = new JTextArea();
        txtExtract.setSize(500,765);
        txtExtract.setLocation(10,55);
        txtExtract.setEnabled(false);
        txtExtract.setFont(txtFile.getFont().deriveFont(Font.BOLD));
        txtExtract.setDisabledTextColor(Color.black);
        pnl.add(txtExtract);
        final JButton btnFile = new JButton("...");
        btnFile.setSize(20,20);
        btnFile.setLocation(510,10);
        pnl.add(btnFile);
        btnFile.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser();
                FileNameExtensionFilter filter = new FileNameExtensionFilter("PDF FILES", "pdf", "pdf");
                fc.setFileFilter(filter);
                fc.setCurrentDirectory(new File(System.getProperty("user.home")));
                int result = fc.showOpenDialog(btnFile);
                if (result==JFileChooser.APPROVE_OPTION){
                    File selectedFile = fc.getSelectedFile();
                        txtFile.setText(selectedFile.getAbsolutePath());
                    try {
                        pdfReadFields(txtFile.getText());
                        txtExtract.setText(JSON_OUTPUT);
                        txtExtract.setToolTipText("Double click to copy JSON");
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });
        txtExtract.addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && !e.isConsumed()) {
                    e.consume();
                    //handle double click event.
                    StringSelection stringSelection = new StringSelection(txtExtract.getText());
                    Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
                    clpbrd.setContents(stringSelection, null);
                    txtExtract.setToolTipText("JSON Copied to Clipboard");
                    //putPPCinMongo();
                    //putPPCinOracle();
                }
            }
            public void mousePressed(MouseEvent e) {}
            public void mouseReleased(MouseEvent e) {}
            public void mouseEntered(MouseEvent e) {}
            public void mouseExited(MouseEvent e) {}
        });
        frm.add(pnl);
        frm.setSize(550,900);
        frm.setVisible(true);
        frm.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    private static void PPCApp() {
        JFrame frm = new JFrame("P.P.C. Invoices ");
        JPanel pnlMngData = new JPanel();
        pnlMngData.setLayout(null);
        cbEPC = new JComboBox();
        cbEPC.setLocation(10,15);
        cbEPC.setSize(200,20);
        pnlMngData.add(cbEPC);

        cbEPC.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getEPClixiDates(cbEPC.getSelectedItem().toString());
            }
        });

        cbLixi = new JComboBox();
        cbLixi.setLocation(10,40);
        cbLixi.setSize(100,20);
        pnlMngData.add(cbLixi);


        cbKeys = new JComboBox();
        cbKeys.setLocation(10,65);
        cbKeys.setSize(200,20);
        pnlMngData.add(cbKeys);
        getBillKeys();

        cbLixi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getEPCRes(cbEPC.getSelectedItem().toString(), cbLixi.getSelectedItem().toString(), cbKeys.getSelectedItem().toString());
            }
        });

        cbKeys.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getEPCRes(cbEPC.getSelectedItem().toString(), cbLixi.getSelectedItem().toString(), cbKeys.getSelectedItem().toString());
            }
        });


        txtAmtResult = new JTextField();
        txtAmtResult.setLocation(220, 65);
        txtAmtResult.setSize(200, 20);
        pnlMngData.add(txtAmtResult);
        pnlMngData.setSize(440, 95);
        pnlMngData.setLocation(10, 10);
        TitledBorder brdMngData;
        brdMngData = BorderFactory.createTitledBorder("Mongo Data");
        pnlMngData.setBorder(brdMngData);



        JPanel pnlProjection = new JPanel();
        pnlProjection.setLayout(null);
        pnlProjection.setSize(440, 290);
        pnlProjection.setLocation(10, 110);
        TitledBorder brdProjection;
        brdProjection = BorderFactory.createTitledBorder("Project Consumption");
        pnlProjection.setBorder(brdProjection);

        UtilDateModel model = new UtilDateModel();

        Properties p = new Properties();
        p.put("text.today", "Today");
        p.put("text.month", "Month");
        p.put("text.year", "Year");
        JDatePanelImpl datePanel = new JDatePanelImpl(model, p);

        txtDCons = new JTextField();
        txtDCons.setLocation(10, 15);
        txtDCons.setSize(150,20);
        GhostText ghostTextD = new GhostText(txtDCons, "Day kWh Consumption");
        txtDCons.setFont(new Font(null, Font.BOLD, 14));
        pnlProjection.add(txtDCons);

        txtNCons = new JTextField();
        txtNCons.setLocation(10, 40);
        txtNCons.setSize(150,20);
        GhostText ghostTextN = new GhostText(txtNCons, "Night kWh Consumption");
        txtNCons.setFont(new Font(null, Font.BOLD, 14));
        pnlProjection.add(txtNCons);

        datePicker = new JDatePickerImpl(datePanel, new DateLabelFormatter());
        datePicker.setLocation(10,65);
        datePicker.setSize(150,20);
        pnlProjection.add(datePicker);
        frm.setLayout(null);

        datePicker.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                miscProjection();
            }
        });

        txtCALC = new JTextArea();
        txtCALC.setEnabled(false);
        txtCALC.setFont(txtCALC.getFont().deriveFont(Font.BOLD));
        txtCALC.setDisabledTextColor(Color.black);
        //JScrollPane scroll = new JScrollPane(txtCALC);
        JScrollPane scroll = new JScrollPane (txtCALC,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scroll.setSize(420,190);
        scroll.setLocation(10, 90);
        //Add Textarea in to middle panel
        pnlProjection.add(scroll);

        txtCALC.addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && !e.isConsumed()) {
                    e.consume();
                    //handle double click event.
                    StringSelection stringSelection = new StringSelection(txtCALC.getText());
                    Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
                    clpbrd.setContents(stringSelection, null);
                    txtCALC.setToolTipText("Copied to Clipboard");
                    try {
                        String content = txtCALC.getText();
                        String fname = NEXT_COUNT;
                        fname = fname.replace('/','_');
                        Date selectedDate = (Date) datePicker.getModel().getValue();

                        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                        String selectedDate_str = df.format(selectedDate);
                        File file = new File("Projection_"+selectedDate_str+"_for_Lixi_"+fname+".txt");

                        // if file doesnt exists, then create it
                        if (!file.exists()) {
                            file.createNewFile();
                        }

                        FileWriter fw = new FileWriter(file.getAbsoluteFile());
                        BufferedWriter bw = new BufferedWriter(fw);
                        bw.write(content);
                        bw.close();
                        JOptionPane.showMessageDialog(txtCALC,"File: Projection_"+selectedDate_str+"_for_Lixi_"+fname+".txt created!","File Created", JOptionPane.INFORMATION_MESSAGE);
                    }catch (Exception ex){
                        JOptionPane.showMessageDialog(txtCALC,"Error Creating File","ERROR", JOptionPane.ERROR_MESSAGE);
                    }
                    //putProjectionINMongo();
                }
            }
            public void mousePressed(MouseEvent e) {}
            public void mouseReleased(MouseEvent e) {}
            public void mouseEntered(MouseEvent e) {}
            public void mouseExited(MouseEvent e) {}
        });


        btnCalc = new JButton("Calc");
        btnCalc.setLocation(10, 90);
        btnCalc.setSize(150,20);
        btnCalc.setEnabled(false);
        //pnlProjection.add(btnCalc);

        btnCalc.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                calcProjection();
            }
        });


        JPanel pnlLastAcc = new JPanel();
        pnlLastAcc.setLayout(null);
        pnlLastAcc.setSize(180, 165);
        pnlLastAcc.setLocation(10, 120);
        TitledBorder brdLastAcc;
        brdLastAcc = BorderFactory.createTitledBorder("Last Bill");
        pnlLastAcc.setBorder(brdLastAcc);
        //pnlProjection.add(pnlLastAcc);
        lblInvoice = new JLabel("Inv. type");
        lblInvoice.setSize(180, 20);
        lblInvoice.setLocation(10, 15);
        lblInvoice.setFont(new Font(null, Font.BOLD, 14));
        pnlLastAcc.add(lblInvoice);

        lblLixi = new JLabel("Lixi Date");
        lblLixi.setSize(180, 20);
        lblLixi.setLocation(10, 40);
        lblLixi.setFont(new Font(null, Font.BOLD, 14));
        pnlLastAcc.add(lblLixi);

        lblEnanti = new JLabel("Enanti");
        lblEnanti.setSize(180, 20);
        lblEnanti.setLocation(10, 65);
        lblEnanti.setFont(new Font(null, Font.BOLD, 14));
        pnlLastAcc.add(lblEnanti);

        lblDCons = new JLabel("Day kWh");
        lblDCons.setSize(180, 20);
        lblDCons.setLocation(10, 90);
        lblDCons.setFont(new Font(null, Font.BOLD, 14));
        pnlLastAcc.add(lblDCons);

        lblNCons = new JLabel("Night kWh");
        lblNCons.setSize(180, 20);
        lblNCons.setLocation(10, 115);
        lblNCons.setFont(new Font(null, Font.BOLD, 14));
        pnlLastAcc.add(lblNCons);

        lblNextCount = new JLabel("Next Count");
        lblNextCount.setSize(180, 20);
        lblNextCount.setLocation(10, 140);
        lblNextCount.setFont(new Font(null, Font.BOLD, 14));
        pnlLastAcc.add(lblNextCount);




        frm.add(pnlMngData);
        frm.add(pnlProjection);
        frm.setSize(480,450);
        frm.setVisible(true);
        frm.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        getEPC();
    }

    static void calcProjection(){
        //int v_now_
    }

    static void miscProjection(){
        Date selectedDate = (Date) datePicker.getModel().getValue();

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        DateFormat dfclassic = new SimpleDateFormat("dd/MM/yyyy");
        String selectedDate_str = df.format(selectedDate);
        MongoClient client = new MongoClient();
        MongoDatabase database = client.getDatabase("ppc");

        String prv_cnt = null;
        MongoCursor<Document> cursPrev = database.getCollection("bills").find(new Document("epc", cbEPC.getSelectedItem().toString()).append("nextcntrd", new Document("$lt", selectedDate_str))).sort(new Document("nextcntrd", 1)).iterator();
        while (cursPrev.hasNext()) {
            Document doc = cursPrev.next();
            prv_cnt = doc.get("nextcntrd").toString();

        }
        MongoCursor<Document> curs = database.getCollection("bills").find(new Document("epc", cbEPC.getSelectedItem().toString()).append("nextcntrd", new Document("$gt", selectedDate_str))).sort(new Document("lixiord", -1)).iterator();
        while (curs.hasNext()){
            Document doc =  curs.next();
            if (doc.get("invoicetype").equals("������")) {
                lblInvoice.setText("Inv. type:" +doc.get("invoicetype").toString());
                lblLixi.setText("Lixi Date:" +doc.get("lixi").toString());
                lblEnanti.setText("Enanti:" +doc.get("enanti").toString());
                lblDCons.setText("Day kWh:" +doc.get("KWHDayBefore").toString());
                lblNCons.setText("Night kWh:" +doc.get("KWHNightBefore").toString());
                NEXT_COUNT = doc.get("nextcnt").toString();
                lblNextCount.setText("Next Count:" +doc.get("nextcnt").toString());
                try {
                    Date IssuesDt = dfclassic.parse(doc.get("issuedate").toString());
                    System.out.println("selectedDate="+selectedDate);
                    System.out.println("IssuesDt="+IssuesDt);
                    if (selectedDate.compareTo(IssuesDt)>0){
                        btnCalc.setEnabled(true);

                        /**
                         *
                         * TODO: find three ints
                         *
                         * */

                        PRV_CONSUME_D = Integer.parseInt(doc.get("KWHDayBefore").toString());
                        PRV_CONSUME_N = Integer.parseInt(doc.get("KWHNightBefore").toString());
                        NOW_CONSUME_D = Integer.parseInt(txtDCons.getText());
                        NOW_CONSUME_N = Integer.parseInt(txtNCons.getText());


                        long diff = selectedDate.getTime() - df.parse(prv_cnt.substring(0, 10)).getTime();
                        DAYS_CALC =  Math.toIntExact(TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS));

                        diff = dfclassic.parse(doc.get("nextcnt").toString()).getTime() - df.parse(prv_cnt.substring(0,10)).getTime();
                        DAYS_ALL_CALC =  Math.toIntExact(TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS));

                        System.out.println("DAYS_CALC="+DAYS_CALC);
                        System.out.println("DAYS_ALL_CALC="+DAYS_ALL_CALC);


                        PRJ_CONSUME_D = (int)( (NOW_CONSUME_D-PRV_CONSUME_D) * ((double)DAYS_ALL_CALC)/((double)DAYS_CALC));
                        PRJ_CONSUME_N = (int)( (NOW_CONSUME_N-PRV_CONSUME_N)* ((double)DAYS_ALL_CALC)/((double)DAYS_CALC));

                        txtCALC.append("PREV=======================================================" + System.lineSeparator());
                        txtCALC.append("PREV COUNT:" + prv_cnt.substring(0, 10) + System.lineSeparator());
                        txtCALC.append("PREV COUNT_D:" + PRV_CONSUME_D + System.lineSeparator());
                        txtCALC.append("PREV COUNT_N:" + PRV_CONSUME_N + System.lineSeparator());
                        txtCALC.append("ENANTI:" + doc.get("enanti").toString() + System.lineSeparator());
                        txtCALC.append("NOW========================================================" + System.lineSeparator());
                        txtCALC.append("NOW COUNT:" + selectedDate_str + System.lineSeparator());
                        txtCALC.append("NOW_DAYS=" + DAYS_CALC + System.lineSeparator());
                        txtCALC.append("NOW COUNT_D:" + NOW_CONSUME_D + System.lineSeparator());
                        txtCALC.append("NOW COUNT_N:" + NOW_CONSUME_N + System.lineSeparator());
                        txtCALC.append("NOW CONSUMPTION_D:" + (NOW_CONSUME_D - PRV_CONSUME_D) + System.lineSeparator());
                        txtCALC.append("NOW CONSUMPTION_N:" + (NOW_CONSUME_N - PRV_CONSUME_N) + System.lineSeparator());
                        txtCALC.append("PROJ=======================================================" + System.lineSeparator());
                        txtCALC.append("NEXT COUNT:" + doc.get("nextcnt").toString() + System.lineSeparator());
                        txtCALC.append("ALL_DAYS=" + DAYS_ALL_CALC + System.lineSeparator());
                        txtCALC.append("PRJ_CONSUME_D:" + PRJ_CONSUME_D + System.lineSeparator());
                        txtCALC.append("PRJ_CONSUME_N:" + PRJ_CONSUME_N + System.lineSeparator());
                        txtCALC.append("������ ������: (���� ������ + ������) x ����. �������� ������: (4.8+2�) x (" + DAYS_ALL_CALC + "/120):" + round((4.8 + 2) * ((double) DAYS_ALL_CALC) / 120, 2) + System.lineSeparator());
                        double KWHcost_D;
                        double KWHcost_N;
                        if (PRJ_CONSUME_D+PRJ_CONSUME_N > 2000) {
                            KWHcost_D = 0.10252;
                            KWHcost_N = 0.0661;
                        }else{
                            KWHcost_D = 0.0946;
                            KWHcost_N = 0.0661;
                        }

                        double YKOcost_D=0;
                        double YKOcost_N=0;

                        if (PRJ_CONSUME_D+PRJ_CONSUME_N <= 1600) {
                            YKOcost_D = 0.00699;
                            YKOcost_N = 0.00889;
                        }else if ((PRJ_CONSUME_D+PRJ_CONSUME_N >= 1601) && (PRJ_CONSUME_D+PRJ_CONSUME_N <=2000)) {
                            YKOcost_D = 0.01570;
                            YKOcost_N = 0.00889;
                        }else if ((PRJ_CONSUME_D+PRJ_CONSUME_N >= 2001) && (PRJ_CONSUME_D+PRJ_CONSUME_N <=3000)) {
                            YKOcost_D = 0.03987;
                            YKOcost_N = 0.00889;
                        }else if (PRJ_CONSUME_D+PRJ_CONSUME_N >= 3001) {
                            YKOcost_D = 0.04488;
                            YKOcost_N = 0.00889;
                        }

                        txtCALC.append("������ ��������� ������ ( kWh x ���� ��������� ������� ): " + round(((double) PRJ_CONSUME_D * KWHcost_D), 2)+ System.lineSeparator());
                        txtCALC.append("������ ��������� ������ ( kWh x ���� ��������� ������� ���������� ): " + round(((double) PRJ_CONSUME_N * KWHcost_N), 2)+ System.lineSeparator());
                        txtCALC.append("________________________________________"+ System.lineSeparator());
                        double XREOSI_PROMITHEIAS_REUMATOS =  round( (4.8 + 2) * ((double) DAYS_ALL_CALC) / 120,2) +
                                round( ((double)PRJ_CONSUME_D*KWHcost_D),2 ) +
                                round( ((double)PRJ_CONSUME_N*KWHcost_N),2 );
                        txtCALC.append("������ ���������� ��������: " + XREOSI_PROMITHEIAS_REUMATOS + System.lineSeparator());
                        txtCALC.append("________________________________________" + System.lineSeparator());
                        txtCALC.append("������� ���������       :" + (
                                round((0.16 * 25 * ((double) DAYS_ALL_CALC) / 365), 2)
                                        + round((((double) PRJ_CONSUME_D) * 0.00563), 2)
                        )
                                + System.lineSeparator());
                        txtCALC.append("������ ��������         :" + (
                                round((0.56 * 25 * ((double) DAYS_ALL_CALC) / 365), 2)
                                        + round((((double) PRJ_CONSUME_D) * 0.0214), 2)
                        )
                                + System.lineSeparator());
                        txtCALC.append("���                     :" + (
                                round((((double) PRJ_CONSUME_D) * YKOcost_D), 2)
                                        + round((((double) PRJ_CONSUME_N) * YKOcost_N), 2)
                        )
                                + System.lineSeparator());
                        txtCALC.append("������                  :" + round((((double)(PRJ_CONSUME_D+PRJ_CONSUME_N))*0.02630),2) + System.lineSeparator());
                        txtCALC.append("������ ��������         :" + round((((double) (PRJ_CONSUME_D + PRJ_CONSUME_N)) * 0.00046), 2) + System.lineSeparator());
                        txtCALC.append("________________________________________" + System.lineSeparator());
                        double RYTHIMIZOMENES_XREOSEIS = round((0.16 * 25 * ((double)DAYS_ALL_CALC)/365),2)
                                +round((((double)PRJ_CONSUME_D)*0.00563),2)
                                +round((0.56 * 25 * ((double)DAYS_ALL_CALC)/365),2)
                                +round((((double)PRJ_CONSUME_D)*0.0214),2)
                                +round((((double)PRJ_CONSUME_D)*YKOcost_D),2)
                                +round((((double)PRJ_CONSUME_N)*YKOcost_N),2)
                                +round((((double)(PRJ_CONSUME_D+PRJ_CONSUME_N))*0.02630),2)
                                +round((((double)(PRJ_CONSUME_D+PRJ_CONSUME_N))*0.00046),2);
                        txtCALC.append("������������ ��������: " + RYTHIMIZOMENES_XREOSEIS + System.lineSeparator());
                        txtCALC.append("________________________________________" + System.lineSeparator());
                        txtCALC.append("������� ����� ����������� (���):" + round((((double) (PRJ_CONSUME_D + PRJ_CONSUME_N)) * 0.0022), 2) + System.lineSeparator());
                        double EID_TELOS = round((RYTHIMIZOMENES_XREOSEIS
                                +XREOSI_PROMITHEIAS_REUMATOS
                                + round((((double)(PRJ_CONSUME_D+PRJ_CONSUME_N))*0.0022),2)
                                - round((((double)(PRJ_CONSUME_D+PRJ_CONSUME_N))*0.02630),2))
                                *(0.005),2);
                        txtCALC.append("������ ����� 5�: (���� ������. �������� - ������ + ���) x 5�:" + EID_TELOS + System.lineSeparator());
                        double FPA = round((RYTHIMIZOMENES_XREOSEIS+XREOSI_PROMITHEIAS_REUMATOS+ round((((double)(PRJ_CONSUME_D+PRJ_CONSUME_N))*0.0022),2))*0.13,2);
                        txtCALC.append("��� (���):" + FPA + System.lineSeparator());
                        txtCALC.append("________________________________________" + System.lineSeparator());
                        txtCALC.append("������ ����� ���������� �������� + ��� + ������ ����� 5� + ���:" + (RYTHIMIZOMENES_XREOSEIS + XREOSI_PROMITHEIAS_REUMATOS + round((((double) (PRJ_CONSUME_D + PRJ_CONSUME_N)) * 0.0022), 2) + EID_TELOS) + System.lineSeparator());
                        txtCALC.append("________________________________________" + System.lineSeparator());
                        String enanti = doc.get("enanti").toString();
                        enanti = enanti.replace(',','.');
                        double KOROPI = round(49,2);
                        double EPOMENOS_LOGARIASMOS_JAVA = KOROPI+RYTHIMIZOMENES_XREOSEIS+XREOSI_PROMITHEIAS_REUMATOS+ round((((double)(PRJ_CONSUME_D+PRJ_CONSUME_N))*0.0022),2)+EID_TELOS +FPA - round(Double.parseDouble(enanti),2);
                        double EPOMENOS_LOGARIASMOS_EXCEL = KOROPI + ((RYTHIMIZOMENES_XREOSEIS+XREOSI_PROMITHEIAS_REUMATOS- round(Double.parseDouble(enanti), 2))*1.13);
                        txtCALC.append("�������� �����������(JAVA):" + EPOMENOS_LOGARIASMOS_JAVA + System.lineSeparator());
                        txtCALC.append("�������� �����������(EXCEL):"+ EPOMENOS_LOGARIASMOS_EXCEL+ System.lineSeparator());
                    }else{
                        btnCalc.setEnabled(false);

                    }
                } catch (Exception e){
                    System.err.println("ERR--> "+e.getMessage());
                }
                break;
            }
        }

    }

    static void getEPC() {
        MongoClient client = new MongoClient();
        MongoDatabase database = client.getDatabase("ppc");
        MongoCursor<String> curs = database.getCollection("bills").distinct("epc", String.class).iterator();
        while (curs.hasNext()){
            cbEPC.addItem(curs.next());
        }
    }

    static void getEPClixiDates(String epc) {
        ///    db.bills.find({epc:'716050666023'},{'lixi':1})
        MongoClient client = new MongoClient();
        MongoDatabase database = client.getDatabase("ppc");
        MongoCursor<Document> curs = database.getCollection("bills").find(new Document("epc", epc)).sort(new Document("lixiord", 1)).iterator();
        while (curs.hasNext()){
            cbLixi.addItem(curs.next().get("lixi"));
        }
    }

    static void getEPCRes(String epc, String lixi, String key) {
        MongoClient client = new MongoClient();
        MongoDatabase database = client.getDatabase("ppc");
        MongoCursor<Document> curs = database.getCollection("bills").find(new Document("epc", epc).append("lixi", lixi)).iterator();
        while (curs.hasNext()){
            txtAmtResult.setText(curs.next().get(key).toString());
        }
    }

    static void getBillKeys() {
        MongoClient client = new MongoClient();
        MongoDatabase database = client.getDatabase("ppc");
        MongoCursor<Document> curs = database.getCollection("bills_keys").find().iterator();
        while (curs.hasNext()){
            cbKeys.addItem(curs.next().get("_id"));
        }
    }

    private static void putPPCinMongo(){
        MongoClient client = new MongoClient();
        MongoDatabase database = client.getDatabase("ppc");
        final MongoCollection<Document> bills = database.getCollection("bills");
        Document myDoc = Document.parse(JSON_OUTPUT);
        bills.insertOne(myDoc);
        System.out.println("Inserted in MongoDB:localhost:27017:ppc");
    }

    private static void putPPCinOracle(){
        Statement stmt = null;
        try {
            Connection connection = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:XE", "HR", "HR");
            stmt = connection.createStatement();
            String sqlText = "insert into ppc_clob (json) values ('" + JSON_OUTPUT + "')";
            System.out.println(sqlText);

            stmt.executeUpdate(sqlText);

            connection.commit();
            stmt.close();
            System.out.println("Inserted in localhost:1521:XE");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

}
