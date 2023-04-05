package com.project.service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import com.project.mapper.ProductMapper;
import com.project.model.Discount;
import com.project.model.Img;
import com.project.model.Option;
import com.project.model.Order;
import com.project.model.Pagination;
import com.project.model.PagingResponse;
import com.project.model.Product;
import com.project.model.SearchDto;
import com.project.model.SetEnum;

import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor
public class ProductService {

	private final ProductMapper productMapper; //Product의 DB처리를 해주는 객체,
	private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"); // 반복적으로 사용하는 날짜 포맷 형식을 미리 선언해놓고 재사용하기 위함.
	
	@Value("${file.Upimg}") // application.yml에 등록해놓은 파일이 저장되는 폴더에대한 위치 
	private String path;
	/**
	 * 제품을 생성하기 위한 로직으로 제품하나가 생성될 때 Img, Discount도 생성되어야 하고, 또 제품의 판매시작 - 종료시간에 맞춰서 공고가 종료되게끔 설정하기 위해
	 * 제품 생성로직에 여러개의 로직들을 받아서 실행시킨다. 그래야 트랜잭션 처리 시 어디 한군데에 문제가 생기면 원자성을 지킬수 있을 것 같아서 설정해놓음.
	 * @param productRequest = 사용자가 입력한 제품에 대한 정보들이 들어있는 요청객체,
	 * @throws Exception
	 */
	@Transactional 
	public void createProduct(Product productRequest) throws Exception {
		productMapper.AddProduct(productRequest);  // 컨트롤러로부터 넘겨받은 request데이터를 기반으로 제품에 대한 정보를 DB에 저장,
		createDiscount(productRequest, SetEnum.ADD.getType()); // 제품에 해당하는 Discount(할인율)DB 정보를 저장
		createImg(productRequest.getP_img(), productRequest.getP_id(), SetEnum.p_img.getType());// 제품의 상세 이미지를 저장시켜주는 로직
		createImg(productRequest.getP_contentimg(), productRequest.getP_id(), SetEnum.p_contentimg.getType()); // 제품의 설명에 관련된 이미지를 저장해주는 로직
		createSqlEvent(productRequest, SetEnum.ADD.getType()); // 해당 쇼핑몰은 시간에 맞춰 공고의 시작, 종료가 이뤄지기 때문에 그 시작시간,종료시간에 맞춰 DB가 변할수 있게 SQL의 이벤트 스케쥴러를 이용하는 로직.
		
	}
	/**
	 * 제품글의 수정을 담당하는 로직으로, 생성과 마찬가지로 해당 로직도 여러테이블이 같이 수정되어야 하기 때문에 여러 기능로직을 받아온다. 
	 * @param productRequest = 사용자가 수정하려하는 정보가 들어있는 요청객체
	 * @throws Exception
	 */
	@Transactional
	public void modifyProduct(Product productRequest) throws Exception {
		productMapper.UpdateProduct(productRequest); // 넘겨받은 request정보를 기반으로 DB의 기존 정보를 수정하는 로직
		createDiscount(productRequest, SetEnum.UPDATE.getType()); // Discount를 수정해주는 로직
		createSqlEvent(productRequest, SetEnum.UPDATE.getType()); // SQL의정보가 바뀌었는지 확인하고 삭제 or 유지 or 생성진행
		modifyImg(productRequest.getP_img(), productRequest.getP_id(), SetEnum.p_img.getType()); // 이미지들의 변동에 따라 수정을 진행해준다.
		modifyImg(productRequest.getP_contentimg(), productRequest.getP_id(), SetEnum.p_contentimg.getType());
	}

