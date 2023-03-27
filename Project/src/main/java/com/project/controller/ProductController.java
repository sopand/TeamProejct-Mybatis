package com.project.controller;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.project.model.Chart;
import com.project.model.Img;
import com.project.model.Option;
import com.project.model.Order;
import com.project.model.PagingResponse;
import com.project.model.Product;
import com.project.model.SearchDto;
import com.project.service.ChartService;
import com.project.service.IndexService;
import com.project.service.ProductService;

import lombok.RequiredArgsConstructor;

@Controller 
@RequiredArgsConstructor 
@RequestMapping("/products") // 기본 맵핑 주소 설정
public class ProductController {

	private final ProductService productService;	// 제품에 대한 로직

	private final ChartService chartService;	// chart에 대한 로직

	private final IndexService indexService;	// 메인 페이지에 출력될 제품에 대한 로직
	
	@Value("${chart.OneWeek}")
	private String oneWeek;
	

	/**
	 * 제품 생성 페이지로 이동시켜주는 Form이동 맵핑,
	 * @param prin  = 스프링 시큐리티의 로그인 정보를 가져올 수 있는 객체로, 현재 제품을 등록하려는 사용자의 아이디값을 가져오기 위함.
	 * @param model = View에 리턴시켜줄 데이터를 설정해주기 위한 객체,
	 * @return = 로직들을 실행후 이동할 View의 이름, .html이 생략되어 있다.
	 */
	@GetMapping("") // /products GET입력시 제품 등록폼이 있는 페이지로 이동
	public String loadProductAddForm(Principal prin, Model model) {
		String memberEmail = prin.getName(); // 제품등록시 등록하는 사람의 아이디값을 받아오기 위함
		model.addAttribute("memberEmail", memberEmail); 
		return "productadd"; 
	}
	/**
	 * 제품의 등록을 실행하기 위한 맵핑
	 * @param productRequest = 사용자가 제품을 등록하기 위해 입력한 제품의 정보가 들어있는 객체,
	 * @param reDirect = forward방식으로 view를 반환한다면 새로고침을 할 경우 중복되는 데이터가 입력될 위험성이 존재하기 떄문에 Redirect를 하는데, 
	 * Redirect시 Option에 등록에 제품의 고유번호(PK)가 필요하기 때문에 그값을 View로 보내주기 위해 사용되는 객체,
	 * @return = 위의 reDirect에서 설명한것처럼 forward 대신 Redirect로 Option생성 페이지로 View를 이동한다.
	 * @throws Exception
	 */
	@PostMapping("") 
	public String createProduct(Product productRequest, RedirectAttributes reDirect) throws Exception {
		productService.createProduct(productRequest); 
		reDirect.addAttribute("p_id", productRequest.getP_id()); 
		return "redirect:/products/options"; 
	}
	

	/**
	 * 제품을 수정하기 위해 수정페이지로 이동시켜주는 Form 맵핑
	 * @param p_id = 사용자가 수정할 제품의 고유번호 (PK)
	 * @param model = View에 리턴시켜줄 데이터를 설정해주기 위한 객체,
	 * @return = productupdate.html로 View 이동시켜준다.
	 */
	@GetMapping("/{p_id}/info")
	public String loadProductUpdateForm(@PathVariable int p_id, Model model) {
		Map<String, Object> findProductMap = productService.findProduct(p_id);	// 단순하게 Product에 대한 정보가아닌 가공된 정보들과 여러
		model.addAttribute("findProductMap", findProductMap); 					// 테이블이 함께 출력되어 Map 으로 여러객체를 한번에 담아서 전송 (Service로직에서 최대한
																				// 처리하고 Controller의 코드를 줄이기 위함)
		return "productupdate";
	}
	
