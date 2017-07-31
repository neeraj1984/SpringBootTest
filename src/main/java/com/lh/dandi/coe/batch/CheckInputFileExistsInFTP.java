package com.lh.dandi.coe.batch;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lh.dandi.coe.AppProperties;

@Component
public class CheckInputFileExistsInFTP implements Tasklet{
	
	Logger logger = LoggerFactory.getLogger(CheckInputFileExistsInFTP.class.getName());	
	
	ExecutionContext jobExecutionContext = null;
	String memberFilePath;
	
	@Autowired
	AppProperties appProperties;

	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		
		
	    jobExecutionContext = chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext();
		memberFilePath = (String) jobExecutionContext.get("memberFilePath");
		
		
		FTPClient ftp = new FTPClient();
        ftp.connect(appProperties.ftpLocation,appProperties.ftpPort); 
        ftp.login(appProperties.ftpUser,appProperties.password);
        
        FTPFile[] files = ftp.listFiles(appProperties.ftpInputFileDirectory);
        
        
        if(files.length <= 0 )
        {
        	logger.info("File does not exists in FTP location "+appProperties.ftpLocation);
        	jobExecutionContext.putString("fileNotPresent", "Y");
        }
        else
        	logger.info("File exists in FTP location "+appProperties.ftpLocation);
        
        	
        
        
        ftp.logout();
		return RepeatStatus.FINISHED;
	}
	
	public void beforeStep(StepExecution stepExecution)
	{
	    
	}


}
