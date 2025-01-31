package com.ssafy.member.controller;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;


import com.ssafy.gooleoauth.GoogleLoginResponse;
import com.ssafy.gooleoauth.GoogleOAuthRequest;
import com.ssafy.member.model.MemberDto;
import com.ssafy.member.model.service.MemberService;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/user")
@Tag(name = "User Controller", description = "회원 관련 기능을 처리하는 클래스")
public class MemberController {

    private final MemberService memberService;
    private final JavaMailSender mailSender;

    public MemberController(MemberService memberService, JavaMailSender mailSender) {
        this.memberService = memberService;
        this.mailSender = mailSender;
        
    }
    @Value("${google.auth.url}")
    private String googleAuthUrl;

    @Value("${google.login.url}")
    private String googleLoginUrl;

    @Value("${google.client.id}")
    private String googleClientId;

    @Value("${google.redirect.url}")
    private String googleRedirectUrl;

    @Value("${google.secret}")
    private String googleClientSecret;
    
 
    

	/*
	 * // 구글 로그인창 호출 // http://localhost:8080/login/getGoogleAuthUrl
	 * 
	 * @Operation(summary = "구글 oauth 이동", description = "구글 계정 확인으로 이동합니다")
	 * 
	 * @GetMapping(value = "/login/getGoogleAuthUrl") public ResponseEntity<?>
	 * getGoogleAuthUrl(HttpServletRequest request) throws Exception {
	 * 
	 * String reqUrl = googleLoginUrl + "/o/oauth2/v2/auth?client_id=" +
	 * googleClientId + "&redirect_uri=" + googleRedirectUrl +
	 * "&response_type=code&scope=email%20profile%20openid&access_type=offline";
	 * 
	 * log.info("myLog-LoginUrl : {}",googleLoginUrl);
	 * log.info("myLog-ClientId : {}",googleClientId);
	 * log.info("myLog-RedirectUrl : {}",googleRedirectUrl);
	 * 
	 * HttpHeaders headers = new HttpHeaders();
	 * headers.setLocation(URI.create(reqUrl));
	 * 
	 * //1.reqUrl 구글로그인 창을 띄우고, 로그인 후 /login/oauth_google_check 으로 리다이렉션하게 한다.
	 * return new ResponseEntity<>(headers, HttpStatus.MOVED_PERMANENTLY); }
	 */

    // 구글에서 리다이렉션
	/*
	 * @Operation(summary = "Handle Google OAuth callback", description =
	 * "구글로 로그인 후 유저 정보 콜백")
	 * 
	 * @GetMapping(value = "/login/oauth_google_check") public
	 * ResponseEntity<String> oauth_google_check(HttpServletRequest request,
	 * 
	 * @RequestParam(value = "code") String authCode, HttpServletResponse response,
	 * HttpSession session) throws Exception {
	 * 
	 * // Google OAuth 토큰 요청 데이터 구성 GoogleOAuthRequest googleOAuthRequest =
	 * GoogleOAuthRequest.builder() .clientId(googleClientId)
	 * .clientSecret(googleClientSecret) .code(authCode)
	 * .redirectUri(googleRedirectUrl) .grantType("authorization_code") .build();
	 * 
	 * RestTemplate restTemplate = new RestTemplate();
	 * 
	 * // Google /token 엔드포인트 호출 ResponseEntity<GoogleLoginResponse> apiResponse =
	 * restTemplate.postForEntity( googleAuthUrl + "/token", googleOAuthRequest,
	 * GoogleLoginResponse.class ); GoogleLoginResponse googleLoginResponse =
	 * apiResponse.getBody();
	 * 
	 * log.info("Token Response: {}", googleLoginResponse.toString());
	 * 
	 * String googleToken = googleLoginResponse.getId_token();
	 * 
	 * // Google /tokeninfo 엔드포인트 호출 String requestUrl = UriComponentsBuilder
	 * .fromHttpUrl("https://oauth2.googleapis.com/tokeninfo")
	 * .queryParam("id_token", googleToken) .toUriString();
	 * 
	 * try { MemberDto resultJson = restTemplate.getForObject(requestUrl,
	 * MemberDto.class); log.info("Token Info Response: {}", resultJson); MemberDto
	 * memberDto = new MemberDto();
	 * 
	 * memberDto.setSub(resultJson.getSub());
	 * memberDto.setEmail(resultJson.getEmail());
	 * memberDto.setName(resultJson.getName());
	 * memberDto.setPicture(resultJson.getPicture());
	 * 
	 * System.out.println(memberDto.toString());
	 * 
	 * if(memberService.emailCheck(resultJson.getEmail())!=1) {
	 * memberService.joinMember(memberDto); } MemberDto membersession =
	 * memberService.loginMember(memberDto);
	 * 
	 * if (memberDto != null) { session.setAttribute("userinfo", membersession); }
	 * 
	 * return ResponseEntity.status(HttpStatus.CREATED).body("로그인 성공"); } catch
	 * (Exception e) { log.error("Error while calling Token Info API: {}",
	 * e.getMessage()); throw e; } }
	 */

