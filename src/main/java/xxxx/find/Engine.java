package xxxx.find;

import org.apache.commons.cli.ParseException;
import xxxx.find.utils.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Engine {
    public Config config;

    public Command command = new Command();
    private Neo4jUtil neo4jUtil;
    private final int DEPTH = 5;

    private Map<String, String> tempSink = new HashMap<>();

    Set<String> nodes = Collections.synchronizedSet(new HashSet<>());
    Set<String> links = Collections.synchronizedSet(new HashSet<>());

    private void parseCommand(String[] args) throws ParseException {
        command.parse(args);
    }


    public static void main(String[] args) throws Exception {
        Engine engine = new Engine();
        engine.run(args);
    }


    public void run(String[] args) throws Exception {
        parseCommand(args);
        loadConfig(command);
        loadAllSinks();
        initNeo4jUtils();
        while (true) {
            System.out.println("------------");
            System.out.println("指令：");
            System.out.println("exit\t退出程序");
            System.out.println("search\t进行搜索");
            System.out.println("print\t打印sink映射关系");
            System.out.println("------------");
            Scanner sc = new Scanner(System.in);
            String cmd = sc.next();
            switch (cmd) {
                case "exit":
                    System.exit(1);
                case "search":
                    search0();
                    break;
                case "print":
                    printAllSinks();
                    break;
                default:
                    System.out.println(String.format("%s 命令无法识别", cmd));
                    break;
            }
        }
    }

    private void loadAllSinks() {
        AtomicInteger num = new AtomicInteger();
        config.getSinks().forEach((key, value) -> {
            value.forEach(s -> {
                tempSink.put(String.valueOf(num.get()), key + "@" + s);
                num.getAndIncrement();
            });
        });
    }

    private void printAllSinks() {
        tempSink.forEach((key, value) -> {
            System.out.println(key + ": " + value);
        });
    }

    private void loadConfig(Command command) {
        try {
            String sinksPath = command.getConfigPath();
            config = (Config) YamlUtil.readYaml(sinksPath, Config.class);
        } catch (Exception e) {
            System.out.println("加载配置文件失败！！！");
            System.exit(1);
        }
    }

    private void initNeo4jUtils() {
        neo4jUtil = new Neo4jUtil(config);
    }

    private void search0() {
        try {
            System.out.println("请选择sink序号");
            printAllSinks();
            Scanner sc = new Scanner(System.in);
            String cmd = sc.next();
            if (tempSink.containsKey(cmd)) {
                String value = tempSink.get(cmd);
                System.out.println("你选择了：" + value);
                search(value.split("@")[1]);
                return;
            }
            System.out.println(String.format("序号为%s的sink不存在！", cmd));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void search(String sink) throws Exception {
        String query = String.format("Match (n) where n.signature contains \"%s\" return n",
                sink);
        List<Node> results = neo4jUtil.searchNode(query);
        if (results.size() == 0) {
            System.out.println("未找到相关sink");
            return;
        }
        for (Node n :
                results) {
            recurseSearch(n, DEPTH);
            drawGraph();
            nodes.clear();
            links.clear();
        }
    }

    private void recurseSearch(Node childrenNode, int depth) {
        String childrenNodeSignature = (String) childrenNode.getProperties().get("signature");
        nodes.add(childrenNodeSignature);
        if (depth == 0) return;
        String query = String.format("match (a) -[r]-> (b:node{signature: \"%s\"}) return a",
                childrenNode.getProperties().get("signature"));
        List<Node> parentNodes = neo4jUtil.searchNode(query);
        parentNodes.forEach(n -> {
            String parentNodeSignature = (String) n.getProperties().get("signature");
            nodes.add(parentNodeSignature);
            links.add(String.format(" %s @ %s", parentNodeSignature, childrenNodeSignature));
            recurseSearch(n, depth - 1);
        });
    }

    private void drawGraph() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer = new StringBuffer(
                "digraph CallGraph{\n" +
                        "node [fontsize=\"20\",];\n" +
                        "edge [fontsize=\"10\",];\n");

        System.out.println("_______________________________________________");
        Map<String, String> nodeNum = new HashMap<>();
        Map<String, String> numToNode = new HashMap<>();
        Set<String> entries = new HashSet<>();
        Set<String> children = new HashSet<>();
        int num = 0;
        for (String node :
                nodes) {
            nodeNum.put(node, String.valueOf(num));
            numToNode.put(String.valueOf(num), node);
            entries.add(String.valueOf(num));
            stringBuffer.append(String.format(" %d[label=\"%s\", shape=\"box\"];\n", num, node));
            num++;
        }
        for (String link :
                links) {
            String[] links = link.split("@");
            String parent = links[0].trim();
            String child = links[1].trim();
            String parentNum = nodeNum.get(parent);
            String childNum = nodeNum.get(child);
            children.add(childNum);
            stringBuffer.append(String.format(" %s -> %s [label=\"%s\"];\n", parentNum, childNum, "call"));
        }
        stringBuffer.append("}");
        System.out.println(stringBuffer.toString());
        System.out.println("_______________________________________________");
    }
}
