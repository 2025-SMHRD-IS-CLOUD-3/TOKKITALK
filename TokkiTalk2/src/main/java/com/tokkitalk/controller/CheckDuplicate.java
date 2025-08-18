package com.tokkitalk.controller;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.JsonObject;
import com.tokkitalk.model.MemberDAO;

@WebServlet("/check-duplicate")
public class CheckDuplicate extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		response.setContentType("application/json; charset=UTF-8");

		String userId = request.getParameter("id");
		if (userId == null || userId.trim().isEmpty()) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			try (PrintWriter out = response.getWriter()) {
				out.write("{\"error\":\"missing_id\"}");
			}
			return;
		}

		MemberDAO dao = new MemberDAO();
		boolean exists = dao.existsById(userId.trim());

		JsonObject obj = new JsonObject();
		obj.addProperty("id", userId.trim());
		obj.addProperty("exists", exists);

		try (PrintWriter out = response.getWriter()) {
			out.write(obj.toString());
		}
	}
}


