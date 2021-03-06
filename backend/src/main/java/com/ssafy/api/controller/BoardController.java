package com.ssafy.api.controller;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ssafy.api.request.BoardRegisterPostReq;
import com.ssafy.api.request.PostRegisterPostReq;
import com.ssafy.api.response.BoardRes;
import com.ssafy.api.response.PostListRes;
import com.ssafy.api.response.PostRes;
import com.ssafy.api.service.BoardService;
import com.ssafy.common.model.response.BaseResponseBody;
import com.ssafy.db.entity.Board;
import com.ssafy.db.entity.Post;
import com.ssafy.db.entity.PostFile;
import com.ssafy.db.repository.PostFileRepository;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Example;
import io.swagger.annotations.ExampleProperty;

/**
 * 게시판 관련 API 요청 처리를 위한 컨트롤러 정의.
 */
@CrossOrigin("*")
@Api(value = "게시판 API", tags = {"Board"})
@RestController
@RequestMapping("/api/v1/boards")
public class BoardController {
	
	
	private final static Logger LOG = Logger.getGlobal();
	@Autowired
	BoardService boardService;

	@PostMapping()
	@ApiOperation(value = "게시판 생성", notes = "<strong>(게시판 이름, 게시판 설명, 속한 게시판 id(pk))로 게시판을 생성한다.</strong>") 
    @ApiResponses({
        @ApiResponse(code = 200, message = "성공"),
        @ApiResponse(code = 404, message = "rid에 해당하는 room이 존재하지 않음"),
        @ApiResponse(code = 500, message = "서버 오류")
    })
	public ResponseEntity<BoardRes> createBoard(
			@RequestBody @ApiParam(value="게시판 생성 정보", required = true) BoardRegisterPostReq boardInfo) {
		try {
			Board board = boardService.createBoard(boardInfo);
//			바로는 적용 안됨
//			System.out.println(board.getRoom().getBoards());
			return ResponseEntity.ok(BoardRes.of(200, "Success", board));
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(404).body(BoardRes.of(404, "No matching room for rid", null));
		}
	}
	
	
	@GetMapping("/id/{id}")
	@ApiOperation(value = "게시판 정보 id로 찾기", notes = "<strong>게시판 id로 게시판을 찾는다</strong>") 
    @ApiResponses({
        @ApiResponse(code = 200, message = "성공"),
        @ApiResponse(code = 401, message = "인증 실패"),
        @ApiResponse(code = 500, message = "서버 오류")
    })
	public ResponseEntity<BoardRes> findBoardById(
			@PathVariable @ApiParam(value="게시판 id", required = true) Long id) {
		try {
			Board board = boardService.getBoardById(id);
			for(Board b : board.getRoom().getBoards()) {
			System.out.println(b.getName());
			}
			return ResponseEntity.ok(BoardRes.of(200, "Success", board));
		} catch (Exception e) {
			return ResponseEntity.status(404).body(BoardRes.of(404, "Board does not exist using this code", null));
		}
	}
	
	@PutMapping("/{id}")
	@ApiOperation(value = "게시판 수정", notes = "<strong>게시판 id로 게시판을 찾아서 수정한다. </strong> </br> 수정하고 싶은 속성을 입력하면 된다 (입력하지 않은 속성은 유지) ") 
    @ApiResponses({
        @ApiResponse(code = 200, message = "성공"),
        @ApiResponse(code = 404, message = "id에 해당하는 게시판이 존재하지 않음"),
        @ApiResponse(code = 500, message = "서버 오류")
    })
	public ResponseEntity<BoardRes> updateBoard(
			@PathVariable @ApiParam(value="수정하고싶은 게시판 id", required = true) Long id,
			@RequestBody @ApiParam(value="수정하고싶은 게시판 정보", required = true, example="{\n \"name\":\"String\", \n \"description\":\"String\"\n}") Map<String, String> boardInfo) {
		try {
			Board board = boardService.modifyBoard(id, boardInfo);
			return ResponseEntity.ok(BoardRes.of(200, "Success", board));
		} catch (Exception e) {
			return ResponseEntity.status(404).body(BoardRes.of(404, "Board does not exist using this id", null));
		}
	}
	
	@DeleteMapping("/{id}")
	@ApiOperation(value = "게시판 삭제", notes = "<strong>게시판 id로 게시판을 삭제한다.</strong>") 
	@ApiResponses({
		@ApiResponse(code = 200, message = "성공"),
		@ApiResponse(code = 404, message = "해당 게시판 없음"),
		@ApiResponse(code = 500, message = "서버 오류")
	})
	public ResponseEntity<BaseResponseBody> deleteBoard(
			@PathVariable @ApiParam(value="게시판 id", required = true) Long id) {
		try {
			if(boardService.removeBoard(id)) {
				return ResponseEntity.status(200).body(BaseResponseBody.of(200, "Success"));
			}
			else {
				return ResponseEntity.status(404).body(BaseResponseBody.of(404, "Board does not exist"));
			}
		} catch (Exception e) {
			return ResponseEntity.status(500).body(BaseResponseBody.of(404, "server error"));
		}
	}
	
