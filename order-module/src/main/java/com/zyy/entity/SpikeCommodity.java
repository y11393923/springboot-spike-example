package com.zyy.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SpikeCommodity {
    private Long spikeId;
    private Long commodityId;
    private BigDecimal spikePrice;
    private Long stockCount;
    private Date startDate;
    private Date endDate;
    private Integer version;
}
