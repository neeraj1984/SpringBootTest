package com.lh.dandi.coe;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AppProperties {
	
	@Value("${ftp.location}")
	public  String ftpLocation;
	
	@Value("${ftp.user}")
	public  String ftpUser;
	
	@Value("${ftp.password}")
	public  String password;
	
	@Value("${csv.file.name}")
	public  String csvFileName;
	
	@Value("${ftp.port}")
	public  Integer ftpPort;
	
	@Value("${ftp.full.location}")
	public  String ftpFullLocation;
	
	@Value("${ftp.input.file.directory}")
	public  String ftpInputFileDirectory;
	
	@Value("${archival.directory}")
	public  String archivalDirectory;
	
	@Value("${source.directory}")
	public  String sourceDirectory;
	
	@Value("${source.directory_2}")
	public String sourceDirectory2;
	
	@Value("${rabbitMQ.host}")
	public  String rabbitMQHost;
	
	@Value("${rabbitMQ.user}")
	public  String rabbitMQUserName;	
	
	@Value("${rabbitMQ.password}")
	public  String rabbitMQPassword;
	
	@Value("${rabbitMQ.queue.name}")
	public  String rabbitMQQueueName;
	
	@Value("${rabbitMQ.exchange.name}")
	public  String rabbitMQExchangeName;
	
	@Value("${rabbitMQ.port}")
	public  Integer rabbitMQPort;
	
	@Value("${rabbitMQ.routing.key}")
	public  String rabbitMQRoutingKey;
}
