package com.project.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.project.mapper.ChartMapper;
import com.project.model.Chart;
import com.project.model.Product;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChartService {

	private final ChartMapper chartMapper;
	
	
	/**
	 * Seller전용페이지인 판매와 관련된 데이터를 차트로 보여주는 서비스를 위한 로직으로 모든 차트와 관련된 데이터를 가져옵니다.
	 * @param memberEmail  = 현재 차트페이지를 보려고 하는 사용자의 아이디 (Seller의 아이디)
	 * @param Day = 보기위해 설정한 날짜의 범위 , Day = 1주,2주, Month =한달 , 6개월 단위로 설정이 가능하다.
	 * @param Month
	 * @return
	 */
	public Map<String, Object> AllChartList(String memberEmail, String Day, String Month) {
		//여러 다른 객체들을 한번에 View로 보낼수 있어야 하기 때문에 Map에 담아준다.
		Map<String, Object> chartMap= new HashMap<>();
		//1주일간 판매된 제품의 숫자
		List<Chart> Week = chartMapper.OneWeekChart(memberEmail, Day);
		List<Map<String, Object>> NowAllSell = chartMapper.NowAllSell(memberEmail);
		List<Chart> WeekSell = chartMapper.OneWeekSellPrice(memberEmail, Day);
		List<Chart> MonthCategory = chartMapper.OneMonthCategorySell(memberEmail, Month);
		List<Chart> MonthFailPro = chartMapper.OneMonthFailedProduct(memberEmail, Month);
		List<Map<String, Object>> NowAllProduct = chartMapper.NowAllProduct(memberEmail);
		List<Map<String, Object>> NowEndSellMoney = chartMapper.NowEndSellMoney(memberEmail);
		chartMap.put("Week", Week);
		chartMap.put("NowAllSell", NowAllSell);
		chartMap.put("NowEndSellMoney", NowEndSellMoney);
		chartMap.put("NowAllProduct", NowAllProduct);
		chartMap.put("Day", Day);
		chartMap.put("Month", Month);
		chartMap.put("WeekSell", WeekSell);
		chartMap.put("MonthCategory", MonthCategory);
		chartMap.put("MonthFailPro", MonthFailPro);
		return chartMap;
	}

	/**
	 * 해당 차트 데이터는 통계를 보기위한 서비스란이아닌 Seller 마이페이지에 입장시 상단에 출력되는 작은 차트를 넣기위한 로직입니다.
	 * @param memberEmail = Seller의 로그인 아이디값
	 * @param Day = 마이페이지에 출력되는 차트의 경우는 1주일단위만출력가능, 
	 * @return
	 */
	public List<Chart> OneWeekChart(String memberEmail, String Day) {

		return chartMapper.OneWeekChart(memberEmail, Day);
	}

}