	/**
	 * 실제로 제품의 수정 기능을 작동시켜주는 맵핑.
	 * @param p_id = 사용자가 수정할 제품의 고유번호 (PK)
	 * @param model = View에 리턴시켜줄 데이터를 설정해주기 위한 객체,
	 * @param productRequest = 제품 수정을 하기위해 작성한 데이터들이 모여있는 객체,
	 * @return = 제품을 수정한 뒤, option의 수정 페이지로 이동하게 된다.
	 * @throws Exception
	 */

	@PutMapping("/{p_id}/info") 
	public String modifyProduct(@PathVariable int p_id, Model model, Product productRequest) throws Exception {
		productService.modifyProduct(productRequest); 
		return "redirect:/products/options/" + p_id + "/info"; 
	}

	@DeleteMapping("/{p_id}/info") // 제품 수정페이지에서 제품삭제 버튼을 클릭할 경우 발동되는 맵핑,
	public String deleteProduct(@PathVariable int p_id) throws Exception {
		productService.deleteEvent(p_id); // 제품의 p_id( 고유번호 )를 받아와 제품에 대한 삭제처리를 진행.
		return "redirect:/products/add/lists"; // CUD가 발생하는 맵핑에 대해서는 새로고침에 의한 에러방지를 위해 Redirect

	}

	@GetMapping("/{p_id}") // 제품에대한 정보를 출력해주는 상세페이지로 이동하는 맵핑,
	public String findProductDetail(@PathVariable int p_id, Model model, String r_pnickname_m_fk) {
		Map<String, Object> findProductMap = productService.findProduct(p_id); // 제품의 고유번호를 기반으로 정보를 찾아와서 Map에 담아준다. (
																				// 여러 데이터들이 복합적으로 존재하여 1개의 객체로 Return할경우
																				// Controller단의 코드가 길어져 Map으로 선택 )

		// 이 부분은 Review담당하는팀원의 코드입니다. ( 간단하게 주석처리로 어떤 기능인지만 작성 )
		int reviewCount = indexService.reviewct(r_pnickname_m_fk); // 제품의 제품명을 기반으로 해당 제품의 점수, 리뷰 갯수를 가져오는 로직입니다.
		double reviewStar = indexService.reviewStar(r_pnickname_m_fk);
		reviewStar = Math.round(((reviewStar / reviewCount) * 10) / 10.0); // 가져온 점수를 갯수를 기반으로 나눠서 평점을 만드는 로직
		model.addAttribute("findProductMap", findProductMap); // 각각의 데이터를 View로 전송
		model.addAttribute("p_id", p_id);
		model.addAttribute("reviewCount", reviewCount);
		model.addAttribute("reviewStar", reviewStar);
		// 이 부분은 Review담당하는팀원의 코드입니다.
		return "productdetail";
	}

	@ResponseBody // Ajax를 이용한 비동기방식의 데이터전송(JSON)을 위한 ResponseBody
	@GetMapping("/options/{p_id}") // 제품 상세페이지에서1번 옵션을 선택할경우 2번의 옵션이 달라져야하기 때문에 1번옵션에 대한 각각의 하위옵션을 가져와주는 맵핑입니다.
	public List<Option> findOptionTwo(String opt_option1, @PathVariable int p_id) {
		return productService.findOptionTwo(opt_option1, p_id); // 현재 선택한 상위옵션과 해당 제품에대한 정보를 받아와 하위옵션을 찾아 리턴해줍니다.
	}



