package mycaffe.web.controller;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import mycaffe.common.controller.CommandMap;
import mycaffe.login.service.CertificationService;
import mycaffe.web.service.BoardService;
import mycaffe.web.service.CustomerService;

@Controller
public class CustomerController {
	Logger log = Logger.getLogger(this.getClass());
	
	@Autowired
	BCryptPasswordEncoder bCryptEncoder;
	
	@Autowired
	AuthenticationManager authManager;
	
	@Resource(name="customerService")
	private CustomerService customerService;
	
	@Resource(name="certificationService")
	private CertificationService certificationService;
	
	@Resource(name="boardService")
	private BoardService boardService;
	
	/***************************************
	 * 회원 가입, 정보 수정 시 유효성 검사 해야함.
	 **************************************/
	
	//================================================================================
    // SignUp
    //================================================================================
	
	@RequestMapping("/customer/signup.do")
	public ModelAndView customerSignUp(CommandMap commandMap, HttpServletRequest request) throws Exception {
		ModelAndView mv = new ModelAndView("redirect:/loginSuccess.do");
		
		//가맹점주 아이디 중복 검사
		if (customerService.getSignUpResult(commandMap.getMap()) == 1) {
			
		}

		else {
			
			int certNumber = (Integer.parseInt(commandMap.getMap().get("certNumber").toString()));
			
			//가맹점주 가입을 위한 인증번호 검사
			if (certificationService.getCertificationNumber(certNumber) == certNumber) {
				
				String username = commandMap.get("userId").toString();
				String userPwd = commandMap.get("userPassword").toString();
				
				String hashPassword = bCryptEncoder.encode(userPwd);
				
				commandMap.getMap().remove("userPassword");
				commandMap.getMap().put("userPassword", hashPassword);
				
				customerService.addCafe(commandMap.getMap());
				
				int cafeIDX = customerService.getCafeIDXByCafeTel(commandMap.getMap().get("cafeTel"));
				System.out.println("cafeIDX : " + cafeIDX);
				
				commandMap.getMap().put("cafeIDX", cafeIDX);
				customerService.addCustomer(commandMap.getMap());
				
				autoLogin(username, userPwd, request);
			}
			
			else {
				log.debug("인증 값이 일치하지 않음. ");
			}
			    
		}
		
		return mv;
	}
	
	//회원 가입 후 자동 로그인 메소드 
	public void autoLogin(String username, String password, HttpServletRequest request) {
		UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username, password);
		Authentication authentication = authManager.authenticate(token);
		SecurityContextHolder.getContext().setAuthentication(authentication);
			
