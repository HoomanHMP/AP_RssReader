import org.jsoup.*;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.net.*;
import java.util.Scanner;

public class RssReader{
    static int MAX_ITEMS=5;
    static int websiteCounter=0;
    static String[][] websiteInfo = new String[1010][3];

    public static void main(String[] args){

        startUp();

        while(true){

            String action;
            while(true){

                System.out.println("\nType a valid number for your desired action:");
                System.out.println("[1] Show updates");
                System.out.println("[2] Add URL");
                System.out.println("[3] Remove URL");
                System.out.println("[4] Exit");

                action = new Scanner(System.in).nextLine();
                if(action.equals("1") || action.equals("2") || action.equals("3") || action.equals("4")) break;
                else System.out.println("invalid request!\n");
            }

            switch(action){
                case "1":
                    showUpdates();
                    break;
                case "2":
                    addUrl();
                    break;
                case "3":
                    removeUrl();
                    break;
                case "4":
                    shutDown();
                    return;
            }
        }
    }

    private static void startUp(){
        System.out.println("\nWelcome to RSS Reader!");
        try{
            File data = new File("data.txt");
            data.createNewFile();
            FileReader fileReader = new FileReader(data);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            String tmp;
            while((tmp = bufferedReader.readLine())!=null){
                websiteInfo[websiteCounter] = tmp.split(";");
                System.out.println(websiteInfo[websiteCounter][2]);
                websiteCounter++;
            }
        }
        catch(Exception e){
            System.out.println("something went wrong!\n");
        }
    }

    private static void showUpdates(){
        String tmp;
        while(true){
            System.out.println("\nShow updates for:\n");
            System.out.println("[0] All websites");
            for(int i=0;i!=websiteCounter;i++) System.out.println("["+(i+1)+"] "+websiteInfo[i][0]);
            System.out.println("Enter -1 to return.");
            tmp = new Scanner(System.in).nextLine();
            try{
                int request = Integer.parseInt(tmp);
                if(request == -1) return;
                else if(request < -1 || request > websiteCounter) System.out.println("invalid request!\n");
                else if(request == 0) {
                    for(int i=0;i!=websiteCounter;i++){
                        System.out.println("\n"+websiteInfo[i][0]+":");
                        retrieveRssContent(websiteInfo[i][2]);
                    }
                }else{
                    System.out.println("\n"+websiteInfo[request-1][0]+":");
                    retrieveRssContent(websiteInfo[request-1][2]);
                }
            }
            catch(Exception e){
                System.out.println("invalid request!\n");
            }
        }
    }

    private static void addUrl(){

        System.out.println("\nPlease enter website URL to add:");
        String url = new Scanner(System.in).nextLine();
        try{
            String pageSource = fetchPageSource(url);
            String rssUrl = extractRssUrl(url);
            if(rssUrl.isEmpty()) throw new Exception();
            String pageTitle = extractPageTitle(pageSource);
            boolean tmp = false;
            for(int i=0;i!=websiteCounter;i++) if(websiteInfo[i][2].equals(rssUrl)) tmp = true;
            if(tmp) System.out.println(url + " already exists.\n");
            else{
                websiteInfo[websiteCounter][0] = pageTitle;
                websiteInfo[websiteCounter][1] = url;
                websiteInfo[websiteCounter][2] = rssUrl;
                websiteCounter++;
                System.out.println("Added " + url + " successfully.\n");
            }
        }
        catch(Exception e){
            System.out.println("Something went wrong!\n");
        }
    }

    private static void removeUrl(){

        System.out.println("\nPlease enter website URL to remove:");
        String url = new Scanner(System.in).nextLine();
        try{
            String rssUrl = extractRssUrl(url);
            for(int i=0;i!=websiteCounter;i++){
                if(rssUrl.equals(websiteInfo[i][2])){
                    for(int j=i;j!=websiteCounter;j++){
                        websiteInfo[j][0]=websiteInfo[j+1][0];
                        websiteInfo[j][1]=websiteInfo[j+1][1];
                        websiteInfo[j][2]=websiteInfo[j+1][2];
                    }
                    websiteCounter--;
                    System.out.println("Removed " + url + " successfully.\n");
                    return;
                }
            }
            System.out.println(url + " does not exist.\n");

        }
        catch(Exception e){
            System.out.println("Something went wrong!\n");
        }

    }

    private static void shutDown(){

        System.out.println("\nShutting down the RSS Reader...");
        try{
            File data = new File("data.txt");
            FileWriter fileWriter = new FileWriter(data);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            for(int i=0;i!=websiteCounter;i++){
                String tmp = websiteInfo[i][0] +";"+ websiteInfo[i][1] +";"+ websiteInfo[i][2]+"\n";
                bufferedWriter.write(tmp);
            }

            bufferedWriter.close();
        }
        catch(Exception e){
            System.out.println("Something went wrong!\n");
        }
    }

    public static void retrieveRssContent(String rssUrl) {
        try {
            String rssXml = fetchPageSource(rssUrl);
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            StringBuilder xmlStringBuilder = new StringBuilder();
            xmlStringBuilder.append(rssXml);
            ByteArrayInputStream input = new ByteArrayInputStream(
                    xmlStringBuilder.toString().getBytes("UTF-8"));
            org.w3c.dom.Document doc = documentBuilder.parse(input);
            NodeList itemNodes = doc.getElementsByTagName("item");

            for (int i = 0; i < MAX_ITEMS; ++i) {
                Node itemNode = itemNodes.item(i);
                if (itemNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) itemNode;
                    System.out.println("Title: " + element.getElementsByTagName("title").item(0).getTextContent());
                    System.out.println("Link: " + element.getElementsByTagName("link").item(0).getTextContent());
                    System.out.println("Description: " + element.getElementsByTagName("description").item(0).
                            getTextContent());
                }
            }
        } catch (Exception e) {
            System.out.println("Error in retrieving RSS content for " + rssUrl + ": " + e.getMessage());
        }
    }

    public static String extractPageTitle(String html)
    {
        try
        {
            org.jsoup.nodes.Document doc = Jsoup.parse(html);
            return doc.select("title").first().text();
        }
        catch (Exception e)
        {
            return "Error: no title tag found in page source!";
        }
    }

    public static String extractRssUrl(String url) throws IOException
    {
        org.jsoup.nodes.Document doc = Jsoup.connect(url).get();
        return doc.select("[type='application/rss+xml']").attr("abs:href");
    }

    public static String fetchPageSource(String urlString) throws Exception {
        URI uri = new URI(urlString);
        URL url = uri.toURL();
        URLConnection urlConnection = url.openConnection();
        urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML , like Gecko) Chrome / 108.0 .0 .0 Safari / 537.36 ");
        return toString(urlConnection.getInputStream());
    }

    private static String toString(InputStream inputStream) throws IOException
    {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream , "UTF-8"));
        String inputLine;
        StringBuilder stringBuilder = new StringBuilder();
        while ((inputLine = bufferedReader.readLine()) != null)
            stringBuilder.append(inputLine);
        return stringBuilder.toString();
    }
}