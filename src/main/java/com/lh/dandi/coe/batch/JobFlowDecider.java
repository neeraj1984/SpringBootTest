package com.lh.dandi.coe.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.stereotype.Component;
@Component
public class JobFlowDecider implements JobExecutionDecider{
	
	Logger logger = LoggerFactory.getLogger(JobFlowDecider.class.getName());	

	@Override
	public FlowExecutionStatus decide(JobExecution jobExecution, StepExecution stepExecution) {
		logger.info("decide() is called...............");
		ExecutionContext jobExecutionContext = stepExecution.getJobExecution().getExecutionContext();
		String fileExists = (String) jobExecutionContext.get("fileNotPresent");
		if("Y".equalsIgnoreCase(fileExists))
		{
			return FlowExecutionStatus.FAILED;
		}
		return FlowExecutionStatus.COMPLETED;

		
	}

}