	@PostMapping("/{id}/posts")
	@ApiOperation(value = "게시글 작성", notes = "<strong>해당 게시판에 새로운 게시글을 작성한다</strong>") 
    @ApiResponses({
        @ApiResponse(code = 200, message = "성공"),
        @ApiResponse(code = 404, message = "게시판id나 유저의 uid가 유효하지 않음"),
        @ApiResponse(code = 500, message = "서버 오류")
    })
	public ResponseEntity<PostRes> createPost(
			@PathVariable @ApiParam(value="게시판 id(pk)", required = true) Long id,
			@RequestBody @ApiParam(value="게시글 정보", required = true) PostRegisterPostReq postInfo) {
		try {
			Post post = boardService.writePost(id, postInfo);
			return ResponseEntity.ok(PostRes.of(200, "Success", post));
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(404).body(PostRes.of(404, "No matching Board or User for bid or rid", null));
		}
	}
	
	@PutMapping("/posts/{pid}")
	@ApiOperation(value = "게시글 수정", notes = "<strong>pid가 일치하는 게시글의 게시판을 옮기거나, 제목·내용을 수정한다</strong></br>수정하고 싶은 항목들만 작성하면 된다.") 
	@ApiResponses({
		@ApiResponse(code = 200, message = "성공"),
		@ApiResponse(code = 404, message = "게시판 id나 pid, uid가 유효하지 않음"),
		@ApiResponse(code = 500, message = "서버 오류")
	})
	public ResponseEntity<PostRes> updatePost(
//			@PathVariable @ApiParam(value="게시판 id(pk)", required = true) Long id,
			@PathVariable @ApiParam(value="게시글 id(pk)", required = true) Long pid,
			@RequestBody @ApiParam(value="게시글 정보", required = true, example="{\n \"bid\":1, \n \"title\":\"String\", \n \"content\":\"String\"\n}") Map<String, Object> postInfo) {
		try {
			Post post = boardService.updatePost(pid, postInfo);
			return ResponseEntity.ok(PostRes.of(200, "Success", post));
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(404).body(PostRes.of(404, "id(board_pk), pid, uid is not invalid", null));
		}
	}
	
	@DeleteMapping("/posts/{pid}")
	@ApiOperation(value = "게시글 삭제", notes = "<strong>게시글 id로 게시글을 삭제한다.</strong>") 
	@ApiResponses({
		@ApiResponse(code = 200, message = "성공"),
//		@ApiResponse(code = 404, message = "해당 게시글 없음"),
		@ApiResponse(code = 500, message = "서버 오류")
	})
	public ResponseEntity<BaseResponseBody> deletePost(
			@PathVariable @ApiParam(value="게시글 id", required = true) Long pid) {
		try {
			if(boardService.removePost(pid)) {
				return ResponseEntity.status(200).body(BaseResponseBody.of(200, "Success"));
			}
			else {
				return ResponseEntity.status(404).body(BaseResponseBody.of(404, "Post does not exist"));
			}
		} catch (Exception e) {
			return ResponseEntity.status(500).body(BaseResponseBody.of(404, "server error"));
		}
	}
	
	@GetMapping("/posts/{pid}")
	@ApiOperation(value = "게시글 정보 pid로 찾기", notes = "<strong>게시글 id로 게시글을 찾는다</strong>") 
    @ApiResponses({
        @ApiResponse(code = 200, message = "성공"),
        @ApiResponse(code = 401, message = "인증 실패"),
        @ApiResponse(code = 500, message = "서버 오류")
    })
	public ResponseEntity<PostRes> findPostByPid(
			@PathVariable @ApiParam(value="게시글 id", required = true) Long pid) {
		try {
			Post post = boardService.getPostByPid(pid);
			return ResponseEntity.ok(PostRes.of(200, "Success", post));
		} catch (Exception e) {
			return ResponseEntity.status(404).body(PostRes.of(404, "Post does not exist using this pid", null));
		}
	}
	
