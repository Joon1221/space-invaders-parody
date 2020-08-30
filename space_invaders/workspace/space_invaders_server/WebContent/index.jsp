<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>회원가입 폼</title>
<style type="text/css">
   body {
      font-family: "돋움";
      font-size: 12px;
   }
   table {
      border-collapse: collapse;
   }
   th,td {
      border: 1px solid blue;
      padding-left: 7px;
   }
   input {
      font-family: "돋움";
      font-size: 12px;
   }
   select {
      font-family: "돋움";
      font-size: 12px;
   }
</style>
</head>
<body>
<h3>회원가입</h3>
<form id="joinForm" action="Test1" method="post">
<table>
<tr>
   <td style="width: 100px">아이디</td>
   <td><input type="text" name="id" />
   <input type="button" value="ID중복체크" /></td>
</tr>
<tr>
   <td>별명</td>
   <td><input type="text" name="nickname" /></td>
</tr>
<tr>
   <td>비밀번호</td>
   <td><input type="password" name="passwd" /></td>
</tr>
<tr>
   <td>비밀번호 확인</td>
   <td><input type="password" name="passwd_confirm" /></td>
</tr>
<tr>
   <td>이름</td>
   <td><input type="text" name="name" /></td>
</tr>
<tr>
   <td>성별</td>
   <td>
         남 <input type="radio" name="gender" value="M" />
         여 <input type="radio" name="gender" value="F" />
   </td>
</tr>
<tr>
   <td>생년월일</td>
   <td>
   <input type="text" name="birthyear" style="width: 30px" /> 년 
   <select name="birthmon">
      <option value="0" selected="selected">-월-</option>
      <option value="1">1</option>
      <option value="2">2</option>
      <option value="3">3</option>
      <option value="4">4</option>
      <option value="5">5</option>
      <option value="6">6</option>
      <option value="7">7</option>
      <option value="8">8</option>
      <option value="9">9</option>
      <option value="10">10</option>
      <option value="11">11</option>
      <option value="12">12</option>
   </select>월
   <select name="birthday">
      <option value="0" selected="selected">-일-</option>
      <option value="1">1</option>
      <option value="2">2</option>
      <option value="3">3</option>
      <option value="4">4</option>
      <option value="5">5</option>
      <option value="6">6</option>
      <option value="7">7</option>
      <option value="8">8</option>
      <option value="9">9</option>
      <option value="10">10</option>
      <option value="11">11</option>
      <option value="12">12</option>
      <option value="13">13</option>
      <option value="14">14</option>
      <option value="15">15</option>
      <option value="16">16</option>
      <option value="17">17</option>
      <option value="18">18</option>
      <option value="19">19</option>
      <option value="20">20</option>
      <option value="21">21</option>
      <option value="22">22</option>
      <option value="23">23</option>
      <option value="24">24</option>
      <option value="25">25</option>
      <option value="26">26</option>
      <option value="27">27</option>
      <option value="28">28</option>
      <option value="29">29</option>
      <option value="30">30</option>
      <option value="31">31</option>		
   </select>일
         양력 <input type="radio" name="solar" value="Y" checked="checked" />
         음력 <input type="radio" name="solar" value="N" />
   </td>
</tr>
<tr>
   <td>휴대전화</td>
   <td>
      <input type="text" name="mobile" />
   </td>
</tr>
<tr>
   <td>일반전화</td>
   <td>
      <input type="text" name="tel" />
   </td>
</tr>
<tr>
   <td rowspan="3">주소</td>
   <td>
      <input type="text" name="zipcode1" style="width: 30px" /> -
      <input type="text" name="zipcode2" style="width: 30px" />
      <input type="button" value="우편번호찾기" />
   </td>
</tr>
<tr>
   <td>
      <input type="text" name="add1" style="width: 300px" />
   </td>
</tr>
<tr>
   <td><input type="text" name="addr2" style="width: 300px" /></td>
</tr>
<tr>
   <td>이메일</td>
   <td><input type="text" name="email" /></td>
</tr>
<tr>
   <td>이메일수신여부</td>
   <td>
         수신 <input type="radio" name="emailyn" value="Y" checked="checked" />
         수신하지 않음 <input type="radio" name="emailyn" value="N" />
   </td>
</tr>
<tr>
   <td>좋아하는 운동</td>
   <td>
     <input type="checkbox" name="ball" value="soccer" />축구
     <input type="checkbox" name="ball" value="baseball" />야구
     <input type="checkbox" name="ball" value="basketball" />농구
     <input type="checkbox" name="ball" value="tennis" />테니스
     <input type="checkbox" name="ball" value="tabletennis" />탁구
   </td>
</tr>
<tr>
   <td>관심분야</td>
   <td>
      <select name="focus" multiple="multiple">
      <option value="">-- 관심분야 --</option>
      <option value="JAVA">JAVA</option>
      <option value="JSP">JSP</option>
      <option value="EJB">EJB</option>
      <option value="Struts">Struts</option>
      <option value="Spring">Spring</option>
      <option value="iBatis">iBatis</option>
      </select>
            복수선택가능
   </td>
</tr>
<tr>
   <td>자기소개</td>
   <td>
      <textarea name="sogae" cols="40" rows="7"></textarea>
   </td>
</tr>
<tr>
   <td colspan="2" style="text-align: center;">
      <input type="submit" value="전송" />
   </td>
</tr>
</form>
</body>
</html>