	@GetMapping("/{keyword}/lists") // 판매자의 판매중인,판매예정,판매종료된 제품에대한 목록을 보여주기 위한 맵핑입니다.
	public String findSellerProducts(Principal principal, Model model, @ModelAttribute("params") SearchDto params,
			@PathVariable String keyword) { // 판매자의정보,페이징을 위한 정보,해당 페이지의keyword ( 판매종료,중,예정 ) 을 가져옵니다
		String memberEmail = principal.getName();
		
		//판매자의아이디, 페이징에대한 정보(현재페이지 쪽수),keyword(판매중,종료,예정)을 가져와서 판매자의 제품목록을 찾아옵니다.
		PagingResponse<Product> sellerProductList = productService.findSellerProducts(memberEmail, params, keyword);
		
		List<Img> imgList = new ArrayList<>();
		for (Product img : sellerProductList.getList()) { // View에서 쉽게 출력하기위해 imgList를 분리해주는 로직입니다.
			imgList.addAll(img.getImg());
		}
		
		//현재 판매자가 판매한 제품의 갯수, 판매완료된 공고의 갯수, 총 판매한 제품의 금액등을 출력해주기 위해 찾아오는 서비스로직
		Map<String, Object> sellerTotalCount = productService.findSellPrices(memberEmail);
		
		//최근 1주일간 판매완료된 제품이 존재할경우 그 제품들에 대한 수익을 보여주는 그래프 차트를 출력해주기위한 데이터를 가져오는 서비스로직
		List<Chart> chart = chartService.OneWeekChart(memberEmail, oneWeek);
		model.addAttribute("img", imgList);
		model.addAttribute("chart", chart);
		model.addAttribute("sellerTotalCount", sellerTotalCount);
		model.addAttribute("sellerProductList", sellerProductList);
		model.addAttribute("keyword", keyword);
		return "mypage"; // 제품 판매자의 mypage로 이동시켜줍니다.
	}

	@GetMapping("/{keyword}/lists/buy") // 판매자의 제품을 구매한 사람들의 목록을 보여줍니다. ( 운송장등록을 위한 페이지 )
	public String findSellerProductBuyers(Principal principal, Model model, @ModelAttribute("params") SearchDto params,
			@PathVariable String keyword) {
		String memberEmail = principal.getName();
		
		//구매한 사람들의 목록을 보여주기위해 페이징처리를 진행하는 로직입니다.
		PagingResponse<Order> sellerBuyerList = productService.findProductBuyers(memberEmail, params, keyword);
		
		//현재 판매자가 판매한 제품의 갯수, 판매완료된 공고의 갯수, 총 판매한 제품의 금액등을 출력해주기 위해 찾아오는 서비스로직
		Map<String, Object> sellerTotalCount = productService.findSellPrices(memberEmail);
		model.addAttribute("sellerTotalCount", sellerTotalCount);
		model.addAttribute("sellerBuyerList", sellerBuyerList);
		model.addAttribute("keyword", keyword);
		model.addAttribute("sortSelect", params.getSort());
		return "buymember"; // buymember.html페이지로 forward
	}

	@GetMapping("/charts/{day}/{month}") //판매자가 최근 1주일,2주일 한달,6개월 단위로 판매한 제품들에대한 정보를 그래프,Chart.js로 출력시켜주는 페이지 맵핑입니다.
	public String findChart(@PathVariable(required = false) String day, @PathVariable(required = false) String month,
			Principal principal, Model model) { // required=false처리된 값들은 항상 존재하는 값들이 아니기때문에 에러에대한 방지를 위해 처리해뒀습니다.
		String memberEmail = principal.getName();
		
		//판매자의 정보, 몇주 단위 ,몇달 단위로 그래프를 출력 할 것인지를 request받아와 차트의 데이터를 받아옵니다.
		Map<String, Object> allChart = chartService.AllChartList(memberEmail, day, month);
		model.addAttribute("allChart", allChart);
		
		return "chart"; //chart.html 페이지로 이동합니다.
	}

	@ResponseBody // Ajax 비동기통신을 위한 처리
	@GetMapping("/options/chk") // Option페이지로 이동시 해당 제품에 대한 기존 옵션 존재유무를 판단 , 단일 ,다중옵션의 중복을 배제하기 위해 반대되는 옵션페이지를 지우기 위한 맵핑.
	public Option hasOptionAdd(int opt_pid_p_fk) { // 
		Option optionCheck = productService.hasOption(opt_pid_p_fk); // 제품고유번호로 제품에대한 옵션 존재유무룰 판단합니다.
		return optionCheck; // 찾아온 옵션데이터를 리턴해줍니다.
	}

