
import org.apache.commons.cli.*;

/**
 * CLI
 * <p>
 *
 * Parse and verify command line arguments, displays help content on error
 *
 * @author Sebastien Roux
 * @mail roux.sebastien@gmail.com
 */
public class CLI {

    private String[] arguments;

    public String[] getArgs() {
        return arguments;
    }

    public void setArgs(String[] arguments) {
        this.arguments = arguments;
    }

    /**
     * Define and parse command line arguments Jakarta CLI library
     */
    @SuppressWarnings("static-access")
    public void parseArgs() {

        // Define options
        Options options = new Options();

        // add user option
        Option user = OptionBuilder.isRequired().withArgName("user").withLongOpt("essusr").hasArg().withDescription("user login").create("u");
        options.addOption(user);

        // add password option
        Option password = OptionBuilder.isRequired().withArgName("password").withLongOpt("esspwd").hasArg().withDescription("password").create("p");
        options.addOption(password);

        // add server option
        Option server = OptionBuilder.isRequired().withArgName("server").withLongOpt("esssvr").hasArg().withDescription("Essbase server").create("s");
        options.addOption(server);

        // add provider url option
        Option provider = OptionBuilder.withArgName("http://ProviderServer:port/aps/JAPI").withLongOpt("essaps").hasArg().withDescription("Provider server URL - if not specified default URL is used http://EssbaseServer:13080/aps/JAPI").create("v");
        options.addOption(provider);

        // add application option
        Option application = OptionBuilder.isRequired().withArgName("application").withLongOpt("essapp").hasArg().withDescription("application name").create("a");
        options.addOption(application);

        // add database option
        Option database = OptionBuilder.isRequired().withArgName("database").withLongOpt("essdb").hasArg().withDescription("database name").create("d");
        options.addOption(database);

        //add indent option
        Option indent = OptionBuilder.withArgName("indent").withLongOpt("indent").withDescription("indent output").create("i");
        options.addOption(indent);

        //add dimension option
        Option dimension = OptionBuilder.withArgName("dimension").withLongOpt("dimension").hasArg().withDescription("dimension name").create("m");
        options.addOption(dimension);

        //add delimiter option
        Option delimiter = OptionBuilder.withArgName("delimiter").withLongOpt("delimiter").hasArg().withDescription("delimiter string - default is tab").create("e");
        options.addOption(delimiter);

        // add help option
        Option help = OptionBuilder.withArgName("help").withLongOpt("help").withDescription("display this help").create("h");
        options.addOption(help);

        // Parse commande line arguments
        CommandLine cmd;

        try {
            ReadOutline readEssOtl = new ReadOutline();

            CommandLineParser parser = new GnuParser();

            cmd = parser.parse(options, arguments);

            // Help
            if (cmd.hasOption("h")) {
                displayHelp(options);
            }

            // User
            if (cmd.hasOption("u")) {
                readEssOtl.setEssUsr(cmd.getOptionValue("u"));
            } else {
                System.out.println("Please specify a user!");
                displayHelp(options);
            }

            //Password
            if (cmd.hasOption("p")) {
                readEssOtl.setEssPwd(cmd.getOptionValue("p"));
            } else {
                System.out.println("Please specify a password!");
                displayHelp(options);
            }

            //Server
            if (cmd.hasOption("s")) {
                readEssOtl.setEssSvr(cmd.getOptionValue("s"));
            } else {
                System.out.println("Please specify an Essbase server!");
                displayHelp(options);
            }

            //Provider url if different or specific server
            readEssOtl.setEssProvider(cmd.getOptionValue("v"));

            //Application
            if (cmd.hasOption("a")) {
                readEssOtl.setEssApp(cmd.getOptionValue("a"));
            } else {
                System.out.println("Please specify an application!");
                displayHelp(options);
            }

            //Database
            if (cmd.hasOption("d")) {
                readEssOtl.setEssDb(cmd.getOptionValue("d"));
            } else {
                System.out.println("Please specify a database!");
                displayHelp(options);
            }

            //Indent
            if (cmd.hasOption("i")) {
                readEssOtl.setIndent(true);
            }

            //Dimension
            if (cmd.hasOption("m")) {
                readEssOtl.setDimension(cmd.getOptionValue("m"));
            }

            //Delimiter
            if (cmd.hasOption("e")) {
                readEssOtl.setDelimiter(cmd.getOptionValue("e"));
            }

            readEssOtl.ReadOutline();

        } // if arguments missing for specified options(user, password, server...)
        catch (ParseException e) {
            displayHelp(options);
        }
    }

    /**
     * Display usage and help
     */
    private final void displayHelp(Options options) {

        final String HELP_DESC = "DESCRIPTION:\n" + "Displays outline properties: "
                + "parent, member, alias for each alias table, children count, "
                + "consolidation, formula, level, generation, UDAs, attributes and comments" + "\n";

        final String HELP_REQU = "REQUIREMENTS:\n" + "JRE 1.5 or higher (current JRE version: " + JavaVersionDisplayApplet() + ")\n";

        final String HELP_VERS = "VERSION:\n" + "version 1.2\n";

        final String HELP_AUTH = "AUTHOR:\n" + "Sebastien Roux <roux.sebastien@gmail.com>\n";

        final String HELP_NOTE = "NOTES:\n" + "Use at your own risk!\n"
                + "You will be solely responsible for any damage\n"
                + "to your computer system or loss of data\n"
                + "that may result from the download\n"
                + "or the use of the following application.\n";

        HelpFormatter formatter = new HelpFormatter();

        formatter.printHelp("java -jar ReadOutline.jar [OPTIONS]",
                HELP_DESC + "OPTIONS:\n", options, HELP_REQU + HELP_VERS
                + HELP_AUTH + HELP_NOTE);
        System.exit(0);
    }

    public static String JavaVersionDisplayApplet() {
        String jVersion = System.getProperty("java.version");
        return jVersion;
    }

    public static void main(String[] args) {
        CLI verifyArguments = new CLI();
        verifyArguments.setArgs(args);
        verifyArguments.parseArgs();
    }
}
