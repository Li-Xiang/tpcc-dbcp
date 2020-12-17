package org.littlestar.tpcc;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.logging.log4j.Level;

import org.littlestar.helper.DataSourceHelper;
import org.littlestar.helper.DataSourceHelper.DBMS;
import org.littlestar.helper.Log4jHelper;

public class Tpcc implements TpccConstants {
	
	////////////
	private static final String HELP_OPTION         = "help";
	private static final String DATA_SOURCE_OPTION  = "datasource";
	private static final String WARMUP_TIME_OPTION  = "warmup";
	private static final String THREADS_OPTION      = "threads";
	private static final String WAREHOUSES_OPTION   = "warehouses";
	private static final String INTERVAL_OPTION     = "report-interval";
	private static final String MEASURE_TIME_OPTION     = "measure-time";
	private static final String LOG_LEVEL_OPTION    = "log-level";
	
	
	public static final String LOAD_COMMAND         = "load";
	public static final String CHECK_COMMAND        = "check";
	public static final String RUN_COMMAND          = "run";
	
	private static final Options options = setupOptions();
	
	private static Options setupOptions() {
	    Options options = new Options();
	    options.addOption(Option.builder("h").longOpt(HELP_OPTION).desc("Print this help and exit.").build());
	    options.addOption(Option.builder("d").longOpt(DATA_SOURCE_OPTION).hasArg().argName("name").desc("Specify datasource name which define in \"dsConfig.json\" config fine, default is 'default-data-source'.").build());
	    options.addOption(Option.builder("w").longOpt(WAREHOUSES_OPTION).hasArg().argName("#").desc("Specifies the number of warehouse, default is tpcc.warehouse table's rows number.").build());
	    options.addOption(Option.builder("t").longOpt(THREADS_OPTION).hasArg().argName("#").desc("Benchmark worker threads. ").build());
	    options.addOption(Option.builder("r").longOpt(WARMUP_TIME_OPTION).hasArg().argName("#").desc("Rampup(warmup) time in seconds, default 10 seconds.").build());
	    options.addOption(Option.builder("m").longOpt(MEASURE_TIME_OPTION).hasArg().argName("#").desc("Total execution time in seconds. default 3600 seconds").build());
	    options.addOption(Option.builder("i").longOpt(INTERVAL_OPTION).hasArg().argName("#").desc("Periodically report intermediate statistics with a specified interval in seconds. set to 0 disables intermediate reports").build());
	    options.addOption(Option.builder("l").longOpt(LOG_LEVEL_OPTION).hasArg().argName("level").desc("Set the tpcc benchmark's logging level {ALL|TRACE|DEBUG|INFO|WARN|ERROR|FATAL|OFF}.").build());
	    return options;
	}
    
	private static void showHelp(Options options) {
		String cmdLineSyntax = "tpcc [options] [command]\n" ;
		String header = "\noptions: \n";
		String footer = "\ncommand: \n  The command is an optional argument, the following commands are understood: \n"
				+ "    load: Generate the test data for TPC-C benchmarks.\n"
				+ "    check: Check the test data for TPC-C benchmarks. \n"
				+ "    run : Runs the actual TPC-C test. (default)\n"
				+ "\nexamples: \n"
				+ "  tpcc --datasource mysql-ds-1 --warehouses 10 --threads 20 load\n"
				+ "  tpcc --datasource mysql-ds-1 check\n"
				+ "  tpcc --datasource mysql-ds-1 --warehouses 10 --warmup 60 --threads 10 --log-level error --measure-time 3600 --report-interval 10 run \n";
				
        HelpFormatter formatter = new HelpFormatter();
        formatter.setWidth(120);
        formatter.printHelp(cmdLineSyntax, header, options, footer);
    }
	////////////
	
