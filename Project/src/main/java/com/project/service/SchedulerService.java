package com.project.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.project.mapper.SchedulerMapper;
import com.project.model.Discount;
import com.project.model.Order;
import com.project.model.Product;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SchedulerService { // 해당 쇼핑몰은 시간이 지나면 공고들의 판매가 시작되고 종료되기 때문에 그에대한 정산처리를 좀더 쉽게 하기위해서
								// 스프링의 스케쥴링을 이용하여 50초마다 DB에서 공고가 종료된 상품에 대해서 고객,판매자들의 정산을 처리합니다.
								// 판매자 = 판매금액 정산, 구매자 = 판매 종료(판매 성공)회원 DB의 캐쉬에 저장된 값에서 차감
	private final SchedulerMapper sMapper;
	// 스케쥴링과 관련된 DB작업을 진행해주는 Mapper클래스다.

		
	/**
	 * 해당 서비스로직은 저희의 쇼핑몰이 시간단위로 종료되는 서비스이기 때문에, 정산처리를 하나하나 손으로 하는것 보다는 시간단위로 자동처리가 가능하게끔 해놓는게 좋을것 같아
	 * 사용해본 스케쥴링 서비스로직입니다. 정산 관련작업이다 보니 계산적인 부분이 다소 존재하는 코드입니다.
	 */
	@Scheduled(cron = "*/60 * * * * *") // 크론표현식으로 발동 시간에대한 설정이 가능. 나는 60초마다 작동하게 설정
	public void run() {
		List<Product> endProductList= sMapper.FindProduct(); // 공고가 종료된 제품들의 리스트
		List<Product> endProductPriceList = new ArrayList<>(); // 공고 종료된 제품들의 구매자정산을위한 리스트
		if (endProductList.size() != 0) {
			for (Product End_Product : endProductList) {
				Product sellerReturn = new Product(); // 판매자에게 돌려줄 값들에대한 저장을 위한 Product객체 생성
				Product buyerProduct = new Product(); // 구매자들에게 돌려줄 값들에 대한 저장을 위한 Product객체
				// 공고의 실패유무와 상관없이 필요한 기본 값들의 셋팅
				sellerReturn.setP_id(End_Product.getP_id()); // 종료된 제품의 고유번호 저장
				sellerReturn.setP_nickname_m_fk(End_Product.getP_nickname_m_fk()); // 제품의 판매자 저장
				buyerProduct.setP_endprice(End_Product.getP_price());  // 구매자에게 돌려주거나 차감시킬 제품의 판매 종료 금액을 저장 기본값은 해당 제품의 할인 전 가격(기본가격)
				buyerProduct.setP_id(End_Product.getP_id()); // 구매자에게 구매완료 처리를할 제품의 고유번호를 저장
				buyerProduct.setP_price(End_Product.getP_price()); // 구매자에게 처음 차감한 기본가격(할인 전의 가격)을 저장
				// 공고의 실패유무와 상관없이 필요한 기본 값들의 셋팅
				for (Discount EndPro_Dis : End_Product.getDiscount()) {// Discount 기준으로 공고의 성공유무가 결정되기 때문에 discount값으로 반복.
					if (End_Product.getP_sell() >= EndPro_Dis.getDis_quantity()) { // 판매량이 할인율기준 갯수보다 높거나 같다면 그 할인율을 적용, 할인율최저치보다 낮다면 해당 제품은 판매 실패상품처리.
						int p_endprice_txt = End_Product.getP_price() - ((End_Product.getP_price() / 100) * (EndPro_Dis.getDis_count()));//종료가격을 알아내기 위한 int 값, 기존 가격에서 할인율 만큼의 가격을 뺀 가격.
						sellerReturn.setP_endprice(p_endprice_txt); // 해당 상품에 대한 판매종료 가격을 DB에 저장하기 위한 값
						sellerReturn.setP_price((p_endprice_txt - (p_endprice_txt / 100 * 5)) * End_Product.getP_sell()); //이부분은 수수료로 5%를 제외하고 나머지 판매 금액에대한 정산을 위한 계산입니다.
						sellerReturn.setP_sell(End_Product.getP_sell()); // 총 판매량을 DB에 저장하기 위한 값
						buyerProduct.setP_endprice(End_Product.getP_price() - p_endprice_txt); // 판매 성공에 대한 판매 종료가격을 저장.
					} else {
						buyerProduct.setP_sell(0); //상품 판매에 실패할 경우 몇개가 팔렷던 결국 판매취소처리(실패)처리가 되기때문에 0개로 초기화 시킨다.
					}
				}
				if (sellerReturn.getP_endprice() != 0) { // p_endprice가 0이 아니라는건 판매가 성공했다는 뜻이다.
					buyerProduct.setP_sell(1); // 1이라면 판매가 성공했다는 0 이라면 실패했다는 뜻으로 밑에 구매자 정산의 Mapper에서 구분하기 위한 값이다.
				}
				sMapper.EndPriceSeller(sellerReturn); //판매가 종료된 공고에 대해서 Seller정산을 위한 DB값이 수정되는 부분. ( Seller 정산 완료 )
				endProductPriceList.add(buyerProduct);
			}
			
			for (Product BuyChk_Product : endProductPriceList) { // 판매 성공 유무와 상관없이 구매자에게는 실패했을경우 기본값을 성공했다면 할인된값을뺀 나머지 남은 돈을 반환해야 한다.
				List<Order> Product_Buyer_List = sMapper.getOrder(BuyChk_Product.getP_id()); // 종료된 제품에대한 
				if (Product_Buyer_List.size() != 0) {
					for (Order Buy_Member : Product_Buyer_List) { // 제품번호에 해당하는 제품을 주문한 모든 주문자에게 적립금으로 반환,
						
						//해당 제품에대한 구매 갯수만큼 반환되는 가격이 바뀌게 설정하기 위한 계산
						//위의 buyerProduct p_sell이 0이냐 1이냐에 따라서 Mapper에서 둘중 하나의 값으로 정산을 한다.
						BuyChk_Product.setP_endprice(Buy_Member.getO_quantity()*BuyChk_Product.getP_endprice());
						BuyChk_Product.setP_price(Buy_Member.getO_quantity()*BuyChk_Product.getP_price());	
						
						sMapper.EndPriceMember(BuyChk_Product.getP_endprice()
								,BuyChk_Product.getP_id()
								,Buy_Member.getO_member_m_fk()
								,BuyChk_Product.getP_sell()
								,BuyChk_Product.getP_price());
					}
					Product_Buyer_List.clear(); // 위에서 List의 값들이 저장되어 있기 때문에 겹치지 않게, 배열을 비워버린다.
				}
			}
		}

	}

}
