package testcode.sqli.domain.entity;

import testcode.sqli.domain.BaseEntity;

public class SysDept extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 祖级列表 */
    private String ancestors;

    /** 部门状态:0正常,1停用 */
    private String status;
}
