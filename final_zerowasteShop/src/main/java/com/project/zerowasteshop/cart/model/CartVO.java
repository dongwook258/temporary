package com.project.zerowasteshop.cart.model;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Data
public class CartVO {

	private int cart_num;
	private int product_num;
	private String member_id;
	private int count;
	private int price;
	private String product_img;
	private String img_name;
	private MultipartFile file;
}
