package xxxx.find.utils;


import org.apache.commons.cli.*;


public class Command {

    public String getConfigPath() {
        return configPath;
    }

    public void setConfigPath(String configPath) {
        this.configPath = configPath;
    }

    private String configPath;


    public Command() {
    }

    public void parse(String[] args) throws ParseException {
        CommandLineParser parser = new BasicParser();
        Options options = new Options();
        options.addOption("h", "help", false, "打印命令行帮助信息");
        options.addOption("cp", "config-path", true, "配置文件地址");

        CommandLine commandLine = parser.parse(options, args);
        HelpFormatter helpFormatter = new HelpFormatter();
        if (commandLine.hasOption("h")) {
            helpFormatter.printHelp("java -jar Find.jar", options, true);
            System.exit(0);
        }

        if (commandLine.hasOption("cp")) {
            this.setConfigPath(commandLine.getOptionValue("cp"));
        }
    }
}
