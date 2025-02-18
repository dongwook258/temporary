package com.project.zerowasteshop.cart.controller;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.extern.slf4j.Slf4j;
import com.project.zerowasteshop.cart.model.CartVO;
import com.project.zerowasteshop.cart.service.CartService;

@Slf4j
@Controller
public class CartController {

	@Autowired
	CartService service;

	// application.properties 에서 설정한 변수(file.dir)를 DI
	@Value("${file.dir}")
	private String realPath;

	@GetMapping("/cart/insert")
	public String insert() {
		log.info("/cart/insert");
		return "cart/insert";
	}

	@GetMapping("/cart/update")
	public String update(CartVO vo, Model model) {
		log.info("/cart/update");
		log.info("vo:{}", vo);

		CartVO vo2 = service.selectOne(vo);
		log.info("vo2:{}", vo2);

		model.addAttribute("vo2", vo2);

		return "cart/update";
	}

	@GetMapping("/cart/delete")
	public String delete() {
		log.info("/cart/delete");
		return "cart/delete";
	}

	@GetMapping("/cart/selectAll")
	public String selectAll(Model model, @RequestParam(defaultValue = "1") int cpage,
			@RequestParam(defaultValue = "5") int pageBlock) {
		log.info("/cart/selectAll");
		log.info("cpage:{}", cpage);
		log.info("pageBlock:{}", pageBlock);

//		List<CartVO> list = service.selectAll();
		List<CartVO> list = service.selectAllPageBlock(cpage, pageBlock);// 해당페이지에 보여줄 5개행씩만 검색
		log.info("list.size():{}", list.size());

		model.addAttribute("list", list);

		// 디비로부터 얻은 검색결과의 모든 행수
		int total_rows = service.getTotalRows();// select count(*) total_rows from cart;
		log.info("total_rows:{}", total_rows);
		// int pageBlock = 5;//1개페이지에서 보여질 행수,파라메터로 받으면됨.
		int totalPageCount = 0;

		// 총행카운트와 페이지블럭을 나눌때의 알고리즘을 추가기
		if (total_rows / pageBlock == 0) {
			totalPageCount = 1;
		} else if (total_rows % pageBlock == 0) {
			totalPageCount = total_rows / pageBlock;
		} else {
			totalPageCount = total_rows / pageBlock + 1;
		}
		log.info("totalPageCount:{}", totalPageCount);

		model.addAttribute("totalPageCount", totalPageCount);

		return "cart/selectAll";
	}

	@GetMapping("/cart/searchList")
	public String searchList(Model model, @RequestParam(defaultValue = "id") String searchKey,
			@RequestParam(defaultValue = "ad") String searchWord,
			@RequestParam(defaultValue = "1") int cpage,
			@RequestParam(defaultValue = "5") int pageBlock) {
		log.info("/cart/searchList");
		log.info("searchKey:{}", searchKey);
		log.info("searchWord:{}", searchWord);
		log.info("cpage:{}", cpage);
		log.info("pageBlock:{}", pageBlock);

//		List<CartVO> list = service.searchList(searchKey, searchWord);
		List<CartVO> list = service.searchListPageBlock(searchKey, searchWord,cpage,pageBlock);
		log.info("list.size():{}", list.size());

		model.addAttribute("list", list);

		// 디비로부터 얻은 검색결과의 모든 행수
//		int total_rows = service.getTotalRows();// select count(*) total_rows from cart;
		// select count(*) total_rows from cart where id like '%ad%';
		// select count(*) total_rows from cart where name like '%ki%';
		int total_rows = service.getSearchTotalRows(searchKey, searchWord);
		log.info("total_rows:{}", total_rows);
		// int pageBlock = 5;//1개페이지에서 보여질 행수,파라메터로 받으면됨.
		int totalPageCount = 0;

		// 총행카운트와 페이지블럭을 나눌때의 알고리즘을 추가기
		if (total_rows / pageBlock == 0) {
			totalPageCount = 1;
		} else if (total_rows % pageBlock == 0) {
			totalPageCount = total_rows / pageBlock;
		} else {
			totalPageCount = total_rows / pageBlock + 1;
		}
		log.info("totalPageCount:{}", totalPageCount);

		model.addAttribute("totalPageCount", totalPageCount);

		return "cart/selectAll";
	}