	/**
	 * 할인율을 등록하는 기능입니다.
	 * @param productRequest 제품등록 페이지에서 입력한 제품에 대한 모든 데이터를 가지고 있는객체,
	 * @param Type = UPDATE와 CREATE를 구분하기 위한 값
	 */
	@Transactional 
	public void createDiscount(Product productRequest, String Type) {
		if (Type.equals(SetEnum.UPDATE.getType())) {
			productMapper.DeleteDiscount(productRequest.getP_id()); // UPDATE라면 기존의 discount 데이터를 지워버리고 다시 생성한다.
		}
		for (int i = 0; i < productRequest.getP_discount_count().size(); i++) {
			Discount dis = Discount.builder().dis_count(productRequest.getP_discount_count().get(i))
					.dis_quantity(productRequest.getP_discount_quan().get(i)).dis_pid_p_fk(productRequest.getP_id())
					.build();
			productMapper.AddDiscount(dis);
		}
	}
	
	/**
	 * 새로운 이미지의 추가와 기존 이미지에서 삭제된 객체들을 삭제처리해주는 생성+수정을 진행시키는 메서드입니다.
	 * @param file = 실제 Img데이터를 가지고 있는 객체
	 * @param p_id = 이미지의 부모가되는 제품글의 고유번호입니다
	 * @param keyword = 수정/삭제되는 이미지가 대표이미지인지 상세정보 이미지인지를 구분하기 위한 용도
	 * @throws Exception
	 */

	@Transactional 
	public void modifyImg(List<MultipartFile> file, int p_id, String keyword) throws Exception {
		Img imgParameter = Img.builder().img_keyword(keyword).img_pid_p_fk(p_id).build();
		List<Img> beforeImgList = productMapper.img_length(imgParameter);
		for (int j = 0; j < file.size(); j++) {
			if (!file.get(j).isEmpty()) {
				String origName = file.get(j).getOriginalFilename(); 
				String savedName=getSavedName(origName,file.get(j));
				if (beforeImgList.size() > j) {
					deleteImg(beforeImgList.get(j));
					Img img = Img.builder().img_keyword(keyword).img_name(savedName).img_origname(origName)
							.img_pid_p_fk(p_id).img_id(beforeImgList.get(j).getImg_id()).build();
					productMapper.UpdateImg(img);				
				} else {
					Img img = Img.builder().img_keyword(keyword).img_name(savedName).img_origname(origName)
							.img_pid_p_fk(p_id).build();
					productMapper.AddImg(img);
				}
			}
		}
	}

	/**
	 * 이미지의 생성을 담당하는 객체입니다.
	 * @param file = 실제 Img 파일의 정보를 가지고있는 객체,
	 * @param p_id = 이 이미지의 부모가 되는 제품의 고유번호
	 * @param keyword = 이미지가 상세정보 이미지인지 , 대표이미지인지 구분하기 위한 keyword입니다.
	 * @throws Exception
	 */
	@Transactional 
	public void createImg(List<MultipartFile> file, int p_id, String keyword) throws Exception {
		if (!CollectionUtils.isEmpty(file)) {
			for (MultipartFile imgFile : file) {
				String origName = imgFile.getOriginalFilename(); // 입력한 원본 파일의 이름
				String savedName=getSavedName(origName,imgFile);
				Img img = Img.builder().img_keyword(keyword).img_name(savedName).img_origname(origName)
						.img_pid_p_fk(p_id).build();
				productMapper.AddImg(img);
			}
		}
	}
	
	/**
	 * 이미지 생성 / 수정시 사용되는 코드들은 거의 같은 방식을 중복이 진행되기 때문에 별도로 필요한 origiName을 인자로 받고 다른코드에서도 필요한 SavedName을 리턴 해준다.
	 * @param origName   = 이미지파일의 원본파일 명
	 * @param imgFile  = 실제 이미지 파일을 담고있는 객체
	 * @return = 다른 코드에서 필요한 값인 SavedName값을 반환해줍니다.
	 * @throws IllegalStateException
	 * @throws IOException
	 */
	public String getSavedName(String origName,MultipartFile imgFile) throws IllegalStateException, IOException {
		String uuid = String.valueOf(UUID.randomUUID());// 문자+숫자의 랜덤한 파일명으로 변경
		String extension = origName.substring(origName.lastIndexOf(".")); // 원본파일의 파일확장자
		String savedName = uuid + extension; // 랜덤이름 + 확장자
		File converFile = new File(path, savedName); // path = 상품 이미지 파일의 저장 경로가 들어있는 프로퍼티 설정값
		if (!converFile.exists()) {
			converFile.mkdirs();
		}
		imgFile.transferTo(converFile); // --- 실제로 저장을 시켜주는 부분 , 해당 경로에 접근할 수 있는 권한이 없으면 에러 발생
		return savedName;
	}
	
