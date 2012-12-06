package XMLParser;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.*;
import java.nio.*;
import java.util.*;

class XMLParserDemo
//klass-obolochka
{
    XMLParserDemo()
    {
        Engine eng = new Engine();
        UI ui = new UI();
        eng.setUI(ui);
        ui.setEngine(eng);
        eng.start();
        ui.start();
    }

    public static void main(String [] args)
    {
        XMLParserDemo p = new XMLParserDemo();
    }


}
class Engine extends Thread
{
    UI ui = null;
    InputStream XMLTextSource = null;
    Document doc = null;
    DocumentBuilder db;
    Cfg configs = null;
    Element currentElement;

    Engine ()
    {
        try
        {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            db = dbf.newDocumentBuilder();
        }
        catch(ParserConfigurationException pce)
        {
            System.out.println("Engine ParserConfigurationException");
            pce.printStackTrace();
        }
    }

    public void setUI(UI link)
    {
        ui = link;
    }
    public void run()
    {
        if(ui==null)
        {
            System.out.println("NO UI");
            System.exit(0);
        }


        while(true)
        {
            try
            {
                while(true)
                {
                    sleep(216000000);
                }
            }
            catch (InterruptedException e)
            {
                //System.out.print("Parser started. Enter commands.Possible commands: ");
                /*
                String s="";
                for (Map.Entry<String, Command> entry : ui.comms.entrySet())
                {
                    s+=entry.getKey()+", ";// + " - " + entry.getValue().desc +"\n";
                }
                s+="\n";
                System.out.print(s);
                System.out.println("Type \"help\" to get description of commands");
                 */

                    try
                    {
                        if(XMLTextSource.available() < 1)
                        {
                            doc = db.newDocument();
                        }
                        else
                        {
                            try
                            {
                                doc = db.parse(XMLTextSource);
                                currentElement = doc.getDocumentElement();
                                interrupted();
                            }
                            catch (IOException ioe)
                            {
                                System.out.println("Engine IOException");
                            }
                            catch (org.xml.sax.SAXException se)
                            {
                                System.out.println("Engine SAXException");
                            }
                        }
                    }
                    catch(IOException ioe)
                    {
                        ioe.printStackTrace();
                    }

            }
        }
    }
}