	@GetMapping("/cart/selectOne")
	public String selectOne(CartVO vo, Model model) {
		log.info("/cart/selectOne");
		log.info("vo:{}", vo);

		CartVO vo2 = service.selectOne(vo);
		log.info("vo2:{}", vo2);

		model.addAttribute("vo2", vo2);

		return "cart/selectOne";
	}

	@PostMapping("/cart/insertOK")
	public String insertOK(CartVO vo) throws IllegalStateException, IOException {
		log.info("/cart/insertOK");
		log.info("vo:{}", vo);

		// 스프링프레임워크에서 사용하던 리얼패스사용불가.
		// String realPath = context.getRealPath("resources/upload_img");

		// @Value("${file.dir}")로 획득한 절대경로 사용해야함.
		log.info(realPath);

		String originName = vo.getFile().getOriginalFilename();
		log.info("originName:{}", originName);

		if (originName.length() == 0) {// 넘어온 파일이 없을때 default.png 할당
			vo.setImg_name("default.png");
		} else {
			// 중복이미지 이름을 배제하기위한 처리
			String save_name = "img_" + System.currentTimeMillis() + originName.substring(originName.lastIndexOf("."));
			log.info("save_name:{}", save_name);
			vo.setImg_name(save_name);

			File f = new File(realPath, save_name);
			vo.getFile().transferTo(f);

			//// create thumbnail image/////////
			BufferedImage original_buffer_img = ImageIO.read(f);
			BufferedImage thumb_buffer_img = new BufferedImage(50, 50, BufferedImage.TYPE_3BYTE_BGR);
			Graphics2D graphic = thumb_buffer_img.createGraphics();
			graphic.drawImage(original_buffer_img, 0, 0, 50, 50, null);

			File thumb_file = new File(realPath, "thumb_" + save_name);

			ImageIO.write(thumb_buffer_img, save_name.substring(save_name.lastIndexOf(".") + 1), thumb_file);

		}

		int result = service.insertOK(vo);
		log.info("result:{}", result);
		if (result == 1) {
			return "redirect:/cart/selectAll";
		} else {
			return "redirect:/cart/insert";
		}
	}

	
//	@PostMapping("/cart/updateOK")
//	public String updateOK(CartVO vo) throws IllegalStateException, IOException {
//		log.info("/cart/updateOK");
//		log.info("vo:{}", vo);
//
//		// 스프링프레임워크에서 사용하던 리얼패스사용불가.
//		// String realPath = context.getRealPath("resources/upload_img");
//
//		// @Value("${file.dir}")로 획득한 절대경로 사용해야함.
//		log.info(realPath);
//
//		String originName = vo.getFile().getOriginalFilename();
//		log.info("originName:{}", originName);
//
//		if (originName.length() == 0) {// 넘어온 파일이 없을때 default.png 할당
//			vo.setImg_name(vo.getImg_name());
//		} else {
//			// 중복이미지 이름을 배제하기위한 처리
//			String save_name = "img_" + System.currentTimeMillis() + originName.substring(originName.lastIndexOf("."));
//			log.info("save_name:{}", save_name);
//			vo.setImg_name(save_name);
//
//			File f = new File(realPath, save_name);
//			vo.getFile().transferTo(f);
//
//			//// create thumbnail image/////////
//			BufferedImage original_buffer_img = ImageIO.read(f);
//			BufferedImage thumb_buffer_img = new BufferedImage(50, 50, BufferedImage.TYPE_3BYTE_BGR);
//			Graphics2D graphic = thumb_buffer_img.createGraphics();
//			graphic.drawImage(original_buffer_img, 0, 0, 50, 50, null);
//
//			File thumb_file = new File(realPath, "thumb_" + save_name);
//
//			ImageIO.write(thumb_buffer_img, save_name.substring(save_name.lastIndexOf(".") + 1), thumb_file);
//
//		}
//
//		int result = service.updateOK(vo);
//		log.info("result:{}", result);
//		if (result == 1) {
//			return "redirect:/cart/selectOne?num=" + vo.getNum();
//		} else {
//			return "redirect:/cart/update?num=" + vo.getNum();
//		}
//	}
//
//	@PostMapping("/cart/deleteOK")
//	public String deleteOK(CartVO vo) {
//		log.info("/cart/deleteOK");
//		log.info("vo:{}", vo);
//
//		int result = service.deleteOK(vo);
//		log.info("result:{}", result);
//		if (result == 1) {
//			return "redirect:/cart/selectAll";
//		} else {
//			return "redirect:/cart/delete?num=" + vo.getNum();
//		}
//	}

}
