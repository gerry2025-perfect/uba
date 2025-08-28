package com.iwhalecloud.bss.uba.plugin.controller;

import com.iwhalecloud.bss.uba.common.prop.CommonConst;
import com.iwhalecloud.bss.uba.plugin.data.dto.IndepProdSpec;
import com.iwhalecloud.bss.uba.plugin.data.service.IIndenpProdSpecService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/" + CommonConst.APP_CODE + "/" + CommonConst.RESTFUL_VERSION)
public class PluginManagementController {

    @Autowired
    private IIndenpProdSpecService indenpProdSpecService;

    @GetMapping("/plugin/getIndenpProdSpec")
    public List<IndepProdSpec> getIndenpProdSpec(@RequestParam("paidFlag") String paidFlag) {
        return indenpProdSpecService.queryIndepProdSpecList(paidFlag);
    }


}
