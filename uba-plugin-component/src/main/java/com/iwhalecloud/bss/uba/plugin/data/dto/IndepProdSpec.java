package com.iwhalecloud.bss.uba.plugin.data.dto;

import com.ztesoft.zsmart.core.jdbc.api.BaseBean;
import com.ztesoft.zsmart.core.jdbc.api.annotation.Id;
import com.ztesoft.zsmart.core.jdbc.api.annotation.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Table("INDEP_PROD_SPEC")
public class IndepProdSpec extends BaseBean {

    @Id
    private Long indepProdSpecId;
    private Integer servType;
    private String paidFlag;
    private Integer spId;

}