class UI extends Thread{
    Engine eng = null;
    BufferedReader in;
    PrintWriter out;
    CommandMap comms;
    CommandMap sysComms;
    FilterInput currentComms;
    File xmlFile;
    String [] cfgComms;
    String cmdPrefix;
    public UI(){
        in = new BufferedReader ( new InputStreamReader(System.in) );
        out = new PrintWriter(System.out);

        comms = new CommandMap();
        sysComms = new CommandMap();

        //Common commands

        comms.put("docName", new Command("docName", new ParserCommand(){
                public void execute(UI ui)
                {
                    System.out.println(ui.eng.doc.getDocumentElement().getTagName());

                }
            },"docName Description", this)
        );

        comms.put("add", new Command("add", new ParserCommand(){
            public void execute(UI ui)
            {
                final UI u = ui;
                u.cmdPrefix = "Enter tag name :";
                ui.currentComms = new FilterInput() {
                    @Override
                    public boolean isOk(String s) {
                        //ui.eng.doc.
                        //"^[^[a-zA-z_][^\s\<\>]*"
                        //"^(x|X)(m|M)(l|L)"
                        return true//ui.eng.db;  //To change body of implemented methods use File | Settings | File Templates.
                    }

                    @Override
                    public String getHelp() {
                        return "Enter new element name: ";  //To change body of implemented methods use File | Settings | File Templates.
                    }

                    @Override
                    public ParserCommand getCommand(String s) {
                        final String tagName = s;
                        return new ParserCommand() {
                            @Override
                            public void execute(UI ui) {
                                Element newElement = ui.eng.doc.createElement(tagName);
                                ui.eng.currentElement.appendChild(newElement);
                                ui.currentComms = ui.comms;
                                ui.cmdPrefix = ui.eng.currentElement.getNodeName()+" :";
                            }
                        };  //To change body of implemented methods use File | Settings | File Templates.
                    }
                };

            }
        },"add Description", this)
        );

        comms.put("select", new Command("select", new ParserCommand(){
                    public void execute(UI u)
                    {
                        final UI ui = u;
                //ui.eng.doc.getDocumentElement();
                    u.cmdPrefix = "Enter tag name :";
                    u.currentComms = new FilterInput() {
                    @Override
                    public boolean isOk(String s) {
                        return ui.eng.doc.getElementsByTagName(s).getLength() > 0;
                    }


                    @Override
                    public String getHelp() {
                        return "Enter tag name";  //To change body of implemented methods use File | Settings | File Templates.
                    }

                    @Override
                    public ParserCommand getCommand(String s) {
                        final String tagName = s;
                        return new ParserCommand() {
                            @Override
                            public void execute(UI ui) {
                                final NodeList nl = ui.eng.doc.getElementsByTagName(tagName);
                                if(nl.getLength()==1){
                                    ui.eng.currentElement = (Element) nl.item(0);
                                    ui.cmdPrefix = ui.eng.currentElement.getNodeName()+" :";
                                    ui.currentComms = ui.comms;
                                    return;
                                }
                                String toOut="";
                                for(int i = 0; i<nl.getLength(); i++)
                                {
                                    toOut +=i+") "+nl.item(i).getNodeName()+"\n";
                                }
                                System.out.println(toOut);
                                ui.cmdPrefix = "Enter tag number :";
                                ui.currentComms = new FilterInput() {
                                    @Override
                                    public boolean isOk(String s) {
                                        int n = -1;
                                        try{
                                            n = Integer.parseInt(s);
                                        }catch(NumberFormatException nfe){
                                            return false;
                                        }
                                        return n>-1 && n < nl.getLength();  //To change body of implemented methods use File | Settings | File Templates.
                                    }

                                    @Override
                                    public String getHelp() {
                                        return "Enter number";  //To change body of implemented methods use File | Settings | File Templates.
                                    }

                                    @Override
                                    public ParserCommand getCommand(String s) {
                                        final int num = Integer.parseInt(s);
                                        return new ParserCommand() {
                                            @Override
                                            public void execute(UI ui) {
                                                ui.eng.currentElement = (Element) nl.item(num);
                                                ui.currentComms = ui.comms;
                                                ui.cmdPrefix = ui.eng.currentElement.getNodeName()+" :";
                                            }
                                        };  //To change body of implemented methods use File | Settings | File Templates.
                                    }
                                };

                            }
                        };  //To change body of implemented methods use File | Settings | File Templates.
                    }
                };
            }
        },"select Description", this)
        );

        comms.put("delete", new Command("delete", new ParserCommand(){
            public void execute(UI u)
            {
                final UI ui = u;
                //ui.eng.doc.getDocumentElement();
                u.cmdPrefix = "Enter tag name :";
                u.currentComms = new FilterInput() {
                    @Override
                    public boolean isOk(String s) {
                        return ui.eng.doc.getElementsByTagName(s).getLength() > 0;
                    }


                    @Override
                    public String getHelp() {
                        return "Enter tag name";  //To change body of implemented methods use File | Settings | File Templates.
                    }

                    public boolean isParent(Node child, Node parent){
                        if(child.equals(parent))return true;
                        if(child.getParentNode()==null)return false;

                        return isParent(child.getParentNode(), parent);
                    }

                    @Override
                    public ParserCommand getCommand(String s) {
                        final String tagName = s;
                        return new ParserCommand() {
                            @Override
                            public void execute(UI ui) {
                                final NodeList nl = ui.eng.doc.getElementsByTagName(tagName);
                                if(nl.getLength()==1){
                                    if(isParent((Node) ui.eng.currentElement, nl.item(0)))
                                        ui.eng.currentElement = ui.eng.doc.getDocumentElement();
                                    ui.cmdPrefix = ui.eng.currentElement.getNodeName()+" :";
                                    ui.currentComms = ui.comms;
                                    nl.item(0).getParentNode().removeChild(nl.item(0));
                                    return;
                                }
                                String toOut="";
                                for(int i = 0; i<nl.getLength(); i++)
                                {
                                    toOut +=i+") "+nl.item(i).getNodeName()+"\n";
                                }
                                System.out.println(toOut);
                                ui.cmdPrefix = "Enter tag number :";
                                ui.currentComms = new FilterInput() {
                                    @Override
                                    public boolean isOk(String s) {
                                        int n = -1;
                                        try{
                                            n = Integer.parseInt(s);
                                        }catch(NumberFormatException nfe){
                                            return false;
                                        }
                                        return n>-1 && n < nl.getLength();  //To change body of implemented methods use File | Settings | File Templates.
                                    }

                                    @Override
                                    public String getHelp() {
                                        return "Enter number";  //To change body of implemented methods use File | Settings | File Templates.
                                    }

                                    @Override
                                    public ParserCommand getCommand(String s) {
                                        final int num = Integer.parseInt(s);
                                        return new ParserCommand() {
                                            @Override
                                            public void execute(UI ui) {
                                                ui.eng.currentElement = (Element) nl.item(num);
                                                ui.currentComms = ui.comms;
                                                ui.cmdPrefix = ui.eng.currentElement.getNodeName()+" :";
                                            }
                                        };  //To change body of implemented methods use File | Settings | File Templates.
                                    }
                                };

                            }
                        };  //To change body of implemented methods use File | Settings | File Templates.
                    }
                };
            }
        },"delete Description", this)
        );

        comms.put("docStruct", new Command("docStruct",new ParserCommand()
            {
                public void execute(UI ui)
                {
                    treeParse(ui.eng.doc.getDocumentElement(),0);
                }
                public void treeParse(Node nd, int tab)
                {
                    String space = "";
                    for(int i = 0; i<tab; i++)
                        space+="  ";

                    tab++;

                    System.out.print( space + nd.getNodeName());

                    if(eng.configs.isSet("va"))
                    {
                        for(int i=0; i<nd.getAttributes().getLength(); i++)
                        {
                            System.out.print(" "+nd.getAttributes().item(i).getNodeName()+"= \""
                                    +nd.getAttributes().item(i).getNodeValue()+"\"");
                        }
                    }

                    System.out.println();

                    for(int i=0; i<nd.getChildNodes().getLength(); i++)
                    {
                        if(nd.getChildNodes().item(i).getNodeName().compareTo("#text")==0 && eng.configs.isSet("tc") &&nd.getChildNodes().item(i).getNodeValue().trim().length()!=0){
                            System.out.println(space+"\""+nd.getChildNodes().item(i).getNodeValue()+"\"");
                            //System.out.println("before :'"+nd.getChildNodes().item(i).getNodeValue()+"' after :'"+nd.getChildNodes().item(i).getNodeValue().trim()+"'");
                        }  //?????

                        if(nd.getChildNodes().item(i).getNodeName()!="#text")
                            treeParse(nd.getChildNodes().item(i), tab);
                    }


                }
            },"docStruct Description", this)
        );

        comms.put("save" , new Command("save", new ParserCommand() {
            @Override
            public void execute(UI ui) {
                try{
                    FileWriter fw = new FileWriter(ui.xmlFile);
                    fw.write(writeTree(ui.eng.doc.getDocumentElement(), 0));
                    fw.flush();
                    fw.close();
                }
                catch(IOException ioe){
                    ioe.printStackTrace();
                }

            }
            public String writeTree(Node nd, int tab)
            {
                String space = "";
                for(int i = 0; i<tab; i++)
                    space+="\t";
                String s="";
                if(tab==0)
                    s+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";

                s+=space;
                s+="<"+nd.getNodeName();
                for(int i=0; i<nd.getAttributes().getLength(); i++)
                {
                    s+=" "+nd.getAttributes().item(i).getNodeName()+"= \""
                            +nd.getAttributes().item(i).getNodeValue()+"\"";
                }
                s+=">";
                if(nd.hasChildNodes())
                {
                    if(nd.getFirstChild().getNodeName().compareTo("#text")!=0 || nd.getFirstChild().getNodeValue().trim().length()==0)
                        s+="\n";
                    NodeList tmplist = nd.getChildNodes();
                    for(int i=0; i<tmplist.getLength(); i++)
                    {

                        if(tmplist.item(i).getNodeName().compareTo("#text")==0 &&tmplist.item(i).getNodeValue().trim().length()!=0){
                            s+=tmplist.item(i).getNodeValue();

                        }

                        if(tmplist.item(i).getNodeName()!="#text")
                        {

                            s+=writeTree(tmplist.item(i), tab+1);
                        }

                    }
                    //s+=">";
                }
                 //else
                     //s+=">";
                if(s.lastIndexOf("\n")==s.length()-1)
                    s+=space;
                s+="</"+nd.getNodeName()+">\n";

                return s;
            }
        }, "save description",this));

        //System commands

        sysComms.put("help", new Command("help",new ParserCommand()
            {
                public void execute(UI ui)
                {
                    System.out.println(ui.currentComms.getHelp());
                    System.out.println(ui.sysComms.getHelp());
                }
            },"help Description", this)
        );

        sysComms.put("exit", new Command("exit", new ParserCommand()
            {
                public void execute(UI ui)
                {
                    System.exit(0);

                }
            },"exit Description", this)
        );

        sysComms.put("close", new Command("close", new ParserCommand()
        {
            public void execute(UI ui)
            {


                ui.eng.doc = null;
                ui.eng.XMLTextSource=null;
                ui.eng.configs = new Cfg(ui);
                ui.xmlFile=null;
                ui.eng.currentElement=null;
                ui.currentComms =ui.eng.configs;
                System.out.println("File closed.");
                ui.cmdPrefix = "Enter file name with parameters :";
            }
        },"close Description", this)
        );

    }