	/**
	 * 이미지 삭제도 공통적으로 여러코드에서 사용되기 때문에 별도로 분리하여 작성하여 재사용성을 높히고 반복 코드를 줄였다.
	 * @param i = DB에 있는 Img데이터를 가지고 있는 객체,
	 */
	public void deleteImg(Img i) {
		String deletePath = path + i.getImg_name();
		File file = new File(deletePath);
		file.delete();
	}
	
	
	/**
	 * SQL의 이벤트 스케쥴링을 활용하기 위한 메서드로 이벤트를 생성해주는 로직입니다.
	 * @param productRequest = 제품 등록 페이지에서 사용자가 입력한 데이터를 가지고 있는 객체
	 * @param type = UPDATE,CREATE 구분을 하기위함
	 */

	public void createSqlEvent(Product productRequest, String type) {
		String value = "";
		if (type.equals(SetEnum.UPDATE.getType())) {   // UPDATE라면 기존의 이벤트기록을 제거합니다.
			value = "DROP EVENT " + productRequest.getP_id() + "_start";
			productMapper.CreateNewEvent(value);
			value = "DROP EVENT " + productRequest.getP_id() + "_end";
			productMapper.CreateNewEvent(value);
		}
		value = "CREATE EVENT IF NOT EXISTS " + productRequest.getP_id() + "_start ON SCHEDULE AT '"
				+ productRequest.getP_recruitdate()
				+ "' ON COMPLETION NOT PRESERVE ENABLE COMMENT 'CHECK' DO UPDATE product set p_chk='start' WHERE p_id="
				+ productRequest.getP_id();
		productMapper.CreateNewEvent(value);
		value = "CREATE EVENT IF NOT EXISTS " + productRequest.getP_id() + "_end ON SCHEDULE AT '"
				+ productRequest.getP_duedate()
				+ "' ON COMPLETION NOT PRESERVE ENABLE COMMENT 'CHECK' DO UPDATE product set p_chk='end' WHERE p_id="
				+ productRequest.getP_id();
		productMapper.CreateNewEvent(value);
	}
	
	/**
	 * 옵션을 생성하기 위한 로직입니다.
	 * @param optionRequest = 추가 하려는 옵션에 대한 입력 정보를 가지고 있는 객체
	 */
	@Transactional
	public void createOption(Option optionRequest) {
		productMapper.AddOption(optionRequest);
	}
	
	/**
	 * 제품 상세페이지에 출력해줄 데이터를 조회 / 가공해주는 로직입니다.
	 * @param p_id = 조회하려는 제품의 고유번호
	 * @return
	 */
	public Map<String, Object> findProduct(int p_id) {

		Product findProduct = productMapper.FindProduct(p_id);
		int nowDiscount = 0;
		int nextDiscountSell = findProduct.getDiscount().get(0).getDis_quantity();
		int discountPrice = findProduct.getP_price(); // 할인이 적용된 가격을 넣으려고 만든거,
		int index = 0;
		for (Discount discount : findProduct.getDiscount()) {
			if (discount.getDis_quantity() <= findProduct.getP_sell()) {
				discountPrice = findProduct.getP_price()
						- ((findProduct.getP_price() / 100) * (discount.getDis_count()));
				nowDiscount = discount.getDis_count();
				nextDiscountSell = findProduct.getDiscount().get(index).getDis_quantity();
				if (index < findProduct.getDiscount().size() - 1) {
					nextDiscountSell = findProduct.getDiscount().get(index + 1).getDis_quantity();
				}

			}
			index++;
		}
		LocalDateTime p_recruitdate = LocalDateTime.parse(findProduct.getP_recruitdate(), formatter);
		LocalDateTime p_duedate = LocalDateTime.parse(findProduct.getP_duedate(), formatter);
		List<Map<String, Object>> categoryBest = productMapper.CategoryBestProduct(findProduct.getP_nickname_m_fk(),
				findProduct.getP_category());
		List<Map<String, Object>> sellerBest = productMapper.SellerBestProduct(findProduct.getP_nickname_m_fk());
		Map<String, Object> map = Map.of("categoryBest", categoryBest, "sellerBest", sellerBest, "nowDiscount",
				nowDiscount, "nextDiscountSell", nextDiscountSell, "discountPrice", discountPrice, "p_recruitdate",
				p_recruitdate, "p_duedate", p_duedate, "findProduct", findProduct);
		return map;
	}

