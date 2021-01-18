package org.littlestar.helper;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.appender.rolling.DefaultRolloverStrategy;
import org.apache.logging.log4j.core.appender.rolling.SizeBasedTriggeringPolicy;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;

public class Log4jHelper {
	final LoggerContext context = (LoggerContext) LogManager.getContext(false);
	
	final Charset charset = StandardCharsets.UTF_8;
	//PatternLayout layout = PatternLayout.newBuilder().withPattern("[%d{yyyy-MM-dd HH:mm:ss.SSS}] [%-5level] %logger{36} (%M:%L): %msg%n%ex").build();
	//PatternLayout layout = PatternLayout.newBuilder().withPattern("[%d{yyyy-MM-dd HH:mm:ss.SSSS}] [%-5level] %logger{36}.%L: %msg%n%ex").build();
	PatternLayout layout = PatternLayout.newBuilder().withPattern("%d{yyyy-MM-dd HH:mm:ss.SSSS} %c{1}.%L [%-5level]: %msg%n%ex").build();
	final Level level = Level.ERROR;
	
	final String maxFileSize = "8 MB";
	final String maxFilesKeep = "5";
	
	private Log4jHelper(File config) {
		//clean root logger appenders. 
		final Configuration configuration = context.getConfiguration();
		final LoggerConfig loggerConfig = configuration.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
		for (String key : loggerConfig.getAppenders().keySet()) {
			loggerConfig.removeAppender(key);
		}
		
		if ((config != null) && config.exists()) {
			context.setConfigLocation(config.toURI());
			context.reconfigure();
		} else {
			context.getConfiguration().getLoggerConfig(LogManager.ROOT_LOGGER_NAME).setLevel(Level.ALL);
		}
	}
	
	public static Log4jHelper newHelper() {
		return new Log4jHelper(null);
	}
	
	public static Log4jHelper newHelper(Level level) {
		return newHelper(level, null);
	}
	
	public static Log4jHelper newHelper(Level level, PatternLayout layout) {
		Log4jHelper helper = new Log4jHelper(null);
		if (layout != null) {
			helper.layout = layout;
		}
		if (level != null) {
			helper.setRoolLevel(level);
		}
		return helper;
	}
	
	public static Log4jHelper newHelper(File config) {
		return new Log4jHelper(config);
	}
	
	public Log4jHelper setRoolLevel(Level level) {
		context.getConfiguration().getLoggerConfig(LogManager.ROOT_LOGGER_NAME).setLevel(level);
		return this;
	}
	
	public Log4jHelper withConsoleAppender(Level level) {
		return withConsoleAppender(level, layout);
	}
	
	public Log4jHelper withConsoleAppender(Level level, PatternLayout layout) {
		if (level == null) {
			level = this.level;
		}
		if (layout == null) {
			layout = this.layout;
		}
		final Configuration configuration = context.getConfiguration();
		final LoggerConfig loggerConfig = configuration.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
		ConsoleAppender consoleAppender = ConsoleAppender.newBuilder().setName("ConsoleAppender").setLayout(layout)
				.build();
		consoleAppender.start();
		configuration.addAppender(consoleAppender);
		loggerConfig.addAppender(consoleAppender, level, null);
		return this;
	}
	
	public Log4jHelper withRollingFileAppender(Level level, PatternLayout layout, String logFile, String fileSize, String maxFilesKeep) {
		if (level == null) {
			level = this.level;
		}
		if (layout == null) {
			layout = this.layout;
		}
		final Configuration configuration = context.getConfiguration();
		final LoggerConfig loggerConfig = configuration.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
		RollingFileAppender fileAppender = RollingFileAppender.newBuilder().setName("RollingFileAppender")
				.withAppend(true)
				//.withBufferedIo(true)
                //.withBufferSize(8192)
				//.withLocking(false)
				.setLayout(layout)
				.withPolicy(SizeBasedTriggeringPolicy.createPolicy(fileSize))
				.withStrategy(DefaultRolloverStrategy.newBuilder().withMax(maxFilesKeep).withFileIndex("min").withConfig(configuration).build())
				.withFilePattern(logFile+".%i")
				.withFileName(logFile).build();
		fileAppender.start();
		configuration.addAppender(fileAppender);
		loggerConfig.addAppender(fileAppender, level, null);
		return this;
	}
	
	public Log4jHelper withRollingFileAppender(Level level, String logFile, String fileSize, String maxFilesKeep) {
		return withRollingFileAppender(level, layout, logFile, fileSize, maxFilesKeep);
	}
	
	public Log4jHelper withRollingFileAppender(Level level, String logFile) {
		return withRollingFileAppender(level, layout, logFile, maxFileSize, maxFilesKeep);
	}
	
	public void setup() {
		context.updateLoggers();
	}
}