	@GetMapping("/{id}/posts")
	@ApiOperation(value = "해당 게시판에 있는 모든 게시글들 반환", notes = "<strong>게시판 id로 해당 게시판의 모든 게시글을 찾는다</strong>") 
	@ApiResponses({
		@ApiResponse(code = 200, message = "성공"),
		@ApiResponse(code = 401, message = "인증 실패"),
		@ApiResponse(code = 500, message = "서버 오류")
	})
	public ResponseEntity<PostListRes> findPostsById(
			@PathVariable @ApiParam(value="게시판 id", required = true) Long id) {
		try {
			List<Post> posts = boardService.getPostsById(id);
			return ResponseEntity.ok(PostListRes.of(200, "Success", posts));
		} catch (Exception e) {
			return ResponseEntity.status(404).body(PostListRes.of(404, "Board does not exist using this pid", null));
		}
	}
	
	@PostMapping("/posts/{pid}/files")
	@ApiOperation(value = "게시글에 파일을 등록", notes = "<strong>게시글에 파일을 등록하고 서버에 저장한다</strong>") 
	@ApiResponses({
		@ApiResponse(code = 200, message = "성공"),
//		@ApiResponse(code = 401, message = "인증 실패"),
		@ApiResponse(code = 500, message = "서버 오류")
	})
	public ResponseEntity<BaseResponseBody> registPostFile(
			@PathVariable @ApiParam(value="게시글 pid", required = true) Long pid,
			@RequestBody @ApiParam(value="파일", required = true) MultipartFile file) {
		try {
			PostFile postFile = boardService.registFile(pid, file);
			LOG.info("파일 업로드 성공"+","+postFile.getFileName());
			return ResponseEntity.status(200).body(BaseResponseBody.of(200, "Success"));
		} catch (Exception e) {
			LOG.info("파일 업로드 실패");
			e.printStackTrace();
			return ResponseEntity.status(200).body(BaseResponseBody.of(500, "fail"));
		}
	}
	
	@GetMapping("/posts/{pid}/files")
	@ApiOperation(value = "게시글의 파일들의 name, id를 반환", notes = "<strong>게시글에 달려있는 파일들의 정보를 반환한다</strong>") 
	@ApiResponses({
//		@ApiResponse(code = 200, message = "성공", examples=@Example(value = { @ExampleProperty(value = "{\n \"pfid\":\"Long\", \n \"file_name\":\"String\"\n}") })),
		@ApiResponse(code = 200, message = "성공"),
//		@ApiResponse(code = 401, message = "인증 실패"),
		@ApiResponse(code = 500, message = "서버 오류")
	})
	public List<Map<String, Object>> findFiles(
			@PathVariable @ApiParam(value="게시글 pid", required = true) Long pid) {
		List<Map<String,Object>> res = new ArrayList<>();
		try {
			List<PostFile> pfs = boardService.getPostFiles(pid);
			for(PostFile pf : pfs) {
				Map<String,Object> map = new HashMap<>();
				map.put("pfid", pf.getId());
				map.put("file_name", pf.getOriginName());
				res.add(map);
			}
			return res;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	@GetMapping("/posts/files/{pfid}")
	@ApiOperation(value = "해당 id의 파일을 반환", notes = "<strong>pfid로 파일을 반환한다</strong>") 
	@ApiResponses({
		@ApiResponse(code = 200, message = "성공"),
//		@ApiResponse(code = 401, message = "인증 실패"),
		@ApiResponse(code = 500, message = "서버 오류")
	})
	public ResponseEntity<?> findFile(
			@PathVariable @ApiParam(value="파일 pfid", required = true) Long pfid) {
		try {
			 File file = boardService.getFileByPfid(pfid);
			 Path path = Paths.get(file.getAbsolutePath());
			 ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(path));
			 return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_LENGTH, String.valueOf(file.length()))	//파일 사이즈 설정
				.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM.toString())	//바이너리 데이터로 받아오기 설정
				.header(HttpHeaders.CONTENT_DISPOSITION,file.getName())	//다운 받아지는 파일 명 설정
				.body(resource);	//파일 넘기기
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	@DeleteMapping("/posts/files/{pfid}")
	@ApiOperation(value = "해당 id의 파일을 삭제", notes = "<strong>pfid로 파일을 삭제한다</strong>") 
	@ApiResponses({
		@ApiResponse(code = 200, message = "성공"),
		@ApiResponse(code = 500, message = "서버 오류")
	})
	public ResponseEntity<BaseResponseBody> deleteFile(
			@PathVariable @ApiParam(value="파일 pfid", required = true) Long pfid) {
		try {
			if(boardService.removeFile(pfid)) {
				return ResponseEntity.status(200).body(BaseResponseBody.of(200, "Success"));
			}
			else {
				return ResponseEntity.status(404).body(BaseResponseBody.of(404, "File does not exist"));
			}
		} catch (Exception e) {
			return ResponseEntity.status(500).body(BaseResponseBody.of(404, "server error"));
		}
	}
}
