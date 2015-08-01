<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:useBean id="filmsOnPage" type="java.util.List<com.hicks.Film>" scope="request"/>
<jsp:useBean id="uniqueLanguages" type="java.util.List<java.lang.String>" scope="request"/>
<jsp:useBean id="uniqueGenres" type="java.util.List<java.lang.String>" scope="request"/>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<!DOCTYPE html>
<html>
<head>
    <title>Cinemang</title>
    <meta name="viewport" content="width=device-width, initial-scale=1">

    <script src="../js/jquery-2.1.1.min.js"></script>
    <script src="../js/jquery-ui.min.js"></script>
    <script src="../js/jquery.ui.touch-punch.min.js"></script>
    <link rel="stylesheet" href="../styles/jquery-ui.min.css" />
    <link rel="shortcut icon" href="../images/spaceCat.png">
    <link href='http://fonts.googleapis.com/css?family=Open+Sans' rel='stylesheet' type='text/css'>
    <link rel="stylesheet" type="text/css" href="../styles/cinemang.css" media="screen" />

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

            $("#fromReleaseDateDatepicker").datepicker({
                showOn: 'button',
                buttonImage: '../images/calendar.gif',
                buttonImageOnly: true,
                changeYear: true,
                yearRange: '1888:2015'
            });

            $("#toReleaseDateDatepicker").datepicker({
                showOn: 'button',
                buttonImage: '../images/calendar.gif',
                buttonImageOnly: true,
                changeYear: true,
                yearRange: '1888:2015'
            });
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
            var previousColumn = '${sessionScope.sortColumn}';
            var previousDirection = '${sessionScope.sortDirection}';
            var direction = 'desc';
            if (column === previousColumn)
            {
                if (previousDirection === 'asc') direction = 'desc';
                if (previousDirection === 'desc') direction = 'asc';
            }
            $('#sortColumn').val(column);
            $('#sortDirection').val(direction);

            document.getElementById("frmFilter").submit();
        }

        function showPlotDialog(title, poster, fullPlot, director, actors, runtime, tomatoConsensus)
        {
            $( "#dialog-plot" ).dialog({
                resizable: false,
                height:'auto',
                width:650,
                modal: true,
                open: function (event, ui)
                {
                    $( "#dialog-plot").dialog('option', 'title', title);
                    $( "#posterUrl").attr('src', poster);
                    $( "#dialogPlot").html('<b>Plot:</b><br>' + fullPlot);
                    $( "#dialogDirector").html('<b>Director:</b><br>' + director);
                    $( "#dialogActors").html('<b>Actors:</b><br>' + actors);
                    $( "#dialogRuntime").html('<b>Running Time:</b><br>' + runtime);
                    $( "#dialogTomatoConsensus").html('<b>Tomato Critic Consensus:</b><br>' + tomatoConsensus);
                },
                close: function (event, ui)
                {
                    $( "#posterUrl").attr('src', '');
                },
                buttons:
                {
                    Close: function()
                    {
                        $( this ).dialog( "close" );
                    }
                }
            }).position({ my: "middle", at: "middle"});
        }

    </script>

