package com.example.study.nettyself.protocoltcp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author lucksoul
 * @version 1.0
 * @date 2026/2/6 2:51
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageProtocol {
    private int len;

    private byte[] content;

}
