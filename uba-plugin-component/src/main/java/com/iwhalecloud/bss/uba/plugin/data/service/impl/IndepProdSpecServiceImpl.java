package com.iwhalecloud.bss.uba.plugin.data.service.impl;

import com.iwhalecloud.bss.uba.plugin.data.dto.IndepProdSpec;
import com.iwhalecloud.bss.uba.plugin.data.mapper.IndepProdSpecMapper;
import com.iwhalecloud.bss.uba.plugin.data.service.IIndenpProdSpecService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("uba.plugin.IndepProdSpecServiceImpl")
public class IndepProdSpecServiceImpl implements IIndenpProdSpecService {

    @Autowired
    private IndepProdSpecMapper indepProdSpecMapper;

    public List<IndepProdSpec> queryIndepProdSpecList(String paidFlag) {
        IndepProdSpec ips = new IndepProdSpec();
        ips.setPaidFlag(paidFlag);
        return indepProdSpecMapper.selectList(ips);
    }

}