	public List<Option> findOptionTwo(String opt_option1, int p_id) {
		List<Option> option_list = productMapper.Option_List(p_id);
		List<Option> FindOption = new ArrayList<>();
		for (Option opt : option_list) {
			if (opt.getOpt_option1().equals(opt_option1)) {
				FindOption.add(opt);
			}
		}
		return FindOption;
	}

	public PagingResponse<Product> findSellerProducts(String p_nickname_m_fk, SearchDto params, String keyword) {
		int elementCount = 0;
		Map<String, Object> parameterMap = new HashMap<>();
		List<Product> list = new ArrayList<>();
		if (params.getSearching() != null) {
			elementCount = productMapper.SearchSellerCount(p_nickname_m_fk, params.getSearching(), keyword);
			parameterMap.put("search", params.getSearching());
		} else {
			elementCount = productMapper.WriterProductlistCount(p_nickname_m_fk, keyword);
		}
		if (elementCount < 1) {
			return new PagingResponse<>(Collections.emptyList(), null);
		}

		Pagination pagination = new Pagination(elementCount, params);
		params.setPagination(pagination);
		parameterMap.put("p_nickname_m_fk", p_nickname_m_fk);
		parameterMap.put("limitstart", params.getPagination().getLimitStart());
		parameterMap.put("recordsize", params.getRecordSize());
		parameterMap.put("keyword", keyword);

		if (params.getSearching() != null) {
			list = productMapper.SearchSeller(parameterMap);
		} else {
			list = productMapper.WriterProductlist(parameterMap);
		}
		return new PagingResponse<>(list, pagination);
	}

	public PagingResponse<Order> findProductBuyers(String p_nickname_m_fk, SearchDto params, String keyword) {
		int elementCount = 0;
		elementCount = productMapper.BuyProductCount(p_nickname_m_fk, keyword);
		if (elementCount < 1) {
			return new PagingResponse<>(Collections.emptyList(), null);
		}
		Pagination pagination = new Pagination(elementCount, params);
		params.setPagination(pagination);

		Map<String, Object> parameterMap = new HashMap<>() {
			{
				put("p_nickname_m_fk", p_nickname_m_fk);
				put("limitstart", params.getPagination().getLimitStart());
				put("recordsize", params.getRecordSize());
				put("sort", params.getSort());
				put("keyword", keyword);
			}
		};

		List<Order> list = productMapper.BuyProduct(parameterMap);
		return new PagingResponse<>(list, pagination);
	}

	@Transactional
	public void deleteEvent(int p_id) {
		String value = "";
		Product findCalender = productMapper.FindCalender(p_id);
		String p_recruitdate_str = findCalender.getP_recruitdate();
		String p_duedate_str = findCalender.getP_recruitdate();
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime p_recruitdate = LocalDateTime.parse(p_recruitdate_str, formatter);
		LocalDateTime p_duedate = LocalDateTime.parse(p_duedate_str, formatter);
		if (now.isBefore(p_recruitdate)) {
			value = "DROP EVENT " + p_id + "_start";
			productMapper.CreateNewEvent(value);
			value = "DROP EVENT " + p_id + "_end";
			productMapper.CreateNewEvent(value);
		} else if (now.isBefore(p_duedate)) {
			value = "DROP EVENT " + p_id + "_end";
		}
		productMapper.removeProduct(p_id);

	}