	@ResponseBody // Ajax 비동기 통신을 위한 처리
	@GetMapping("/options/lists") // 판매자의 판매제품 목록에서 옵션에대한 정보를 알아보기 위해 사용하는 맵핑.
	public List<Option> findOptions(int p_id) {
		return productService.findAllOptions(p_id); // 판매자가 옵션을 보려고 선택한 제품에 대한 고유번호를 기반으로 Option 데이터를 찾아옵니다.
	}

	@ResponseBody // Ajax 비동기 통신을 위한 처리
	@PutMapping("/options/lists") // 위의 옵션 List로 옵션을 보고 그 옵션에 대한 재고를 변경시켜줄 수 있는 맵핑입니다.
	public void modifyQuantity(int[] opt_id, int[] opt_quantity) {
		productService.modifyQuantity(opt_id, opt_quantity); // 선택한 제품의 옵션을 변경하기 버튼을 클릭시 옵션의 고유번호,수량을 가져와 변경 처리합니다.
	}

	@ResponseBody // Ajax 비동기 통신을 위한 처리
	@DeleteMapping("/options") // Option생성페이지에서 Option을 생성하지 않고 페이지 이동 or 새로고침 or 등록완료처리를 하려고 할 경우 경고문을 출력합니다.
	public String deleteProductOptionCheck(int opt_pid_p_fk) { // 경고문 출력후 이동시에 옵션이 존재하지 않는다면 그 제품은 삭제처리 하도록 처리해두는 맵핑입니다!!
		Option productOptionCheck = productService.hasOption(opt_pid_p_fk); // 제품의 고유번호로 옵션의 존재유무를 체크합니다.
		if (productOptionCheck == null) { // null이라면 옵션이 존재하지 않는다는 뜻으로 삭제처리를 진행하게됩니다.
			productService.deleteProductOptions(opt_pid_p_fk);
			return "옵션이 존재하지 않아 해당 제품은 삭제 처리되었습니다.";
		} else { // null이 아니라면 옵션이 1개라도 존재한다는 뜻으로 옵션이 저장완료 및 제품의 추가가 종료되게 됩니다.
			return "옵션 저장 및 제품 추가 완료";
		}
	}

