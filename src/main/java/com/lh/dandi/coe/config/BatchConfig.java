package com.lh.dandi.coe.config;

import java.net.MalformedURLException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.support.JobRegistryBeanPostProcessor;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.FlowJobBuilder;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.UrlResource;
import org.springframework.scheduling.annotation.Scheduled;

import com.lh.dandi.coe.AppProperties;
import com.lh.dandi.coe.batch.CSVReaderInitializer;
import com.lh.dandi.coe.batch.CheckInputFileExistsInFTP;
import com.lh.dandi.coe.batch.JSONDataWriter;
import com.lh.dandi.coe.batch.JobFlowDecider;
import com.lh.dandi.coe.batch.MemberDataProcessor;
import com.lh.dandi.coe.bean.MemberDataFileBean;
import com.lh.dandi.coe.mapper.MemberDataFieldSetMapper;

@Configuration
@EnableBatchProcessing
public class BatchConfig {
	
	Logger logger = LoggerFactory.getLogger(BatchConfig.class.getName());
	
	@Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;
    
    @Autowired
    CheckInputFileExistsInFTP taskletStep;
    
    @Autowired
    CSVReaderInitializer csvReaderInitializer;
    
    @Autowired
    private JobLauncher jobLauncher;

	@Autowired
	AppProperties appProperties;
    
    @Bean
    public ItemReader<MemberDataFileBean> reader() throws MalformedURLException {
        FlatFileItemReader<MemberDataFileBean> reader = new FlatFileItemReader<MemberDataFileBean>();
        
		//member360 file name
		String member360File = appProperties.ftpFullLocation + appProperties.csvFileName;
		
        reader.setResource(new UrlResource(new URL(member360File)));
        reader.setLineMapper(new DefaultLineMapper<MemberDataFileBean>() {{
            setLineTokenizer(new DelimitedLineTokenizer());
            setFieldSetMapper(new MemberDataFieldSetMapper());
        }});
        return reader;
    }
    
    @Bean
    public ItemProcessor<MemberDataFileBean, MemberDataFileBean> processor() {
        return new MemberDataProcessor();
    }
     
    @Bean
    public ItemWriter<MemberDataFileBean> writer() {
        return new JSONDataWriter();
    }
    
    @Bean
    public Job startBatchJob() throws MalformedURLException 
    {
        /* Way:1 */
    	/*
    	return jobBuilderFactory.get("csvToJSONJob")
        		.incrementer(new RunIdIncrementer())
                .start(fileCheck())
                .next(processMemberData())
                .build();
                */
         
    	/* Way:2 */
        JobBuilder jobBuilder = jobBuilderFactory.get("csvToJSONJob").incrementer(new RunIdIncrementer());
        FlowBuilder<FlowJobBuilder> builder = jobBuilder.flow(paramSetting()).
        		next(fileCheck()).
        		next(decider()).on("FAILED").end()
        		.next(processMemberData());
        return builder.build().build();
        
        
    }
 
    @Bean
    public Step processMemberData() throws MalformedURLException {
        return stepBuilderFactory.get("processCSVFile")
                .<MemberDataFileBean, MemberDataFileBean> chunk(1)
                .reader(reader())
                .processor(processor())
                .writer(writer())
                .build();
    }
    
    @Bean
    public Step fileCheck() {
        return stepBuilderFactory.get("fileCheck")
        		.tasklet(taskletStep)
        		.allowStartIfComplete(true)
                .build();
    }
    
    @Bean
    public Step paramSetting() {
        return stepBuilderFactory.get("paramSetting")
        		.tasklet(csvReaderInitializer)
        		.allowStartIfComplete(true)
                .build();
    }
    
    /* for registering job*/
    @Bean
    public JobRegistryBeanPostProcessor jobRegistryBeanPostProcessor( JobRegistry jobRegistry) {
    JobRegistryBeanPostProcessor jobRegistryBeanPostProcessor = new JobRegistryBeanPostProcessor();
    jobRegistryBeanPostProcessor.setJobRegistry(jobRegistry);
        return jobRegistryBeanPostProcessor;
    }
    
    
    @Bean
    public JobFlowDecider decider() {
    	JobFlowDecider decider = new JobFlowDecider(); 
        return decider;
    }
   
    
    /* initial delay is required otherwise job gets started twice 
     * 
     * set schedular to get fired in every 3 minutes(180000 mili seconds)
     * 
     */
     
     @Scheduled(initialDelay = 180000,fixedRate = 180000)
	 public void scheduleBatchJob() throws JobExecutionAlreadyRunningException, JobInstanceAlreadyCompleteException, JobParametersInvalidException
	 {
		logger.info("BatchConfig.scheduleBatchJob() is called by schedular..............."); 
		try 
		{	        
	        JobParameters param = new JobParametersBuilder().toJobParameters();
	        jobLauncher.run(startBatchJob(), param);
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		 
	 }
	 
	 

}
