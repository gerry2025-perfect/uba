package com.iwhalecloud.bss.uba.plugin.data.mapper;

import com.iwhalecloud.bss.uba.plugin.data.dto.IndepProdSpec;
import com.ztesoft.zsmart.core.jdbc.mybatis.BaseMapper;
import org.springframework.stereotype.Repository;

@Repository("uba.plugin.IndepProdSpecMapper")
public interface IndepProdSpecMapper extends BaseMapper<IndepProdSpec> {
}