	@GetMapping("/options")  //Option 생성페이지로 이동시켜주는Form페이지 맵핑입니다.
	public String loadOptionAddForm(Model model, int p_id) {
		//Option productOptionCheck = productService.hasOption(p_id);
		//model.addAttribute("productOptionCheck", productOptionCheck);
		
		// 해당 페이지에서는 이전 페이지인 ProductAdd페이지에서 생성된 Product의 고유번호를 FK로 사용하고 있어 필수값이므로,p_id를 View로 전송시켜줍니다.		
		model.addAttribute("p_id", p_id);
		return "option";
	}
	@ResponseBody // Ajax 비동기통신을 위한 처리
	@PostMapping("/options") // Option생성페이지에서 Option을 생성하기 위한 맵핑.
	public String createOption(Option optionRequest, int opt_pid_p_fk) {
		if (optionRequest.getOpt_opt1_list() == null) { // 하위옵션이 존재하지 않는 단일옵션인지 하위옵션이 존재하는 방식인지에따라 다른 값이 들어가기 때문에 1번옵션의
														// 유무를 체크하기 위한 if문입니다.
														// 단일옵션일 경우에는 opt1가 여러개 (List)로 존재하기 때문에 null이 아니고 하위옵션이 존재하면
														// opt1 하나에 여러개의 opt2를 가지고 있기때문에 List가 아닌 String객체로 받아옵니다.
			for (int i = 0; i < optionRequest.getOpt_opt2_list().size(); i++) {
				if (!optionRequest.getOpt_opt2_list().get(i).isEmpty()) { // opt2의 input이 여러개 존재할수가 있어 그 input에 입력값이
																			// 있을때만 그값에대한 추가를 하기위한 if문
					Option option = Option.builder().opt_pid_p_fk(opt_pid_p_fk) // 옵션이 등록될 제품의 고유번호
							.opt_option1(optionRequest.getOpt_option1()) // 다중옵션에 대한 1번옵션의 값
							.opt_option2(optionRequest.getOpt_opt2_list().get(i)) // 다중옵션의 하위가될 2번옵션의 값
							.opt_quantity(optionRequest.getOpt_quantity_list().get(i)).build();// 이 옵션에대한 수량
					productService.createOption(option);// 옵션 등록을위한 서비스로직
				}
			}
			return "AllOptionAdd"; // 단일,다중 옵션이 중복되는 에러를 방지하기위해 어떤 옵션을 등록했는지 View로 보내주고 이 값을 기반으로 JS에서 단일 옵션에대한
									// 추가View를 지워버립니다.
		} else {
			for (int i = 0; i < optionRequest.getOpt_opt1_list().size(); i++) { // 단일옵션일경우 실행,단일은 옵션2가 없이 옵션1만 여러개 존재.
				Option option = Option.builder().opt_pid_p_fk(opt_pid_p_fk) // 옵션에 대한 제품의 고유번호
						.opt_option1(optionRequest.getOpt_opt1_list().get(i)) // 옵션1의 값
						.opt_quantity(optionRequest.getOpt_quantity_list().get(i)).build(); // 옵션에대한 제품수량
				productService.createOption(option); // 제품등록을위한 서비스로직
			}
		}
		return "OneOptionAdd"; // 단일,다중옵션이 중복되는 에러를 방지하기위해 값을 View로 보내고 js에서 반대되는 옵션방식에 대한 추가View를 지워버립니다.
	}

	@GetMapping("/options/{p_id}/info") // 옵션 수정에대한 Form으로 이동시켜주는 맵핑입니다.
	public String loadOptionEditForm(@PathVariable int p_id, Model model) {
		//옵션 수정하려는 제품의 고유변호를 가져와 Option 데이터를 찾아옵니다
		Map<String, Object> productFindOption = productService.findOptions(p_id);
		model.addAttribute("p_id", p_id);
		model.addAttribute("productFindOption", productFindOption);
		return "optionUpdate"; // optionUpdate.html 페이지로 이동 
	}

	//Option 수정페이지에서 옵션을 삭제할경우 사용하는 맵핑입니다.다중옵션에 대한 삭제처리를 담당합니다.
	@DeleteMapping("/options/{opt_option1}/info/{opt_pid_p_fk}")
	public String deleteOption(@PathVariable String opt_option1, @PathVariable int opt_pid_p_fk) {
		// 다중옵션의 대분류인 1번옵션 정보와 해당 제품의 고유번호를 함께 받아옵니다. ( 옵션1번에 대한 중복삭제를 방지하기 위함)
		productService.deleteOption(opt_option1, opt_pid_p_fk);
		
		return "redirect:/products/options/" + opt_pid_p_fk + "/info";
	}
	
	/**
	 * Option 수정페이지에서 옵션을 삭제할 경우 사용하는 맵핑. 단일 옵션에 대한 삭제처리를 담당.
	 * 
	 * opt_id : 삭제하려고 하는 옵션에대한 고유번호 PK
	 * opt_pid_p_fk : 삭제할 옵션을 가지고 있는 제품에 대한 고유번호 Product의 PK
	 */
	@DeleteMapping("/options/{opt_id}/info") 
	public String deleteOneOption(@PathVariable int opt_id, int opt_pid_p_fk) {
		productService.deleteOneOption(opt_id); // 단일 옵션은 각각 하나가 대분류이기 때문에 opt의 고유번호만 가져와서 삭제를 진행합니다.
		return "redirect:/products/options/" + opt_pid_p_fk + "/info";
	}
	
}