    public void setEngine(Engine link)
    {
        eng = link;
    }


    public void run()
    {
        if(eng==null)
        {
            System.out.println("NO ENG");
            System.exit(0);

        }
        else
        {
            try{
                eng.configs = new Cfg(this);
                System.out.print("Enter file name and arguments. Possible arguments: ");
                System.out.print(eng.configs);
                System.out.println("Note: case sensitive!");
                /*
                String s = in.readLine();

                while(parseCfg(s)<0)
                {
                 System.out.println("File not found");
                 s = in.readLine();
                        //eng.configs = new Cfg(S.split(" -"))

                }*/
                currentComms = eng.configs;
                cmdPrefix = "Enter file name with parameters :";

                while(true)
                {
                    getCommand(currentComms).execute(this);
                }

            }
            catch(Exception e)
            {
                System.out.println("UI Exception");
                e.printStackTrace();
            }                        //

        }
    }

    public ParserCommand getCommand(FilterInput m)
    {

        String s="";
        while(true)
        {
            try
            {
                System.out.print(cmdPrefix);
                s = in.readLine();
            }
            catch(IOException ioe)
            {
                ioe.printStackTrace();
            }
            if(m.isOk(s))
            {
                return m.getCommand(s);
            }
            if(sysComms.isOk(s)){
                return sysComms.getCommand(s);
            }
            else
            {
                System.out.println("Command not found, type \"help\" to get list of possible commands");
            }
        }
        //return m.getCommand(s);
    }

}

