package com.lh.dandi.coe.batch;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import com.lh.dandi.coe.bean.MemberDataFileBean;

@Component
public class MemberDataProcessor implements ItemProcessor<MemberDataFileBean, MemberDataFileBean>{

	@Override
	public MemberDataFileBean process(MemberDataFileBean item) throws Exception {
		return item;
	}

}