</head>
<body onload="initHeader();">
<form name="frmFilter" id="frmFilter" method="post" action="?tab1=home&action=filterFilms">
    <input type="hidden" id="fldRating" name="fldRating">
    <input type="hidden" name="sortColumn" id="sortColumn" value="${sessionScope.sortColumn}"/>
    <input type="hidden" name="sortDirection" id="sortDirection" value="${sessionScope.sortDirection}"/>

    <table style="margin: 10px auto 0 auto" class="list">
        <tr>
            <td colspan="4">
                <h1 style="text-align: center;margin: 0;">CINEMANG <img src="../images/spaceCat.png" style="height: 30px;vertical-align: middle"></h1>
            </td>
        </tr>
        <tr>
            <td class="alignright"><label for="title">Title:</label></td>
            <td colspan="3"><input id="title" name="title" type="text" size="20" maxlength="32" value="${sessionScope.title}"></td>
        </tr>
        <tr>
            <td class="alignright"><label for="fromReleaseDateDatepicker">Release Date:</label></td>
            <td>
                <table style="border: none;">
                    <tr>
                        <td style="border: none;text-align: right">
                            From:
                        </td>
                        <td style="border: none;">
                            <input type="text" id="fromReleaseDateDatepicker" name="fromReleaseDateDatepicker" size="6" maxlength="10" value="${sessionScope.fromReleaseDate}">
                        </td>
                    </tr>
                    <tr>
                        <td style="border: none;text-align: right">
                            <label for="toReleaseDateDatepicker">To:</label>
                        </td>
                        <td style="border: none;">
                            <input type="text" id="toReleaseDateDatepicker" name="toReleaseDateDatepicker" size="6" maxlength="10" value="${sessionScope.toReleaseDate}">
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
        <tr>
            <td class="alignright"><label for="minimumVotes">Minimum Votes:</label></td>
            <td colspan="3"><input id="minimumVotes" name="minimumVotes" type="number" size="7" maxlength="7" value="${sessionScope.minimumVotes}"></td>
        </tr>
        <tr>
            <td class="alignright">IMDb Rating: (<span id="ratingStart"></span>-<span id="ratingEnd"></span>)</td>
            <td colspan="3">
                <div id="ratingSlider" style="width: 80%;margin-left: auto;margin-right: auto"></div>
            </td>
        </tr>
        <tr>
            <td class="alignright"><label for="language">Language:</label></td>
            <td colspan="3">
                <select id="language" name="language">
                    <option value="" ${language == '' ? 'selected' : ''}>Any</option>
                    <c:forEach var="uniqueLanguage" items="${uniqueLanguages}">
                        <option value="${uniqueLanguage}" ${language == uniqueLanguage ? 'selected' : ''}>${uniqueLanguage}</option>
                    </c:forEach>
                </select>
            </td>
        </tr>
        <tr>
            <td class="alignright"><label for="fldGenre">Genre:</label></td>
            <td colspan="3">
                <select id="fldGenre" name="fldGenre">
                    <option value="" ${genre == '' ? 'selected' : ''}>Any</option>
                    <c:forEach var="uniqueGenre" items="${uniqueGenres}">
                        <option value="${uniqueGenre}" ${genre == uniqueGenre ? 'selected' : ''}>${uniqueGenre}</option>
                    </c:forEach>
                </select>
            </td>
        </tr>
        <tr><td colspan="4" style="text-align: center"><input type="submit" value="Search"/></td></tr>
        <tr><td colspan="4" style="text-align: center"><span>${searchResultsSize} Results</span></td></tr>
    </table>