	private static void start(CommandLine cli) throws Throwable {
		////
		String dataSourceName = "default-data-source";
		int warmup         = 60;
		int warehouses     = 5;
		int threads        = 1;
		int measureTime    = 3600;
		int reportInterval = 20;
		Level level    = Level.ERROR;
		String command = RUN_COMMAND;
		
		if (cli.hasOption(HELP_OPTION)) {
			showHelp(options);
			System.exit(0);
		}
		
		String inputDataSourceName = "";
		if (cli.hasOption(DATA_SOURCE_OPTION)) {
			inputDataSourceName = cli.getOptionValue(DATA_SOURCE_OPTION).trim();
		}
		
		String inputWarehouses = "";
		if (cli.hasOption(WAREHOUSES_OPTION)) {
			inputWarehouses = cli.getOptionValue(WAREHOUSES_OPTION);
			try {
				int tmp = Integer.parseInt(inputWarehouses);
				warehouses = tmp;
			} catch (Throwable e) {
			}
		}
		
		String inputThreads = "";
		if (cli.hasOption(THREADS_OPTION)) {
			inputThreads = cli.getOptionValue(THREADS_OPTION).trim();
			try {
				int tmp = Integer.parseInt(inputThreads);
				if (tmp > 0) {
					threads = tmp;
				}
			} catch (Throwable e) {
			}
		}
		
		String inputWarmup = "";
		if (cli.hasOption(WARMUP_TIME_OPTION)) {
			inputWarmup = cli.getOptionValue(WARMUP_TIME_OPTION);
			try {
				int tmp = Integer.parseInt(inputWarmup);
				if (tmp >= 0) {
					warmup = tmp;
				}
			} catch (Throwable e) {
			}
		}
		
		String inputMeasureTime = "";
		if (cli.hasOption(MEASURE_TIME_OPTION)) {
			inputMeasureTime = cli.getOptionValue(MEASURE_TIME_OPTION);
			try {
				int tmp = Integer.parseInt(inputMeasureTime);
				if (tmp > 0) {
					measureTime = tmp;
				}
			} catch (Throwable e) {
			}
		}
		
		String intputInterval = "";
		if (cli.hasOption(INTERVAL_OPTION)) {
			intputInterval = cli.getOptionValue(INTERVAL_OPTION);
			try {
				int tmp = Integer.parseInt(intputInterval);
				if (tmp > 1) {
					reportInterval = tmp;
				}
			} catch (Throwable e) {
			}
		}
		
		String inputLogLevel = "";
		if (cli.hasOption(LOG_LEVEL_OPTION)) {
			inputLogLevel = cli.getOptionValue(LOG_LEVEL_OPTION);
			level = Level.toLevel(inputLogLevel, level);
		}
		
		String inputCommand = "";
		if (cli.getArgs().length > 0) {
			inputCommand = cli.getArgs()[0];
			if (inputCommand.trim().equalsIgnoreCase(LOAD_COMMAND)) {
				command = LOAD_COMMAND;
			} else if (inputCommand.trim().equalsIgnoreCase(CHECK_COMMAND)) {
				command = CHECK_COMMAND;
			}
		}

		System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.Log4JLogger");
		Log4jHelper.newHelper(Level.ALL).withConsoleAppender(level).init();
		final Log logger = LogFactory.getLog(Tpcc.class);
		
		logger.debug("Command line input -> Command: " + command
				+ " -> Option: datasource = "+ inputDataSourceName 
				+ "; warmup = "+ inputWarmup 
				+ "; warehouses = " + inputWarehouses 
				+ "; threads = " + inputThreads
				+ "; measure-time =" + inputMeasureTime
				+ "; report-interval = " + intputInterval 
				+ "; log-level = " + inputLogLevel);
		////
		
		StringBuilder message = new StringBuilder();
		message.append("******************************************************************\n")
		       .append("***           TPC-C Load Generator (for Apache DBCP)           ***\n")
		       .append("******************************************************************\n");
		TpccHelper.output(message, false);
		message.setLength(0);
		
		if(inputDataSourceName.length() > 0) {
			dataSourceName = inputDataSourceName;
		}
		String dsConfig = USER_DIR + "dataSource.json";
		DataSourceHelper dsHelper = new DataSourceHelper(new File(dsConfig));
		BasicDataSource dataSource = dsHelper.getDataSource(dataSourceName);
		DBMS dbms = dsHelper.getDbms(dataSourceName);
		if (dataSource == null)
			throw new Throwable("Datasource '" + dataSourceName + "' not found in +'" + dsConfig + "'.");
		TpccContext.initial(dataSource, dbms);
		if (warehouses < 1) warehouses = 1;
		if(command.equalsIgnoreCase(LOAD_COMMAND)) {
			TpccContext.getContext().setWarehouses(warehouses);
			message.append("Loading the test data with the following options:")
			       .append("\n  [data-source]  : ").append(dataSourceName)
			       .append("\n  [threads]      : ").append(threads)
			       .append("\n  [connection]   : ").append(dataSource.getMaxTotal()).append(" (max.)")
			       .append("\n  [warehouse]    : ").append(warehouses)
			       .append("\n  [log-level]    : ").append(level.toString().toLowerCase())
			       .append("\n......"); 
			TpccHelper.output(message);
			message.setLength(0);
			TpccLoad.load(warehouses, threads);
			TpccHelper.check();
		} else if (command.equalsIgnoreCase(CHECK_COMMAND)) {
			try {
				TpccHelper.check();
			} catch (Throwable e) {
				logger.error("Check TPC-C Tables failed ......", e);
			}
		} else {
			int wareRows = TpccHelper.checkWarehouse();
			if (wareRows < 1) {
				throw new Throwable("Warehouse's table is empty (rows = 0), please load data.");
			}
			
			// 跑测试场景中, 指定的仓库数量不能超过数据库中实际的仓库数量, 且数据库仓库表不能为空.
			warehouses = Math.min(wareRows, warehouses);
			TpccContext.getContext().setWarehouses(warehouses);
			message.append("Running the test with the following options:")
			       .append("\n  [data-source]     : ").append(dataSourceName)
			       .append("\n  [warehouse]       : ").append(warehouses)
			       .append("\n  [rampup]          : ").append(warmup).append(" (sec.)")
			       .append("\n  [threads]         : ").append(threads)
			       .append("\n  [connection]      : ").append(dataSource.getMaxTotal()).append(" (max.)")
			       .append("\n  [report-interval] : ").append(reportInterval).append(" (sec.)")
			       .append("\n  [measure-time]    : ").append(measureTime).append(" (sec.)")
			       .append("\n  [log-level]       : ").append(level.toString().toLowerCase())
			       .append("\n......");
			TpccHelper.output(message);
			message.setLength(0);
			TpccTransaction.run(threads, warmup, reportInterval, measureTime);
		}
	}
	
	public static void main(String[] args) {
		final CommandLineParser cliParser = new DefaultParser();
		final Options options = setupOptions();
		try {
			CommandLine cli = cliParser.parse(options, args);
			start(cli);
		} catch (Throwable e) {
			final Log logger = LogFactory.getLog(Tpcc.class);
			logger.fatal("Starting tpcc-dbcp benchmark failed.", e);
		}
	}
}
