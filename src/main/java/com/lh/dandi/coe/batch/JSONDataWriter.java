package com.lh.dandi.coe.batch;

import java.io.IOException;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lh.dandi.coe.AppProperties;
import com.lh.dandi.coe.bean.JSONDataBean;
import com.lh.dandi.coe.bean.MemberDataFileBean;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

@Component
public class JSONDataWriter implements ItemWriter<MemberDataFileBean>, StepExecutionListener{
	
	public final static Logger logger = LoggerFactory.getLogger(JSONDataWriter.class.getName());
	String start = null;
	DateTime end = null;
	int counter = 0;
	
	ExecutionContext jobExecutionContext = null;
	
	protected Channel channel;
    protected Connection connection;
    protected String endPointName;
    
    String userName;
    String password;
    String queueName;
    String exchangeName;
    String routingKey;
    String hostName;
    Integer port;
    Map<String, Object> headers = new HashMap<String, Object>();
    
	@Autowired
	AppProperties appProperties;

	public void beforeStep(StepExecution stepExecution)
	{
		
		jobExecutionContext = stepExecution.getJobExecution().getExecutionContext();
		
		start = (String) jobExecutionContext.get("jobStartTime");
		userName = (String) jobExecutionContext.get("rabitMQUser");
		password = (String) jobExecutionContext.get("rabitMQPassword");
		queueName = (String) jobExecutionContext.get("rabitMQQueueName");
		exchangeName = (String) jobExecutionContext.get("rabitMQExchangeName");
		routingKey = (String) jobExecutionContext.get("rabitMQRoutingKey");
		hostName = (String) jobExecutionContext.get("rabitMQHostAddress");
		port = (Integer) jobExecutionContext.get("rabitMQPort");
		
		headers.put("vhost", "");
		headers.put("name", exchangeName);
		headers.put("routing_key", routingKey);
		headers.put("delivery_mode", "1");
		
        ConnectionFactory factory = new ConnectionFactory();        
        factory.setUsername(userName);
        factory.setPassword(password);
        //factory.setVirtualHost("/");
        factory.setHost(hostName);
        factory.setPort(port);

        try {
			connection = factory.newConnection();
			channel = connection.createChannel();
			//channel.queueDeclare(queueName, true, false, false, null);
		} catch (IOException e2) {
			e2.printStackTrace();
		} catch (TimeoutException e2) {
			e2.printStackTrace();
		}
	    
	}

	public ExitStatus afterStep(StepExecution stepExecution) 
	{
		end = DateTime.now();
		DateTime startTime  = new DateTime(start);
		double seconds = (end.getMillis() - startTime.getMillis()) / 1000d;
		int actualSeconds = (int) Math.round(seconds);		
		logger.info("Total time taken for processing " +counter+ " records are "+actualSeconds + " seconds");
		try 
		{
			moveInputFile();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		} 
		return stepExecution.getExitStatus();
	}

	public void write(List<? extends MemberDataFileBean> memData) throws Exception 
	{
		Iterator<? extends MemberDataFileBean> memberDataItr = memData.iterator();
		//ObjectMapper mapper = new ObjectMapper();
		counter++;
		while(memberDataItr.hasNext())
		{
			MemberDataFileBean memberDataVal = memberDataItr.next();
			JSONDataBean outputBean = createOutputRecord(memberDataVal);
		
			//String jsonInString = mapper.writeValueAsString(outputBean.getFinalOutputData());
			
			logger.info(outputBean.getFinalOutputData());
			
			channel.basicPublish(exchangeName,
					routingKey,
					new AMQP.BasicProperties.Builder().headers(headers).contentType("application/json").build(), 
					outputBean.getFinalOutputData().getBytes());
			
		}			
		
	}
	
	public JSONDataBean createOutputRecord(MemberDataFileBean memberDataVal)
	{
		JSONDataBean bean = new JSONDataBean();
		StringBuilder sb = new StringBuilder();
		
		/* message body payload */
		sb.append("{\n");
		sb.append("\t\"resourceType\": \"Patient\",\n");
		sb.append("\t\"id\": \"").append(memberDataVal.getMemberId()).append("\",\n");
		sb.append("\t\"active\": true,\n");
		sb.append("\t\"name\": [{\n");
		sb.append("\t\t\"use\": \"usual\",\n");
		sb.append("\t\t\"family\": [\n");
		sb.append("\t\t\t\"").append(memberDataVal.getMemberLastName()).append("\"\n");
		sb.append("\t\t],\n");
		sb.append("\t\t\"given\": [\n");
		sb.append("\t\t\t\"").append(memberDataVal.getMemberFirstName()).append("\"\n");
		sb.append("\t\t]\n");
		sb.append("\t}],\n");
		sb.append("\t\"telecom\": [{\n");
		sb.append("\t\t\"system\": \"phone\",\n");
		sb.append("\t\t\"value\": \"").append(memberDataVal.getPhoneNumber()).append("\"\n");
		sb.append("\t}, {\n");
		sb.append("\t\t\"system\": \"email\",\n");
		sb.append("\t\t\"value\": \"NA\"\n");
		sb.append("\t}],\n");
		sb.append("\t\"gender\": \"").append((memberDataVal.getGender().charAt(0)=='F')?"female":"male").append("\",\n");
		sb.append("\t\"birthDate\": \"").append(memberDataVal.getDob()).append("\",\n");
		sb.append("\t\"address\": [{\n");
		sb.append("\t\t\"use\": \"home\",\n");
		sb.append("\t\t\"line\": [\n");
		sb.append("\t\t\t\"").append(memberDataVal.getAddress()).append("\"\n");
		sb.append("\t\t],\n");
		sb.append("\t\t\"city\": \"").append(memberDataVal.getCity()).append("\",\n");
		sb.append("\t\t\"state\": \"").append(memberDataVal.getCity()).append("\",\n");
		sb.append("\t\t\"postalCode\": \"").append(memberDataVal.getPostalCode()).append("\",\n");
		sb.append("\t\t\"country\": \"USA\"\n");
		sb.append("\t}]\n");
		sb.append("}\n");

		
		bean.setFinalOutputData(sb.toString());
		return bean;
	}
	
	/*
	 * This method will move the input file from ftp locaion to archival location  
	 */
	public void moveInputFile() throws SocketException, IOException
	{
		FTPClient ftp = new FTPClient();
        ftp.connect(appProperties.ftpLocation,appProperties.ftpPort); 
        ftp.login(appProperties.ftpUser,appProperties.password); 
        
        /* form the file name */
        String fileName = "members_"+new SimpleDateFormat("yyyyMMddHHmm'.csv'").format(new Date());
        
        FTPFile[] ftpFiles = ftp.listFiles(appProperties.sourceDirectory2);
        
		for (FTPFile remoteFile : ftpFiles) {
			
			logger.info("removing file "+appProperties.sourceDirectory2 +remoteFile.getName() + " to " +appProperties.archivalDirectory );
			ftp.rename((appProperties.sourceDirectory2 + remoteFile.getName()),(appProperties.archivalDirectory + fileName));			
			//ftp.rename(("/home/dicoe/DataAndIntegration/members.csv"),("/home/dicoe/archival/" + fileName));			
			logger.info("removed file" +remoteFile.getName()+ " from "+appProperties.sourceDirectory + " location to " +appProperties.archivalDirectory );
		}
        
        ftp.logout();
	}
	

}