class Cfg implements FilterInput
{
    private NavigableMap<String, String> commands;
    UI ui;

    Cfg(UI u)
    {
        commands = new TreeMap<String, String>();
        commands.put("va","viewAttributes");
        commands.put("tc","textContent");
        ui=u;
    }
    void actualize(String[] argarray)
    {
        if (argarray.length==0)
        {
            commands.clear();
            return;
        }
        for (String tmp_str : argarray) {
            while (tmp_str.compareTo(commands.floorKey(tmp_str)) != 0)
                commands.remove(commands.floorKey(tmp_str));
        }
        while(commands.higherKey(argarray[argarray.length-1])!=null)
            commands.remove(commands.higherKey(argarray[argarray.length-1]));
    }
    public boolean isSet(String key)
    {
        return commands.containsKey(key);
    }
    public String toString()
    {
        String s = "";
        for (Map.Entry<String, String> entry : commands.entrySet())
        {
            s+=entry.getKey()+", "; //+ " - " + entry.getValue() +"\n";
        }
        return s+"\n";
    }

    public boolean isOk(String s)
    {
        File tmpfile = new File (s);
        if(tmpfile.isFile())
        {
            ui.xmlFile = tmpfile;
            ui.cfgComms = new String[0];
            return true;
        }
        else
        {
            String[] m  = s.trim().split(".xml");
            String tmpstr = "";
            for(int i = 0; i<m.length-1;i++)
            {
                tmpstr+=m[i]+".xml";
            }
            tmpfile = new File (tmpstr);
            if(tmpfile.isFile())
            {
                ui.xmlFile = tmpfile;

                ui.cfgComms = m[m.length-1].split(" -");

                List<String> list = Arrays.asList(ui.cfgComms);
                list = list.subList(1,list.size());
                ui.cfgComms = new String[0];
                ui.cfgComms = list.toArray(ui.cfgComms);
                Arrays.sort(ui.cfgComms);

                return true;
            }
            else
            {
                //System.out.println("Incorrect file name");
                return false;

            }


        }


    }
    public String getHelp()
    {
        Set<Map.Entry<String, String>> set = commands.entrySet();
        java.lang.String s = "";
        for (Map.Entry<String, String> entry : set)
        {

            s+=entry.getKey()+" - "+ entry.getValue()+"\n";
        }
        return s;
    }
    public ParserCommand getCommand(java.lang.String s)
    {
        return new ParserCommand(){
            public void execute(UI ui){
                ui.eng.configs.actualize(ui.cfgComms);
                try{
                    ui.eng.XMLTextSource = new FileInputStream(ui.xmlFile);
                }catch(IOException e){
                    e.printStackTrace();
                    System.exit(0);
                }
                ui.eng.interrupt();
                ui.currentComms = ui.comms;
                while(ui.eng.currentElement==null)
                {
                    try{
                        Thread.sleep(100);
                    }catch(InterruptedException ie){
                        ie.printStackTrace();
                    }
                }

                ui.cmdPrefix = ui.eng.currentElement.getNodeName()+" :";
            }
        };
    }


}
class Command
{
    String name;
    ParserCommand commandCode;
    String desc;
    UI ui;

