package com.project.model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Product {
	private String p_category;
	private String p_nickname_m_fk;
	private String p_duedate;
	private String p_recruitdate;
	private String p_chk;
	private String p_name;
	private Timestamp p_createdate;
	private int p_id;
	private int p_price;
	private int p_sell;
	private int p_endprice;
	private ArrayList<Img> img;
	private ArrayList<Option> option;
	private ArrayList<Discount> discount;

	private List<MultipartFile> p_img; // request 용
	private List<MultipartFile> p_contentimg; // request 용
	private	List<Integer> p_discount_quan; // request 용
	private List<Integer> p_discount_count; // request 용
	
	public void sellerEndCal(int endPriceParam,int sellParam) {
		this.p_endprice=endPriceParam; // 해당 상품에 대한 판매종료 가격을 DB에 저장하기 위한 값
		this.p_price=(endPriceParam - (endPriceParam / 100 * 5)) * sellParam; //이부분은 수수료로 5%를 제외하고 나머지 판매 금액에대한 정산을 위한 계산입니다.
		this.p_sell=sellParam;		 // 총 판매량을 DB에 저장하기 위한 값
	}
	
	public void buyerEndCal(int endPriceParam,int priceParam,int sellParam) {
		this.p_sell=sellParam;
		this.p_endprice=(priceParam - endPriceParam);
	}

}
