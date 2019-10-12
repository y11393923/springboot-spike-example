package com.zyy.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SpikeOrder {
    private Long id;
    private Long userId;
    private Long orderId;
    private Long commodityId;
}
