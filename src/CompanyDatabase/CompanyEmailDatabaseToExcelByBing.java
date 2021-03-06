package CompanyDatabase;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.apache.poi.ss.usermodel.Sheet;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.*;
import java.util.concurrent.CountDownLatch;

/**
 * Created by sdlds on 2016/12/20.
 */
public class CompanyEmailDatabaseToExcelByBing implements Runnable {
    private static String excelPath = "D:\\company_database_masachi.xlsx";
    private Sheet sheet;
    //    private static Workbook wb;
//    private static Row row = null;
//    private static int colnum = 0;
    private static int num = 0;
//    private static int proxynum = 0;
//    private static String originStr;
    private static Document doc = null;
    private WebClient wc;
    //    private static int page = 1;
//    private static int wrong = 0;
//    private static List<Cookie> cookie = new ArrayList<>();
//    private static List<Cookies> cookies = new ArrayList<>();
//    private static JLabel label2;
//    private static JTextArea row1;
//    private static JTextArea row2;
//    private static JLabel label;
//    private static JTextArea cate;
//    private static int emailnum = 0;
//    private static JTextArea origin;
    private static Random random = new Random();
    //    private static String UA_PATH = "file/user_agents";
    private static String URL = "http://global.bing.com/search?q=";
    private static String query = "";
    private Map<Integer, String> excel = new HashMap<>();
    private Map<Integer,String> comexcel = new HashMap<>();
    private List<String> ua = new ArrayList<>();
    private int j = 1;
    private int end;
    private CountDownLatch signal;
    private String phone;

    public CompanyEmailDatabaseToExcelByBing(Sheet sheet, Map<Integer, String> excel,Map<Integer,String> comexcel, List<String> ua, CountDownLatch signal) {
        this.sheet = sheet;
        this.excel = excel;
        this.comexcel = comexcel;
        this.ua = ua;
        //this.j = j;
        //this.end = end;
        this.signal = signal;
    }

    @Override
    public void run() {
        try {
            //ReadExcel();
            //ReadUA();

            System.out.println(Thread.currentThread().getName() + "Process ING!!!!!!!");

            SetBrowser();


            //for (int i = j; i < end; i++) {
            j = MultiThread.GetRow();

            while (j != -1) {
                phone = excel.get(j).trim();
                String url = URL + "\"" + phone  + "\"" + "%20singapore%20email";
                if (sheet.getRow(j - 1).getCell(11) == null || sheet.getRow(j - 1).getCell(11).toString().equals("")) {
                    getDataFromWeb(url, phone);
                } else {
                    System.out.println("Already!" + "-----" + j);
                }
                //j++;
//                if (j % 20 == 0) {
//                    OutputExcel();
//                }
                //}
                j = MultiThread.GetRow();
            }
            signal.countDown();

        } catch (Exception e) {
            if (e instanceof ArrayIndexOutOfBoundsException)
                e.printStackTrace();
            //OutputExcel();
        }
    }

//    private static void ReadUA() {
//        java.io.File file = new java.io.File(UA_PATH);
//        BufferedReader reader = null;
//        try {
//            reader = new BufferedReader(new FileReader(file));
//            String temp = "";
//            while ((temp = reader.readLine()) != null) {
//                ua.add(temp);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    private static void ReadExcel() throws Exception {
//        FileInputStream fileInputStream = new FileInputStream(excelPath);
////        POIFSFileSystem ts = new POIFSFileSystem(fileInputStream);
//        wb = WorkbookFactory.create(fileInputStream);
//        sheet = wb.getSheetAt(0);
//        int totalrow = sheet.getLastRowNum();
//        for (int i = 3; i <= totalrow; i++) {
//            excel.put(i + 1, sheet.getRow(i).getCell(5).toString());
//        }
//        //System.out.println(row.getCell(5));
//    }

    private void SetBrowser() {
        wc = new WebClient(BrowserVersion.CHROME);
        wc.getOptions().setJavaScriptEnabled(true); //启用JS解释器，默认为true
        wc.getOptions().setCssEnabled(false); //禁用css支持
//        wc.getOptions().setProxyConfig(new ProxyConfig("185.10.17.134",3128));
        wc.getCookieManager().setCookiesEnabled(false);
        wc.getOptions().setThrowExceptionOnScriptError(false); //js运行错误时，是否抛出异常
        wc.getOptions().setThrowExceptionOnFailingStatusCode(false);
        wc.getOptions().setTimeout(10000); //设置连接超时时间 ，这里是10S。如果为0，则无限期等待

        wc.waitForBackgroundJavaScript(600 * 1000);
        wc.setAjaxController(new NicelyResynchronizingAjaxController());

        wc.waitForBackgroundJavaScript(1000 * 3);
        wc.setJavaScriptTimeout(0);
        wc.addRequestHeader("User-Agent", ua.get(random.nextInt(9800)));
        wc.getOptions().setTimeout(50000);
    }

//    private static void OutputExcel() {
//        try {
//            FileOutputStream fileOutputStream = new FileOutputStream(excelPath);
//            wb.write(fileOutputStream);
//            fileOutputStream.flush();
//            fileOutputStream.close();
//            System.out.println("Success");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    private void getDataFromWeb(String Url, String company) throws Exception {
        System.out.println(j + "+++++" + Url);
        HtmlPage page = wc.getPage(Url);
////        System.out.println(page);
//        String pageXml = page.asXml(); //以xml的形式获取响应文本
//        doc = Jsoup.connect(Url).get();
        //System.out.println(pageXml);
        String pageXml = page.asXml();
        doc = Jsoup.parse(pageXml);
        //System.out.println(doc);
        getEmail(company);
    }

    private void getEmail(String company) {
        String email = "";
        String companyName = "";
        String title ="";
        Elements results = doc.getElementsByClass("b_algo");

        if(results == null){
            return;
        }
        for(Element ele : results){
            title = ele.select("h2").text().replace("\"","").trim();
            email = ele.getElementsByClass("b_caption").select("p:contains(@)").text().replace("\"","").trim();

            if(!email.equals("") && title.contains(comexcel.get(j))){
                MultiThread.WriteExcel(j - 1, email);
                //MultiThread.AddList(j - 1, email.replace("Email : ",""));
                return;
            }
        }

        if(email.equals("")){
            return;
        }
    }

//    private static void WriteExcel(int rownum, String email) {
//        email = email.replace("\r|\n", "").trim();
//        row = sheet.getRow(rownum);
//        Cell cell;
//        cell = row.createCell(11);
//        cell.setCellValue(email);
//        System.out.println("YES" + "---" + String.valueOf(rownum + 1) + "----" + email);
//    }
}
