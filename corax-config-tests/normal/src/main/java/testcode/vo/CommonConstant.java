package testcode.vo;

/**
 * @Description: 通用常量
 * @author: jeecg-boot
 */
public interface CommonConstant {

	/**
	 * 正常状态
	 */
	Integer STATUS_NORMAL = 0;

	/**
	 * 禁用状态
	 */
	Integer STATUS_DISABLE = -1;

	/**
	 * 删除标志
	 */
	Integer DEL_FLAG_1 = 1;

	/**
	 * 未删除
	 */
	Integer DEL_FLAG_0 = 0;

	/**
	 * 系统日志类型： 登录
	 */
	int LOG_TYPE_1 = 1;
	
	/**
	 * 系统日志类型： 操作
	 */
	int LOG_TYPE_2 = 2;

	/**
	 * 操作日志类型： 查询
	 */
	int OPERATE_TYPE_1 = 1;
	
	/**
	 * 操作日志类型： 添加
	 */
	int OPERATE_TYPE_2 = 2;
	
	/**
	 * 操作日志类型： 更新
	 */
	int OPERATE_TYPE_3 = 3;
	
	/**
	 * 操作日志类型： 删除
	 */
	int OPERATE_TYPE_4 = 4;
	
	/**
	 * 操作日志类型： 倒入
	 */
	int OPERATE_TYPE_5 = 5;
	
	/**
	 * 操作日志类型： 导出
	 */
	int OPERATE_TYPE_6 = 6;
	
	
	/** {@code 500 Server Error} (HTTP/1.0 - RFC 1945) */
    Integer SC_INTERNAL_SERVER_ERROR_500 = 500;
    /** {@code 200 OK} (HTTP/1.0 - RFC 1945) */
    Integer SC_OK_200 = 200;
    
    /**访问权限认证未通过 510*/
    Integer SC_JEECG_NO_AUTHZ=510;

 
}
