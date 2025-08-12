package com.smhrd.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Board {

	private int b_no;
	private String b_title;
	private String b_writer;
	private String b_date;
	private int b_view;
	
	
	
}
