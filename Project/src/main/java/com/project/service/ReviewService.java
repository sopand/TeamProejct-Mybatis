package com.project.service;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import com.project.mapper.IndexMapper;
import com.project.mapper.ReviewMapper;
import com.project.model.Img;
import com.project.model.Pagination;
import com.project.model.PagingResponse;
import com.project.model.Review;
import com.project.model.SearchDto;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class ReviewService {
	
	@Autowired
	private final ReviewMapper rMapper;
	
	@Value("${file.Upimg}")
	private String path;

	@Transactional
	public void ReviewAdd(Review r) throws Exception {
		rMapper.AddReview(r);
		for (MultipartFile img : r.getR_img()) {
			if (!img.isEmpty()) {
				AddImg(r.getR_img(), r.getR_id(), "r_img");
			}
		}

	}

	public PagingResponse<Review> AllReview(SearchDto params, String r_pnickname_m_fk) {
		int count = rMapper.Review_count(r_pnickname_m_fk);
		if (count < 1) {
			return new PagingResponse<>(Collections.emptyList(), null);
		}
		Pagination pagination = new Pagination(count, params);
		params.setPagination(pagination);
		Map<String, Object> map = new HashMap<>();
		map.put("r_pnickname_m_fk", r_pnickname_m_fk);
		map.put("limitStart", params.getPagination().getLimitStart());
		map.put("recordSize", params.getRecordSize());
		List<Review> list = rMapper.ReviewList(map);
		return new PagingResponse<>(list, pagination);
	}

	public String findnick(String m_email) {

		return rMapper.findnick(m_email);
	}

	public int reviewct(String r_pnickname_m_fk) {
		return rMapper.Review_count(r_pnickname_m_fk);
	}

	public double reviewStar(String r_pnickname_m_fk) {
		return rMapper.ReviewStar(r_pnickname_m_fk);
	}

	public void ReviewDel(int r_id) {
		rMapper.RemoveReview(r_id);
		delimg(r_id);
	}

	@Transactional // ????????? ?????? , ??????
	public void AddImg(List<MultipartFile> file, int r_id, String keyword) throws Exception {
		
		if (!CollectionUtils.isEmpty(file)) {
			for (MultipartFile imgfile : file) {
				String origName = imgfile.getOriginalFilename(); // ????????? ?????? ????????? ??????
				String uuid = String.valueOf(UUID.randomUUID());// ??????+????????? ????????? ??????????????? ??????
				String extension = origName.substring(origName.lastIndexOf(".")); // ??????????????? ???????????????
				String savedName = uuid + extension; // ???????????? + ?????????
				File converFile = new File(path, savedName); // path = ?????? ????????? ????????? ?????? ????????? ???????????? ???????????? ?????????
				if (!converFile.exists()) {
					converFile.mkdirs();
				}
				imgfile.transferTo(converFile); // --- ????????? ????????? ???????????? ?????? , ?????? ????????? ????????? ??? ?????? ????????? ????????? ?????? ??????
				Img img = Img.builder().img_keyword(keyword).img_name(savedName).img_origname(origName)
						.img_rid_r_fk(r_id).build();
				rMapper.AddImg(img);
			}
		}
	}

	public void delimg(int img_rid_r_fk) {
		rMapper.RemoveImg(img_rid_r_fk);
	}

	public int checkComments(String m_email, int p_id) {
		 
		 Map<String, Object> map = new HashMap<>();
		 
		 map.put("m_email", m_email);
		 map.put("p_id", p_id);
		 
		 int chk = rMapper.CheckComments(map);
		 
		 return chk;
	 }
}
