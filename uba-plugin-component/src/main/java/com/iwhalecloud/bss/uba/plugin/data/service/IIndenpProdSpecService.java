package com.iwhalecloud.bss.uba.plugin.data.service;

import com.iwhalecloud.bss.uba.plugin.data.dto.IndepProdSpec;

import java.util.List;

public interface IIndenpProdSpecService {

    public List<IndepProdSpec> queryIndepProdSpecList(String paidFlag);

}
