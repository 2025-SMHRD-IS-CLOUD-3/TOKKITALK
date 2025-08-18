package com.tokkitalk.model;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MevenMember {
	
	private String user_id;
	private String user_pw;
	private String user_name;
	private String gender;
	private Date user_date;
}