	public Map<String, Object> findOptions(int p_id) {
		List<Option> optionList = productMapper.Option_List(p_id);
		List<String> newList = new ArrayList<>();
		Map<String, Object> map = new HashMap<>();
		if (optionList.size() != 0 && optionList.get(0).getOpt_option2() != null) {
			for (Option option1 : optionList) {
				newList.add(option1.getOpt_option1());
			}
			List<String> opt1 = newList.stream().distinct().collect(Collectors.toList());
			map.put("opt1", opt1);
		}
		map.put("optionList", optionList);
		return map;
	}

	public Map<String, Object> findSellPrices(String p_nickname_m_fk) { // 총 판매액 계산
		List<Map<String, Object>> allSell = productMapper.Sell_chart(p_nickname_m_fk);
		int sellMoney = 0;
		int totalSell = 0;
		for (Map<String, Object> map : allSell) {
			sellMoney += Integer.parseInt(map.get("p_endprice").toString())
					* Integer.parseInt(map.get("p_sell").toString());
			totalSell += Integer.parseInt(map.get("p_sell").toString());
		}
		Map<String, Object> create_map = new HashMap<>();
		create_map.put("sellCount", allSell.size());
		create_map.put("totalSell", totalSell);
		create_map.put("sellMoney", sellMoney);

		return create_map;
	}

	@Transactional
	public void deleteOption(String opt_option1, int opt_pid_p_fk) {
		Option opt = Option.builder().opt_option1(opt_option1).opt_pid_p_fk(opt_pid_p_fk).build();
		productMapper.OptionRemove(opt);
	}

	@Transactional
	public void deleteOneOption(int opt_id) {
		productMapper.OneOptionRemove(opt_id);
	}
	
	
	/**
	 * 해당 쇼핑몰은 등록이 완벽히 완료 된후에 삭제 처리를 하면 DB삭제가 아니라 제품공고의 상태를 remove로 바꾸는 식인데
	 * 이 로직은 제품등록 단계에서 옵션등록을 하지 않아 삭제 처리하는 로직으로, 완전한 제품이 아니기 때문에 DB데이터도 삭제 처리한다.
	 * @param p_id
	 */
	@Transactional
	public void deleteProductOptions(int p_id) {
		productMapper.OptionRemoveProduct(p_id);
	}
	
	
	/**
	 * 처음 제품을 생성하고 난 뒤 옵션 생성페이지에서 해당 제품의 옵션 등록 유무를 판단하기 위함 / 혹시나 옵션이 존재한다면 다른옵션을 입력하지 못하게 하기 위함(중분류/대분류)
	 * @param opt_pid_p_fk = 확인하려 하는 제품의 고유번호
	 * @return
	 */
	public Option hasOption(int opt_pid_p_fk) {
		List<Option> pid_All_OptionList = productMapper.Option_List(opt_pid_p_fk);
		if (pid_All_OptionList.size() > 0) {
			return pid_All_OptionList.get(0);
		}
		return null;
	}

	/**
	 * 해당하는 제품의 하위에있는 모든 옵션에 관련한 정보를 가져오는 로직입니다
	 * @param p_id = 제품의 고유번호 PK
	 * @return
	 */
	public List<Option> findAllOptions(int p_id) { 
		return productMapper.Option_List(p_id);
	}

	/**
	 * Seller의 마이페이지에서 재고정보를 클릭시 나오는 페이지에서 재고 수정을 하는 기능을 담당하는 로직
	 * @param opt_id = 수정하려는 옵션의 고유번호의 배열입니다. 
	 * @param opt_quantity = 변경 하려는 재고량의 배열
	 */
	public void modifyQuantity(int[] opt_id, int[] opt_quantity) { 
		for (int i = 0; i < opt_id.length; i++) {
			Option modifyOption = Option.builder().opt_id(opt_id[i]).opt_quantity(opt_quantity[i]).build();
			productMapper.modifyQuantity(modifyOption);
		}

	}

}