    @Operation(summary = "회원 가입 페이지 반환", description = "회원가입을 위한 페이지를 반환합니다.")
    @GetMapping("/join")
    public ResponseEntity<String> join() {
        return ResponseEntity.ok("user/join");
    }
    
    @Operation(summary = "회원 ID 중복 확인", description = "입력한 사용자 ID가 존재하는지 확인합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "중복 확인 성공"),
        @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    
    @GetMapping("/{userid}")
    public ResponseEntity<String> emailCheck(@PathVariable("userid") String userId) {
        log.debug("idCheck userid : {}", userId);
        try {
            int cnt = memberService.emailCheck(userId);
            return ResponseEntity.ok(String.valueOf(cnt));
        } catch (Exception e) {
            log.error("ID 중복 확인 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    @Operation(summary = "회원가입", description = "회원의 정보를 받아 데이터베이스에 등록합니다.")
    @PostMapping("/join")
    public ResponseEntity<String> join(@RequestBody MemberDto memberDto) {
        log.debug("memberDto info : {}", memberDto);
        try {
            memberService.joinMember(memberDto);
            return ResponseEntity.status(HttpStatus.CREATED).body("회원가입 성공");
        } catch (Exception e) {
            log.error("회원가입 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("회원 가입 중 문제 발생!");
        }
    }

    @Operation(summary = "로그인", description = "사용자 정보를 확인하고 로그인 처리합니다.")
    //@PostMapping("/login")
    public ResponseEntity<String> login(
        @Parameter(description = "사용자 아이디", required = true) @RequestParam("userId") String userId,
        @Parameter(description = "사용자 비밀번호", required = true) @RequestParam("password") String password,
        @RequestParam(name = "saveid", required = false) String saveid,
        HttpSession session,
        HttpServletResponse response
    ) {
        log.debug("login userId : {}, password : {}", userId, password);
        try {
            // 로그인 정보 map 생성 및 전달
            Map<String, String> loginMap = Map.of("userId", userId, "password", password);
            MemberDto memberDto = null;
            		//memberService.loginMember(loginMap);
            
            if (memberDto != null) {
                session.setAttribute("userinfo", memberDto);
                Cookie cookie = new Cookie("ssafy_id", userId);
                cookie.setPath("/");
                cookie.setMaxAge("ok".equals(saveid) ? 60 * 60 * 24 * 365 * 40 : 0);
                response.addCookie(cookie);
                return ResponseEntity.ok("로그인 성공");
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("아이디 또는 비밀번호 확인 후 다시 로그인하세요.");
            }
        } catch (Exception e) {
            log.error("로그인 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("로그인 중 문제 발생!");
        }
    }


    @Operation(summary = "비밀번호 찾기", description = "사용자의 비밀번호를 이메일로 보냅니다.")
    @PostMapping("/searchpassword")
    public ResponseEntity<String> retrievePassword( @RequestParam String userId) {
        try {
            MemberDto member = memberService.searchpassword(userId);
            if (member == null ) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("사용자를 찾을 수 없습니다.");
            }
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(member.getEmail());
            helper.setSubject("비밀번호 찾기 요청");
            //helper.setText("요청하신 비밀번호는 다음과 같습니다: " +  member.getPassword());

            mailSender.send(message);

            return ResponseEntity.ok("비밀번호가 이메일로 전송되었습니다.");
        } catch (Exception e) {
            log.error("비밀번호 찾기 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("이메일 전송 중 오류가 발생했습니다.");
        }
    }


    @Operation(summary = "로그아웃", description = "현재 사용자 세션을 종료합니다.")
    @GetMapping("/logout")
    public ResponseEntity<String> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok("로그아웃 성공");
    }

    @Operation(summary = "회원 목록 페이지 반환", description = "회원 목록 페이지로 리디렉션합니다.")
    @GetMapping("/list")
    public ResponseEntity<String> list() {
        return ResponseEntity.ok("redirect:/assets/list.html");
    }

    @GetMapping("/all")
    public ResponseEntity<?> listMember() {
        try {
            return ResponseEntity.ok().body(memberService.listMember(null));
        } catch (Exception e) {
            log.error("회원 목록 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("회원 목록 조회 중 문제 발생!");
        }
    }
  
    
}
