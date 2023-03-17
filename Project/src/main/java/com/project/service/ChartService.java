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

	// product service mypage 필요
	public List<Chart> OneWeekChart(String p_nickname_m_fk, String Day) {

		return chartMapper.OneWeekChart(p_nickname_m_fk, Day);
	}

}
