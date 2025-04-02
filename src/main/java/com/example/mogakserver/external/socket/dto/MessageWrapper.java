package com.example.mogakserver.external.socket.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class MessageWrapper {
	private String type;
	private Object data;
}