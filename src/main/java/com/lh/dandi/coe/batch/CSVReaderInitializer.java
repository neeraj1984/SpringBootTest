package com.lh.dandi.coe.batch;



import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lh.dandi.coe.AppProperties;
@Component
public class CSVReaderInitializer implements Tasklet {

	@Autowired
	AppProperties appProperties;

	Logger logger = LoggerFactory.getLogger(CSVReaderInitializer.class.getName());

	public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
		logger.info("calling execute() from CSVReaderInitializer.....");

		DateTime start = DateTime.now();
		
		//member360 file name
		String member360File = appProperties.ftpFullLocation + appProperties.csvFileName;
		
		ExecutionContext jobExecutionContext = chunkContext.getStepContext().getStepExecution().getJobExecution()
				.getExecutionContext();

		jobExecutionContext.putString("memberFilePath", member360File);
		jobExecutionContext.putString("jobStartTime", start.toString());
		jobExecutionContext.putString("rabitMQHostAddress",appProperties.rabbitMQHost);
		jobExecutionContext.putString("rabitMQUser", appProperties.rabbitMQUserName);
		jobExecutionContext.putString("rabitMQPassword", appProperties.rabbitMQPassword);
		jobExecutionContext.putString("rabitMQQueueName", appProperties.rabbitMQQueueName);
		jobExecutionContext.putString("rabitMQExchangeName", appProperties.rabbitMQExchangeName);
		jobExecutionContext.putString("rabitMQRoutingKey", appProperties.rabbitMQRoutingKey);

		jobExecutionContext.putInt("rabitMQPort", appProperties.rabbitMQPort);

		return RepeatStatus.FINISHED;
	}
		
}
