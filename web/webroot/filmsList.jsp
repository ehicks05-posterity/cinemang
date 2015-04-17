<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:useBean id="filmsOnPage" type="java.util.List<com.hicks.Film>" scope="request"/>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<html>
<head>
    <title>Cinemang</title>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="stylesheet" type="text/css" href="../styles/cinemang.css" media="screen" />

    <script src="../js/jquery-2.1.1.min.js"></script>
    <script src="../js/jquery-ui.min.js"></script>
    <script src="../js/jquery.ui.touch-punch.min.js"></script>
    <link rel="stylesheet" href="../styles/jquery-ui.min.css" />

    <style>#ratingSlider { margin: 10px; }	</style>
    <script>
        function initHeader()
        {
            var ratingParam = '${sessionScope.rating}';
            if (ratingParam.length === 0) ratingParam = '0-10';
            var ratingValues = ratingParam.split('-');

            $('#ratingSlider').slider({
                range: true,
                min: 0,
                max: 10,
                animate: "fast",
                values: [ ratingValues[0], ratingValues[1] ]
            });
            $('#ratingSlider').slider( "option", "step", 0.1 );

            $('#ratingSlider').on( "slide", function( event, ui ) {updateRatingSelection()} );

            updateRatingSelection();
        }

        function updateRatingSelection()
        {
            var values = $('#ratingSlider').slider( "option", "values" );

            $('#ratingStart').text(values[0]);
            $('#ratingEnd').text(values[1]);
            $('#fldRating').val(values[0] + '-' + values[1]);
        }

        function sortFilms(column)
        {
            var previousColumn = '${param.column}';
            var previousDirection = '${param.direction}';
            var direction = 'asc';
            if (column === previousColumn)
            {
                if (previousDirection === 'asc') direction = 'desc';
                if (previousDirection === 'desc') direction = 'asc';
            }
            document.location.href = '?tab1=home&action=sortFilms&column=' + column + '&direction=' + direction;
        }

    </script>

</head>
<body onload="initHeader();">
<h1 style="text-align: center">Welcome to Cinemang</h1>

<form name="frmFilter" method="post" action="?tab1=home&action=filterFilms">
    <input type="hidden" id="fldRating" name="fldRating">

    <table style="margin: 0 auto" class="list">
        <tr>
            <td><label for="title">Title:</label></td>
            <td><input id="title" name="title" type="text" size="20" maxlength="32" value="${sessionScope.title}"></td>
        </tr>
        <tr>
            <td><label for="minimumVotes">Minimum Votes:</label></td>
            <td><input id="minimumVotes" name="minimumVotes" type="number" size="7" maxlength="7" value="${sessionScope.minimumVotes}"></td>
        </tr>
        <tr>
            <td><label for="minimumRating">Rating: (<span id="ratingStart"></span>-<span id="ratingEnd"></span>)</label></td>
            <td>
                <div id="ratingSlider" style="width: 80%;margin-left: auto;margin-right: auto"></div>
            </td>
        </tr>
        <tr><td colspan="2" style="text-align: center"><input type="submit" value="Search"/></td></tr>
    </table>
</form>

<table style="margin: 0 auto" class="list">
    <tr class="listheading">
        <td><fmt:formatNumber value="${fn:length(films)}" pattern="#,###"/> <br>Results</td>
        <td onclick="sortFilms('title')">Title <c:if test="${param.column eq 'title' and param.direction eq 'asc'}">&#9650;</c:if><c:if test="${param.column eq 'title' and param.direction eq 'desc'}">&#9660;</c:if></td>
        <td onclick="sortFilms('year')">Year <c:if test="${param.column eq 'year' and param.direction eq 'asc'}">&#9650;</c:if><c:if test="${param.column eq 'year' and param.direction eq 'desc'}">&#9660;</c:if></td>
        <td class="alignright" onclick="sortFilms('releaseDate')">Release Date <c:if test="${param.column eq 'releaseDate' and param.direction eq 'asc'}">&#9650;</c:if><c:if test="${param.column eq 'releaseDate' and param.direction eq 'desc'}">&#9660;</c:if></td>
        <td class="alignright" onclick="sortFilms('rating')">Rating <c:if test="${param.column eq 'rating' and param.direction eq 'asc'}">&#9650;</c:if><c:if test="${param.column eq 'rating' and param.direction eq 'desc'}">&#9660;</c:if></td>
        <td class="alignright" onclick="sortFilms('votes')">Votes <c:if test="${param.column eq 'votes' and param.direction eq 'asc'}">&#9650;</c:if><c:if test="${param.column eq 'votes' and param.direction eq 'desc'}">&#9660;</c:if></td>
    </tr>

    <c:set var="count" value="${1 + ((page - 1) * 100)}"/>
    <c:set var="rowStyle" value="listrowodd"/>
    <c:set var="rowToggle" value="${true}"/>
    <c:forEach var="film" items="${filmsOnPage}">

        <tr class="${rowStyle}">
            <td class="alignright"><fmt:formatNumber value="${count}" pattern="#,###"/></td>
            <td>${film.title}</td>
            <td>${film.year}</td>
            <td class="alignright"><fmt:formatDate value="${film.releaseDate}" pattern="MMMM d, yyyy"/></td>
            <td class="alignright">${film.rating}</td>
            <td class="alignright"><fmt:formatNumber value="${film.votes}" pattern="#,###"/></td>
        </tr>

        <c:if test="${rowToggle}"><c:set var="rowStyle" value="listroweven"/></c:if>
        <c:if test="${!rowToggle}"><c:set var="rowStyle" value="listrowodd"/></c:if>
        <c:set var="rowToggle" value="${!rowToggle}"/>
        <c:set var="count" value="${count + 1}"/>
    </c:forEach>
    <c:if test="${empty filmsOnPage}">
        <tr><td colspan="6">-</td></tr>
    </c:if>
    <tr>
        <td colspan="6" style="text-align: right;">
            <c:if test="${hasPrevious}">
                <a href="?tab1=home&action=index&page=1"><</a>
                <a style="text-decoration: none" href="?tab1=home&action=index&page=${page - 1}"><</a>
            </c:if>

            &nbsp;${page}&nbsp;

            <c:if test="${hasNext}">
                <a style="text-decoration: none" href="?tab1=home&action=index&page=${page + 1}">> </a>
                <a href="?tab1=home&action=index&page=${pages}">> </a>
            </c:if>
        </td>
    </tr>
</table>
</body>
</html>