</form>
<br>
<table style="margin: 0 auto" class="list">
    <tr>
        <td colspan="100" style="text-align: center;">
            <div>
                <c:if test="${hasPrevious}">
                    <a href="?tab1=home&action=form&page=1"><</a>
                    <a style="text-decoration: none" href="?tab1=home&action=form&page=${page - 1}"><</a>
                </c:if>
                &nbsp;${page}&nbsp;
                <c:if test="${hasNext}">
                    <a style="text-decoration: none" href="?tab1=home&action=form&page=${page + 1}">> </a>
                    <a href="?tab1=home&action=form&page=${pages}">> </a>
                </c:if>
            </div>
        </td>
    </tr>
    <tr class="listheading">
        <td></td>
        <td class="sortableHeader" onclick="sortFilms('title')">Title
            <c:if test="${sessionScope.sortColumn eq 'title' and sessionScope.sortDirection eq 'asc'}">&#9650;</c:if>
            <c:if test="${sessionScope.sortColumn eq 'title' and sessionScope.sortDirection eq 'desc'}">&#9660;</c:if>
        </td>
        <td class="sortableHeader alignright" onclick="sortFilms('comboRating')">
            <img src="../images/spaceCat.png" title="Combo Rating: Combines imdb Rating, Tomato Meter, and Tomato User Meter" style="height:24px;vertical-align: middle"/>
            <c:if test="${sessionScope.sortColumn eq 'comboRating' and sessionScope.sortDirection eq 'asc'}">&#9650;</c:if>
            <c:if test="${sessionScope.sortColumn eq 'comboRating' and sessionScope.sortDirection eq 'desc'}">&#9660;</c:if>
        </td>
        <td class="sortableHeader alignright" onclick="sortFilms('tomatoMeter')">
            <img src="../images/rottenTomatoes_logo.png" title="Tomato Meter" style="height:24px;vertical-align: middle"/>
            <c:if test="${sessionScope.sortColumn eq 'tomatoMeter' and sessionScope.sortDirection eq 'asc'}">&#9650;</c:if>
            <c:if test="${sessionScope.sortColumn eq 'tomatoMeter' and sessionScope.sortDirection eq 'desc'}">&#9660;</c:if>
        </td>
        <td class="sortableHeader alignright" onclick="sortFilms('tomatoUserMeter')">
            <img src="../images/rottenTomatoes_user_logo.png" title="Tomato User Meter" style="height:24px;vertical-align: middle"/>
            <c:if test="${sessionScope.sortColumn eq 'tomatoUserMeter' and sessionScope.sortDirection eq 'asc'}">&#9650;</c:if>
            <c:if test="${sessionScope.sortColumn eq 'tomatoUserMeter' and sessionScope.sortDirection eq 'desc'}">&#9660;</c:if>
        </td>
        <td class="sortableHeader alignright" onclick="sortFilms('imdbRating')">
            <img src="../images/imdb_logo.png" title="IMDb Rating" style="height:24px;vertical-align: middle"/>
            <c:if test="${sessionScope.sortColumn eq 'imdbRating' and sessionScope.sortDirection eq 'asc'}">&#9650;</c:if>
            <c:if test="${sessionScope.sortColumn eq 'imdbRating' and sessionScope.sortDirection eq 'desc'}">&#9660;</c:if>
        </td>
        <%--<td class="sortableHeader mediumPriority alignright" onclick="sortFilms('metascore')">--%>
            <%--<img src="../images/metacritic_logo.png" title="Metascore" style="height:24px;vertical-align: middle"/>--%>
            <%--<c:if test="${sessionScope.sortColumn eq 'metascore' and sessionScope.sortDirection eq 'asc'}">&#9650;</c:if>--%>
            <%--<c:if test="${sessionScope.sortColumn eq 'metascore' and sessionScope.sortDirection eq 'desc'}">&#9660;</c:if>--%>
        <%--</td>--%>
        <td class="sortableHeader mediumPriority alignright" onclick="sortFilms('releaseDate')">Release Date
            <c:if test="${sessionScope.sortColumn eq 'releaseDate' and sessionScope.sortDirection eq 'asc'}">&#9650;</c:if>
            <c:if test="${sessionScope.sortColumn eq 'releaseDate' and sessionScope.sortDirection eq 'desc'}">&#9660;</c:if>
        </td>
        <td class="sortableHeader lowPriority alignright" onclick="sortFilms('imdbVotes')">IMDb Votes
            <c:if test="${sessionScope.sortColumn eq 'imdbVotes' and sessionScope.sortDirection eq 'asc'}">&#9650;</c:if>
            <c:if test="${sessionScope.sortColumn eq 'imdbVotes' and sessionScope.sortDirection eq 'desc'}">&#9660;</c:if>
        </td>
        <td class="sortableHeader lowPriority" onclick="sortFilms('language')">Language
            <c:if test="${sessionScope.sortColumn eq 'language' and sessionScope.sortDirection eq 'asc'}">&#9650;</c:if>
            <c:if test="${sessionScope.sortColumn eq 'language' and sessionScope.sortDirection eq 'desc'}">&#9660;</c:if>
        </td>
        <td class="lowPriority">Genres</td>
    </tr>

    <c:set var="count" value="${1 + ((page - 1) * 100)}"/>
    <c:set var="rowStyle" value="listrowodd"/>
    <c:set var="rowToggle" value="${true}"/>
    <c:forEach var="film" items="${filmsOnPage}">

        <tr class="${rowStyle}">
            <td class="alignright"><fmt:formatNumber value="${count}" pattern="#,###"/></td>
            <td>
                <span onclick='showPlotDialog("${fn:escapeXml(film.title)}", "${film.poster}",
                        "<c:out value="${fn:escapeXml(film.plot)}"/>",
                        "<c:out value="${fn:escapeXml(film.director)}"/>",
                        "<c:out value="${fn:escapeXml(film.actors)}"/>",
                        "<c:out value="${fn:escapeXml(film.runtime)}"/>",
                        "<c:out value="${fn:escapeXml(film.tomatoConsensus)}"/>")'
                         style="color: blue; text-decoration: underline; cursor: pointer">
                    <c:set var="filmTitle" value="${film.title}"/>
                    <c:if test="${fn:length(film.title) > 50}"><c:set var="filmTitle" value="${fn:substring(film.title, 0, 50)}..."/></c:if>
                    ${filmTitle}
                    <c:if test="${film.tomatoImage=='fresh'}"><img src="../images/certified_logo.png" style="vertical-align: middle" height="16px"/></c:if>
                </span>
            </td>
            <td class="alignright">${film.comboRating}</td>
            <td class="alignright">${film.tomatoMeter}</td>
            <td class="alignright">${film.tomatoUserMeter}</td>
            <td class="alignright">
                <a href="http://www.imdb.com/title/${film.imdbID}" title="${film.title}" target="_blank">
                    ${film.imdbRating}
                </a>
            </td>
            <%--<td class="mediumPriority alignright">${film.metascore}</td>--%>
            <td class="mediumPriority alignright">${film.released}</td>
            <td class="alignright lowPriority"><fmt:formatNumber value="${film.imdbVotes}" pattern="#,###"/></td>
            <td class="lowPriority">${film.language}</td>
            <td class="lowPriority">${film.genre}</td>
        </tr>

        <c:if test="${rowToggle}"><c:set var="rowStyle" value="listroweven"/></c:if>
        <c:if test="${!rowToggle}"><c:set var="rowStyle" value="listrowodd"/></c:if>
        <c:set var="rowToggle" value="${!rowToggle}"/>
        <c:set var="count" value="${count + 1}"/>
    </c:forEach>
    <c:if test="${empty filmsOnPage}">
        <tr><td colspan="100">-</td></tr>
    </c:if>
    <tr>
        <td colspan="100" style="text-align: center;">
            <c:if test="${hasPrevious}">
                <a href="?tab1=home&action=form&page=1"><</a>
                <a style="text-decoration: none" href="?tab1=home&action=form&page=${page - 1}"><</a>
            </c:if>
            &nbsp;${page}&nbsp;
            <c:if test="${hasNext}">
                <a style="text-decoration: none" href="?tab1=home&action=form&page=${page + 1}">> </a>
                <a href="?tab1=home&action=form&page=${pages}">> </a>
            </c:if>
        </td>
    </tr>
</table>

<%-- Plot Dialog --%>
<div style="display:none;">
    <div id="dialog-plot" title="Plot" style="text-align: justify">
        <div style="float: left; padding-right: 10pt">
            <img id="posterUrl" src=""/>
        </div>
        <span id="dialogPlot"></span><br><br>
        <span id="dialogDirector"></span><br><br>
        <span id="dialogActors"></span><br><br>
        <span id="dialogRuntime"></span><br><br>
        <span id="dialogTomatoConsensus"></span><br><br>
    </div>
</div>
</body>
</html>