    Command(String nm, ParserCommand cc, UI uint)
    {
        name = nm;
        commandCode =cc;
        desc = "";
        ui = uint;
    }
    Command(String nm, ParserCommand cc, String d, UI uint)
    {
        name = nm;
        commandCode =cc;
        desc = d;
        ui = uint;
    }
    public void execute()
    {
        commandCode.execute(ui);
    }
}
class CommandMap extends HashMap<String, Command> implements FilterInput
{
    public boolean isOk(java.lang.String s)
    {
        return this.containsKey(s);
    }
    public java.lang.String getHelp()
    {
        Set<Map.Entry<String, XMLParser.Command>> set = this.entrySet();
        java.lang.String s = "";
        for (Map.Entry<String, XMLParser.Command> entry : set)
        {

            s+=entry.getKey()+" - "+ entry.getValue().desc+"\n";
        }
        return s;
    }
    public XMLParser.ParserCommand getCommand(java.lang.String s)
    {
        XMLParser.Command tmp = (XMLParser.Command) this.get(s);
        return (XMLParser.ParserCommand) tmp.commandCode;
    }
}
interface ParserCommand
{
    public void execute(UI ui);
}

interface FilterInput
{
    public boolean isOk(String s);
    public String getHelp();
    public ParserCommand getCommand(String s);

}

