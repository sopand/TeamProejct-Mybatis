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
public class SchedulerService {
								
								
	private final SchedulerMapper sMapper;

	
	/**
	 * 판매자가 정산단계에서 종료된 ( 할인이 적용된 가격을 정확하게 책정하기 위한 계산기)
	 * @param priceParam = 할인 전 기본가격
	 * @param disCountParam = 할인을 적용할 퍼센트
	 * @return
	 */
	public int endPriceCal(int priceParam, int disCountParam) {
		return priceParam - ((priceParam / 100) * (disCountParam)); //기존 가격에서 할인율 만큼의 가격을 뺀 가격.		
	}
	
	/**
	 * 
	 * 해당 서비스로직은 저희의 쇼핑몰이 시간단위로 종료되는 서비스이기 때문에, 정산처리를 하나하나 손으로 하는것 보다는 시간단위로 자동처리가
	 * 가능하게끔 해놓는게 좋을것 같아 사용해본 스케쥴링 서비스로직입니다. 정산 관련작업이다 보니 계산적인 부분이 다소 존재하는 코드입니다.
	 * 판매자 = 판매금액 정산, 구매자 = 판매 종료(판매 성공)회원 DB의 캐쉬에 저장된 값에서 차감
	 */
	@Scheduled(cron = "*/60 * * * * *") 																// 크론표현식으로 발동 시간에대한 설정이 가능. 나는 60초마다 작동하게 설정
	public void run() {
		List<Product> endProductList = sMapper.FindProduct(); 											// 공고가 종료된 제품들의 리스트
		if (endProductList.size() == 0) { 																// 종료가 없으면 메서드가 실행되어야 할 이유가 없으므로 return
			return;
		}
		List<Product> endProductPriceList = new ArrayList<>(); 											// 공고 종료된 제품들의 구매자정산을위한 리스트
		for (Product End_Product : endProductList) {
			
			
			Product sellerReturn = Product.builder().p_id(End_Product.getP_id()).p_nickname_m_fk(End_Product.getP_nickname_m_fk()).p_sell(0).build();
			// p_id = 판매가 종료된 제품의 고유번호 , p_nickname = 해당 판매자의 이름
			
			Product buyerProduct = Product.builder().p_endprice(End_Product.getP_price()).p_id(End_Product.getP_id()).p_price(End_Product.getP_price()).p_sell(0).build();
			// p_endprice= 구매자에게 돌려주거나 차감시킬 제품의 판매 종료 금액 (할인 된 금액) , p_id = 구매자에게 구매완료 처리를 할 제품의 고유번호 , p_price = 구매자에게 처음 받아온 제품의 기본가격 ( 할인 전 가격 ) , p_sell 기본값을 0으로 설정
			
			End_Product.getDiscount().stream()
			.filter(endDiscount->End_Product.getP_sell()>=endDiscount.getDis_quantity())
			.forEach(endDiscount->{ 																	// Discount 기준으로 공고의 성공유무가 결정되기 때문에 discount값으로 반복.
				int p_endprice_txt = endPriceCal(End_Product.getP_price(), endDiscount.getDis_count());	// 종료가격을 알아내기위한 int 값,
				sellerReturn.sellerEndCal(p_endprice_txt, End_Product.getP_sell());
				buyerProduct.buyerEndCal(p_endprice_txt, End_Product.getP_price(), End_Product.getP_sell());
			});

			sMapper.EndPriceSeller(sellerReturn); 														// 판매가 종료된 공고에 대해서 Seller정산을 위한 DB값이 수정되는 부분. ( Seller 정산 완료 )
			endProductPriceList.add(buyerProduct);
		}
		
		
		for (Product BuyChk_Product : endProductPriceList) { 											// 판매 성공 유무와 상관없이 구매자에게는 실패했을경우 기본값을 성공했다면 할인된값을뺀 나머지 남은 돈을반환해야 한다.
			List<Order> Product_Buyer_List = sMapper.getOrder(BuyChk_Product.getP_id()); // 종료된 제품에대한
			if(Product_Buyer_List.size()==0) {
				continue;
			}
				for (Order Buy_Member : Product_Buyer_List) { // 제품번호에 해당하는 제품을 주문한 모든 주문자에게 적립금으로 반환,
					// 해당 제품에대한 구매 갯수만큼 반환되는 가격이 바뀌게 설정하기 위한 계산
					BuyChk_Product.setP_endprice(Buy_Member.getO_quantity() * BuyChk_Product.getP_endprice());
					BuyChk_Product.setP_price(Buy_Member.getO_quantity() * BuyChk_Product.getP_price());
					sMapper.EndPriceMember(BuyChk_Product.getP_endprice(), BuyChk_Product.getP_sell(),BuyChk_Product.getP_id(),
							Buy_Member.getO_member_m_fk(),BuyChk_Product.getP_price());
				}
				Product_Buyer_List.clear(); // 위에서 List의 값들이 저장되어 있기 때문에 겹치지 않게, 배열을 비워버린다.
			}

	}

}
