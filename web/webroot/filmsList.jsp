<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:useBean id="films" type="java.util.List<com.hicks.Film>" scope="session"/>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<html>
<head>
    <title>Cinemang</title>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="stylesheet" type="text/css" href="../styles/cinemang.css" media="screen" />
</head>
<body>
<h1 style="text-align: center">Welcome to Cinemang</h1>

<form name="frmFilter" method="post" action="?tab1=home&action=filterFilms">
    <table style="margin: 0 auto" class="list">
        <tr>
            <td><label for="minimumVotes">Minimum Votes:</label></td><td><input id="minimumVotes" name="minimumVotes" type="text" size="10" maxlength="10" value="${param.minimumVotes}"></td>
        </tr>
        <tr>
            <td><label for="minimumRating">Minimum Rating:</label></td><td><input id="minimumRating" name="minimumRating" type="text" size="10" maxlength="10" value="${param.minimumRating}"></td>
        </tr>
        <tr><td colspan="2" style="text-align: center"><input type="submit" value="Submit"/></td></tr>
    </table>
</form>

<table style="margin: 0 auto" class="list">
    <tr class="listheading">
        <td>${fn:length(films)} Results</td>
        <td>Title</td>
        <td class="alignright">Rating</td>
        <td class="alignright">Votes</td>
    </tr>

    <c:set var="count" value="1"/>
    <c:set var="rowStyle" value="listrowodd"/>
    <c:set var="rowToggle" value="${true}"/>
    <c:forEach var="film" items="${films}">

        <tr class="${rowStyle}">
            <td class="alignright"><fmt:formatNumber value="${count}" pattern="#,###"/></td>
            <td>${film.title}</td>
            <td class="alignright">${film.rating}</td>
            <td class="alignright"><fmt:formatNumber value="${film.votes}" pattern="#,###"/></td>
        </tr>

        <c:if test="${rowToggle}"><c:set var="rowStyle" value="listroweven"/></c:if>
        <c:if test="${!rowToggle}"><c:set var="rowStyle" value="listrowodd"/></c:if>
        <c:set var="rowToggle" value="${!rowToggle}"/>
        <c:set var="count" value="${count + 1}"/>
    </c:forEach>
</table>
</body>
</html>