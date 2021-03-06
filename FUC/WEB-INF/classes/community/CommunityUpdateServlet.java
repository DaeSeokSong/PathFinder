package community;

import java.io.File;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.oreilly.servlet.MultipartRequest;
import com.oreilly.servlet.multipart.DefaultFileRenamePolicy;

@WebServlet("/CommunityUpdateServlet")
public class CommunityUpdateServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		response.setContentType("text/html;charset=UTF-8");
		
		MultipartRequest multi = null;
		int fileMaxSize = 10 * 1024 * 1024;
		String savePath = request.getRealPath("/upload").replaceAll("\\\\", "/");
		
		try {
			multi = new MultipartRequest(request, savePath, fileMaxSize, "UTF-8", new DefaultFileRenamePolicy());
		} catch(Exception e) {
			request.getSession().setAttribute("messageType", "오류 메세지");
			request.getSession().setAttribute("messageContent", "파일 크기는 10MB를 넘을 수 없습니다.");
			response.sendRedirect("communityUpdate.jsp");
			return;
		}
		String userID = multi.getParameter("userID");
		HttpSession session = request.getSession();
		if(!userID.equals((String)session.getAttribute("userID"))) {
			session.setAttribute("messageType", "오류 메세지");
			session.setAttribute("messageContent", "접근할 수 없습니다.");
			response.sendRedirect("main.jsp");
			return;
		}
		
		String comID = multi.getParameter("comID");
		if(comID == null || comID.equals("")) {
			session.setAttribute("messageType", "오류 메세지");
			session.setAttribute("messageContent", "접근할 수 없습니다.");
			response.sendRedirect("main.jsp");
			return;
		}
		
		CommunityDAO comDAO = new CommunityDAO();
		CommunityDTO com = comDAO.getCom(comID);
		if(!userID.equals(com.getUserID())) {
			session.setAttribute("messageType", "오류 메세지");
			session.setAttribute("messageContent", "접근할 수 없습니다.");
			response.sendRedirect("main.jsp");
			return;
		}
		
		String comTitle = multi.getParameter("comTitle");
		String comContent = multi.getParameter("comContent");
		String comLang = multi.getParameter("comLang");
		if(comTitle == null || comTitle.equals("") || comContent == null || comContent.equals("")) {
			session.setAttribute("messageType", "오류 메세지");
			session.setAttribute("messageContent", "내용을 모두 채워주세요.");
			response.sendRedirect("communityUpdate.jsp");
			return;
		}
		
		String comFile = "";
		String comRealFile = "";
		File file = multi.getFile("comFile");
		if(file != null) {
			comFile = multi.getOriginalFileName("comFile");
			comRealFile = file.getName();
			String prev = comDAO.getRealFile(comID);
			File prevFile = new File(savePath + "/" + prev);
			if(prevFile.exists()) {
				prevFile.delete();
			}
		} else {
			comFile = comDAO.getFile(comID);
			comRealFile = comDAO.getRealFile(comID);
		}
		comDAO.update(comID, comLang, comTitle, comContent, comFile, comRealFile);
		session.setAttribute("messageType", "성공 메세지");
		session.setAttribute("messageContent", "게시물 수정 완료.");
		response.sendRedirect("community.jsp");
		return;
	}

}