		request.getSession().setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());
	}
	
	//================================================================================
    // Cafe Management
    //================================================================================
	
	@RequestMapping("/customer/openCafeInfoUpdate.do")
	public ModelAndView openCafeInfoUpdate(CommandMap commandMap) throws Exception {
		ModelAndView mv = new ModelAndView("/customer/cafe_info_update");
		
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
   		String customerId = auth.getName();
   		commandMap.getMap().put("customerId", customerId);
    					
   		int customerIDX = customerService.getCustomerIDX(commandMap.getMap().get("customerId"));
   		String cafeName = customerService.getCafeName(customerIDX);
  		commandMap.getMap().put("customerIDX", customerIDX);
    			
    	int cafeIDX = customerService.getCafeIDX(customerIDX);
    	commandMap.getMap().put("cafeIDX", cafeIDX);
    	
    	mv.addObject("cafeName", cafeName);
    	mv.addObject("cafeIDX", cafeIDX);
		
		return mv;
	}
	
	@RequestMapping("/customer/updateCafeName.do")
	public ModelAndView updateCafeName(CommandMap commandMap) throws Exception {
		ModelAndView mv = new ModelAndView("redirect:/customer/openCafeInfoMnt.do");
		
		//Param : CafeName, CafeIDX
		
		customerService.updateCafeName(commandMap.getMap());
		
		return mv;
	}
	
	@RequestMapping("/customer/updateCafeTel.do")
	public ModelAndView updateCafeTel(CommandMap commandMap) throws Exception {
		ModelAndView mv = new ModelAndView("redirect:/customer/openCafeInfoMnt.do");
		
		//Param : CafeName, CafeIDX
		
		customerService.updateCafeTel(commandMap.getMap());
		
		return mv;
	}
	
	@RequestMapping("/customer/updateCafeLocation.do")
	public ModelAndView updateCafeLocation(CommandMap commandMap) throws Exception {
		ModelAndView mv = new ModelAndView("redirect:/customer/openCafeInfoMnt.do");
		
		//Param : CafeName, CafeIDX
		
		customerService.updateCafeLocation(commandMap.getMap());
		
		return mv;
	}
	
	@RequestMapping("/customer/updateCafeComment.do")
	public ModelAndView updateCafeComment(CommandMap commandMap) throws Exception {
		ModelAndView mv = new ModelAndView("redirect:/customer/openCafeInfoMnt.do");
		
		//Param : CafeName, CafeIDX
		
		customerService.updateCafeComment(commandMap.getMap());
		
		return mv;
	}
	
	@RequestMapping("/customer/openCafeMenuMnt.do")
	public ModelAndView openCafeMenuMnt(CommandMap commandMap) throws Exception {
		ModelAndView mv = new ModelAndView("/customer/cafe_menu_mnt");
		
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
   		String customerId = auth.getName();
   		commandMap.getMap().put("customerId", customerId);
    					
   		int customerIDX = customerService.getCustomerIDX(commandMap.getMap().get("customerId"));
   		String cafeName = customerService.getCafeName(customerIDX);
  		commandMap.getMap().put("customerIDX", customerIDX);
    			
    	int cafeIDX = customerService.getCafeIDX(customerIDX);
    	commandMap.getMap().put("cafeIDX", cafeIDX);
    	
    	List<Map<String, Object>> list = customerService.selectMenuBoardList(commandMap.getMap());
    	
    	mv.addObject("list", list);
    	mv.addObject("cafeName", cafeName);
    	mv.addObject("cafeIDX", cafeIDX);
		
		return mv;
	}
	
	@RequestMapping("/customer/openCafeMenuUpdate.do")
	public ModelAndView openCafeMenuUpdate(CommandMap commandMap) throws Exception {
		ModelAndView mv = new ModelAndView("/customer/cafe_menu_update");
		
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
   		String customerId = auth.getName();
   		commandMap.getMap().put("customerId", customerId);
    					
   		int customerIDX = customerService.getCustomerIDX(commandMap.getMap().get("customerId"));
   		String cafeName = customerService.getCafeName(customerIDX);
  		commandMap.getMap().put("customerIDX", customerIDX);
    			
    	int cafeIDX = customerService.getCafeIDX(customerIDX);
    	commandMap.getMap().put("cafeIDX", cafeIDX);
    		
    	Map<String, Object> map = customerService.selectMenuDetail(commandMap.getMap());
    	
    	mv.addObject("map", map);
    	mv.addObject("cafeName", cafeName);
    	mv.addObject("cafeIDX", cafeIDX);
		
		return mv;
	}
	
	@RequestMapping("/customer/insertCafeMenu.do")
	public ModelAndView insertCafeMenu(CommandMap commandMap) throws Exception {
		ModelAndView mv = new ModelAndView("redirect:/customer/openCafeMenuMnt.do");
		
		//Param : cafeIDX, menuType, menuName, menuPrice
		
		customerService.insertCafeMenu(commandMap.getMap());
		
		return mv;
	}
	
	@RequestMapping("/customer/updateCafeMenu.do")
	public ModelAndView updateCafeMenu(CommandMap commandMap) throws Exception {
		ModelAndView mv = new ModelAndView("redirect:/customer/openCafeMenuMnt.do");
		
		//Param : cafeIDX, menuIDX, menuType, menuName, menuPrice
		
		customerService.updateCafeMenu(commandMap.getMap());
		
		return mv;
	}
	
	@RequestMapping("/customer/deleteCafeMenu.do")
	public ModelAndView deleteCafeMenu(CommandMap commandMap) throws Exception {
		ModelAndView mv = new ModelAndView("redirect:/customer/openCafeMenuMnt.do");
		
		//Param : cafeIDX, menuIDX
		
		customerService.deleteCafeMenu(commandMap.getMap());
		
		return mv;
	}
	
	@RequestMapping("/customer/openCafeEventMnt.do")
	public ModelAndView openCafeEventMnt(CommandMap commandMap) throws Exception {
		ModelAndView mv = new ModelAndView("/customer/cafe_event_mnt");
		
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
   		String customerId = auth.getName();
   		commandMap.getMap().put("customerId", customerId);
    					
   		int customerIDX = customerService.getCustomerIDX(commandMap.getMap().get("customerId"));
   		String cafeName = customerService.getCafeName(customerIDX);
  		commandMap.getMap().put("customerIDX", customerIDX);
    			
    	int cafeIDX = customerService.getCafeIDX(customerIDX);
    	commandMap.getMap().put("cafeIDX", cafeIDX);
    	
    	mv.addObject("cafeName", cafeName);
    	mv.addObject("cafeIDX", cafeIDX);
		
		return mv;
	}
	

	//================================================================================
    // E-Coupon Management
    //================================================================================
	
	@RequestMapping("/customer/openCouponSave.do")
	public ModelAndView openCouponSave(CommandMap commandMap) throws Exception {
		ModelAndView mv = new ModelAndView("/customer/coupon_save");
		
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
   		String customerId = auth.getName();
   		commandMap.getMap().put("customerId", customerId);
    					
   		int customerIDX = customerService.getCustomerIDX(commandMap.getMap().get("customerId"));
   		String cafeName = customerService.getCafeName(customerIDX);
  		commandMap.getMap().put("customerIDX", customerIDX);
    			
    	int cafeIDX = customerService.getCafeIDX(customerIDX);
    	commandMap.getMap().put("cafeIDX", cafeIDX);
    	
    	mv.addObject("cafeName", cafeName);
    	mv.addObject("cafeIDX", cafeIDX);
		
		return mv;
	}
	
	@RequestMapping("/customer/insertCoupon.do")
	public ModelAndView insertCoupon(CommandMap commandMap) throws Exception {
		ModelAndView mv = new ModelAndView("redirect:/customer/openCouponSave.do");
		
		//Param : cafeIDX, userNickname, userIDX, couponAmount
		
		customerService.insertCoupon(commandMap.getMap());
		
		return mv;
	}
	
	@RequestMapping("/customer/openCouponAnalytics.do")
	public ModelAndView openCouponAnalytics(CommandMap commandMap) throws Exception {
		ModelAndView mv = new ModelAndView("/customer/coupon_analytics");
		
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
   		String customerId = auth.getName();
   		commandMap.getMap().put("customerId", customerId);
    					
   		int customerIDX = customerService.getCustomerIDX(commandMap.getMap().get("customerId"));
   		String cafeName = customerService.getCafeName(customerIDX);
  		commandMap.getMap().put("customerIDX", customerIDX);
    			
    	int cafeIDX = customerService.getCafeIDX(customerIDX);
    	commandMap.getMap().put("cafeIDX", cafeIDX);
    	
    	List<Map<String, Object>> list = customerService.selectCouponAnalyticsBoardList(commandMap.getMap());
    	
    	mv.addObject("list", list);
    	mv.addObject("cafeName", cafeName);
    	mv.addObject("cafeIDX", cafeIDX);
		
		return mv;
	}

	//================================================================================
    // User(Member) Management
    //================================================================================
	
	@RequestMapping("/customer/openUserInfo.do")
	public ModelAndView openUserInfo(CommandMap commandMap) throws Exception {
		ModelAndView mv = new ModelAndView("/customer/user_info");
		
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
   		String customerId = auth.getName();
   		commandMap.getMap().put("customerId", customerId);
    					
   		int customerIDX = customerService.getCustomerIDX(commandMap.getMap().get("customerId"));
   		String cafeName = customerService.getCafeName(customerIDX);
  		commandMap.getMap().put("customerIDX", customerIDX);
    			
    	int cafeIDX = customerService.getCafeIDX(customerIDX);
    	commandMap.getMap().put("cafeIDX", cafeIDX);
		
    	List<Map<String, Object>> list = boardService.selectUserOrderList(commandMap.getMap());
    	Map<String, Object> map = customerService.selectUserInfo(commandMap.getMap());
    	
    	mv.addObject("map", map);
    	mv.addObject("list", list);
    	mv.addObject("cafeName", cafeName);
    	mv.addObject("cafeIDX", cafeIDX);
    	
		return mv;
	}
	
	@RequestMapping("/customer/openUserAnalytics.do")
	public ModelAndView openUserAnalytics(CommandMap commandMap) throws Exception {
		ModelAndView mv = new ModelAndView("/customer/user_analytics");
		
		return mv;
	}
	
	//================================================================================
    // Recently Received Orders
    //================================================================================
	
	@RequestMapping("/customer/openRecentlyOrders.do")
	public ModelAndView openRecentlyOrders(CommandMap commandMap) throws Exception {
		ModelAndView mv = new ModelAndView("/customer/recently_order_mnt");
		
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
   		String customerId = auth.getName();
   		commandMap.getMap().put("customerId", customerId);
    					
   		int customerIDX = customerService.getCustomerIDX(commandMap.getMap().get("customerId"));
   		String cafeName = customerService.getCafeName(customerIDX);
  		commandMap.getMap().put("customerIDX", customerIDX);
    			
    	int cafeIDX = customerService.getCafeIDX(customerIDX);
    	commandMap.getMap().put("cafeIDX", cafeIDX);
    	
    	List<Map<String, Object>> list = boardService.selectRecentlyOrderList(commandMap.getMap());
    	
    	mv.addObject("list", list);
    	mv.addObject("cafeName", cafeName);
    	mv.addObject("cafeIDX", cafeIDX);
		
		return mv;
	}
	
	@RequestMapping("/customer/ddd.do")
	public ModelAndView openDdddddd(CommandMap commandMap) throws Exception {
		ModelAndView mv = new ModelAndView("/customer/ddddddd");
		
		return mv;
	}
	

}
