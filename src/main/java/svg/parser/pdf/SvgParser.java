package svg.parser.pdf;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripperByArea;
import org.bson.Document;

import javax.swing.*;
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

public class SvgParser {
    public static String JSON_OUTPUT;
    public static JComboBox cbEPC;
    public static JComboBox cbLixi;
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
        System.out.println("arg[0]: .PDF filename or folder of PDF's or APP to open application arg[1]: INSERTDB to be inserted");
    }

    private static void pdfExtract(String[] args) throws IOException {
        pdfReadFields(args[0]);
        if (args[1].toUpperCase().equals("INSERTDB")){
            putPPCinMongo();
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

        Rectangle rectAdr1 = new Rectangle(275, 200, 180, 15);
        String adr1 = get_rect_char(filename, 0, rectAdr1);

        Rectangle rectAdr2 = new Rectangle(275, 215, 180, 15);
        String adr2 = get_rect_char(filename, 0, rectAdr2);

        Rectangle rectAmt = new Rectangle(10, 220, 120, 20);
        String amt = get_rect_char(filename, 0, rectAmt);

        Rectangle rectLixi = new Rectangle(130, 220, 100, 20);
        String lixi = get_rect_char(filename, 0, rectLixi);

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
/*
        System.out.println(invoicetype);
        System.out.println(custname);
        System.out.println(custaddress1 + " " + custaddress2);
        System.out.println(epc);
        System.out.println(adr1 + " " + adr2);
        System.out.println(nextcnt);
        System.out.println(amt);
        System.out.println(lixi);
        System.out.println(contractNumber);
        System.out.println(consFrom +" - " + consTo);

        System.out.println(days);
        System.out.println(consume);
        System.out.println(issuedate);
        System.out.println(accNumber);
        System.out.println(customer);
        System.out.println(invoice);
        System.out.println(tin);
        System.out.println(ppcAmt);
        System.out.println(regulatedAmt);
        System.out.println(enanti);
        System.out.println(miscAmt);
        System.out.println(vatAmt);
        System.out.println(ertAmt);
        System.out.println(unpayedAmt);
        System.out.println(KWHDayLast);
        System.out.println(KWHDayBefore);
        System.out.println(KWHDayCons);
        System.out.println(KWHNightLast);
        System.out.println(KWHNightBefore);
        System.out.println(KWHNightCons);
*/

        JSON_OUTPUT = "{"+"\n" +
                      "\"invoicetype\":\""+invoicetype+"\",\n" +
                      "\"custname\":\""+custname+"\",\n" +
                      "\"custaddress\":\""+custaddress1 + " " + custaddress2+"\",\n" +
                      "\"epc\":\""+epc+"\",\n" +
                      "\"building\":\""+adr1 + " " + adr2+"\",\n" +
                      "\"nextcnt\":\""+nextcnt+"\",\n" +
                      "\"amt\":\""+amt+"\",\n" +
                      "\"lixi\":\""+lixi+"\",\n" +
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
        JFrame frm = new JFrame("Select P.P.C. (Ä.Å.Ç.) Invoice (pdf)");
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
                    putPPCinOracle();
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
        JPanel pnl = new JPanel();
        pnl.setLayout(null);
        cbEPC = new JComboBox();
        cbEPC.setLocation(10,10);
        cbEPC.setSize(200,20);
        pnl.add(cbEPC);

        cbEPC.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getEPClixiDates(cbEPC.getSelectedItem().toString());
            }
        });

        cbLixi = new JComboBox();
        cbLixi.setLocation(10,35);
        cbLixi.setSize(100,20);
        pnl.add(cbLixi);
        frm.add(pnl);
        frm.setSize(400,300);
        frm.setVisible(true);
        frm.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        getEPC();
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
        MongoCursor<Document> curs = database.getCollection("bills").find(new Document("epc", epc)).sort(new Document("lixi","1")).iterator();
        while (curs.hasNext()){
            cbEPC.addItem((String)curs.next().get("lixi"));
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
            Connection connection = DriverManager.getConnection(
                    "jdbc:oracle:thin:@localhost:1521:XE", "HR", "HR");
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

}
