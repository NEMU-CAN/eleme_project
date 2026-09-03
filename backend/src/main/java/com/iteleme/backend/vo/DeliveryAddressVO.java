package com.iteleme.backend.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 送货地址展示对象。
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeliveryAddressVO {
    /** 送货地址编号。 */
    private Integer id;
    /** 联系人姓名。 */
    private String contactName;
    /** 联系人性别。 */
    private Integer contactSex;
    /** 联系人电话。 */
    private String contactTel;
    /** 详细送货地址。 */
    private String address;
    /** 所属用户编号。 */
    private String userId;
}
