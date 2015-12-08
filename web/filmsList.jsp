<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:useBean id="filmsForm" type="com.hicks.FilmsForm" scope="session"/>
<jsp:useBean id="filmSearchResult" type="com.hicks.FilmSearchResult" scope="session"/>
<jsp:useBean id="uniqueLanguages" type="java.util.List<java.lang.String>" scope="request"/>
<jsp:useBean id="uniqueGenres" type="java.util.List<java.lang.String>" scope="request"/>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<!DOCTYPE html>
<html>
<head>
    <title>Cinemang</title>
    <meta name="viewport" content="width=device-width, initial-scale=1">

    <script src="js/jquery-2.1.1.min.js"></script>
    <script src="js/jquery-ui.min.js"></script>
    <script src="js/jquery.ui.touch-punch.min.js"></script>
    <link rel="stylesheet" href="styles/jquery-ui.min.css" />
    <link rel="shortcut icon" href="images/spaceCat.png">
    <link href='http://fonts.googleapis.com/css?family=Open+Sans' rel='stylesheet' type='text/css'>
    <link rel="stylesheet" type="text/css" href="styles/cinemang.css" media="screen" />

    <style>#ratingSlider { margin: 10px; }	</style>
    <script>
        var page = ${filmSearchResult.page};
        var pages = ${filmSearchResult.pages};
        var sortColumn = '${filmSearchResult.sortColumn}';
        var sortDirection = '${filmSearchResult.sortDirection}';

        function initHeader()
        {
            var ratingParam = '${filmsForm.ratingParam}';
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

        function sortFilms(element, column)
        {
            var previousColumn = sortColumn;
            var previousDirection = sortDirection;
            var direction = 'desc';
            if (column === previousColumn)
            {
                if (previousDirection === 'asc') direction = 'desc';
                if (previousDirection === 'desc') direction = 'asc';
            }
            $('#sortColumn').val(column);
            $('#sortDirection').val(direction);
            sortColumn = column;
            sortDirection = direction;


            // remove sort indicator from all .sortableHeader
            $('.sortableHeader span').each(function(index) {
                if (this.textContent.indexOf('▼') > -1 || this.textContent.indexOf('▲') > -1)
                {
//                    this.textContent = this.textContent.replace('▼', '');
//                    this.textContent = this.textContent.replace('▲', '');
                    $(this).closest('span')[0].textContent = $(this).closest('span')[0].textContent.replace('▼', '');
                    $(this).closest('span')[0].textContent = $(this).closest('span')[0].textContent.replace('▲', '');
                }
            });
            // add sort indicator to the one that was clicked
            var sortIcon = direction == 'asc' ? '▲' : '▼';
            $(element).find('span')[0].innerHTML += ' ' + sortIcon;
//            element.textContent = element.textContent + ' ' + sortIcon;

            ajaxFilms('', column, direction);
        }

        function goToPage(pageNumber)
        {
            var parsedPage = '';
            if (pageNumber == 'first') parsedPage = 1;
            if (pageNumber == 'last') parsedPage = pages;
            if (pageNumber == 'next') parsedPage = (page + 1);
            if (pageNumber == 'previous') parsedPage = (page - 1);

            page = parsedPage;
            $('#page').val(parsedPage);

            $('.currentPageSpan').html(parsedPage);

            if (parsedPage == 1)
            {
                $('.firstButton').prop( "disabled", true );
                $('.previousButton').prop( "disabled", true );
            }
            else
            {
                $('.firstButton').prop( "disabled", false );
                $('.previousButton').prop( "disabled", false );
            }
            if (parsedPage == ${filmSearchResult.pages})
            {
                $('.nextButton').prop( "disabled", true );
                $('.lastButton').prop( "disabled", true );
            }
            else
            {
                $('.nextButton').prop( "disabled", false );
                $('.lastButton').prop( "disabled", false );
            }

            ajaxFilms(parsedPage, '', '');
        }

        function resetPagination()
        {
            $('#resetPage').val('yes');
        }

        function showPlotDialog(title, imdbID, fullPlot, director, actors, runtime, tomatoConsensus)
        {
            $( "#dialog-plot" ).dialog({
                resizable: false,
                height:'auto',
                width:650,
                modal: true,
                open: function (event, ui)
                {
                    $( "#dialog-plot").dialog('option', 'title', title);

                    var myUrl = '${pageContext.request.contextPath}/view?tab1=home&action=getPoster';
                    $( "#posterUrl").load(myUrl, {imdbId : imdbID},
                        function(responseTxt, statusTxt, xhr)
                        {
                            if(statusTxt == "success")
                            {
                                $( "#posterUrl").attr('src', responseTxt);
                            }
//                            if(statusTxt == "error")
//                                alert("Error: " + xhr.status + ": " + xhr.statusText);
                        });

                    $( "#dialogDirector").html('<b>Director:</b><br>' + director);
                    $( "#dialogActors").html('<br><br><b>Actors:</b><br>' + actors);
                    $( "#dialogRuntime").html('<br><br><b>Running Time:</b><br>' + runtime);

                    if (tomatoConsensus != '')
                        $( "#dialogTomatoConsensus").html('<br><br><b>Tomato Critic Consensus:</b><br>' + tomatoConsensus);
                    else
                        $( "#dialogTomatoConsensus").html('');

                    if (fullPlot != '')
                        $( "#dialogPlot").html('<br><br><b>Plot:</b><br>' + fullPlot);
                    else
                        $( "#dialogPlot").html('');
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

        function ajaxFilms(newPage, newSortColumn, newSortDirection)
        {
            var myUrl = '${pageContext.request.contextPath}/view?tab1=home&action=getNewPage';
            var params = {};
            if (newPage) params.page = newPage;
            if (newSortColumn) params.sortColumn = newSortColumn;
            if (newSortDirection) params.sortDirection = newSortDirection;

            $.getJSON(myUrl, params,
                    function(data, textStatus, xhr)
                    {
                        if(textStatus == "success")
                        {
                            var rows = [];
                            $.each(data, function(key, value){
                                var indexOnCurrentPage = key + 1;
                                var resultIndex = (page - 1) * 100 + indexOnCurrentPage;
                                var rowClass = indexOnCurrentPage % 2 == 0 ? 'listroweven' : 'listrowodd';
                                var row = "<tr class=" + rowClass + ">";
                                row += "<td class='alignright'>" + resultIndex + "</td>";

                                var onclickValue = "\"showPlotDialog(&quot;" +
                                        value.title + "&quot;, &quot;" +
                                        value.imdbId + "&quot;, &quot;" +
                                        value.shortFullPlot + "&quot;, &quot;" +
                                        value.director + "&quot;, &quot;" +
                                        value.actors + "&quot;, &quot;" +
                                        value.runtime + "&quot;, &quot;" +
                                        value.tomatoConsensus + "&quot;" +
                                        ");\"";

                                var styleValue = "'color: blue; text-decoration: underline; cursor: pointer'";

                                var freshImage = "";
                                if (value.tomatoImage == 'fresh')
                                    freshImage = "<img src='images/certified_logo.png' style='vertical-align: middle' height='16px'/>";

                                row += "<td><span onclick=" + onclickValue + " style=" + styleValue + ">" + value.title + freshImage + "</span></td>";

                                row += "<td class='alignright'>" + value.cinemangRating + "</td>";
                                row += "<td class='alignright'>" + value.tomatoMeter + "</td>";
                                row += "<td class='alignright'>" + value.tomatoUserMeter + "</td>";

                                onclickValue = "'window.open(&quot;" + "http://www.imdb.com/title/" + value.imdbId + "&quot;, &quot;_blank&quot);'";
                                row += "<td class='alignright' onclick=" + onclickValue + " style=" + styleValue + ">" + value.imdbRating + "</td>";

                                row += "<td class='mediumPriority alignright'>" + value.released + "</td>";
                                row += "<td class='lowPriority alignright'>" + value.imdbVotes + "</td>";
                                row += "<td class='lowPriority'>" + value.language + "</td>";
                                row += "<td class='lowPriority'>" + value.genre + "</td>";

                                row += "</tr>";
                                rows.push(row);
                            });

                            var oldTBody = document.getElementById('myTBody');
                            oldTBody.innerHTML = rows.join("");
                        }
                        if (textStatus == "error")
                            alert("Error: " + xhr.status + ": " + xhr.statusText);
                    });
        }

    </script>

</head>
<body onload="initHeader();">

<table style="margin: 0 auto; width: 100%; max-width: 1000px" class="list">
    <tr>
        <td style="width: 100%;">
            <h1 style="text-align: center;margin: 0;padding: 0;">CINEMANG <img src="images/spaceCat.png" style="height: 30px;vertical-align: middle"></h1>
        </td>
    </tr>
</table>
<br>

<form name="frmFilter" id="frmFilter" method="post" action="${pageContext.request.contextPath}/view?tab1=home&action=filterFilms">
    <input type="hidden" id="fldRating" name="fldRating">
    <input type="hidden" name="sortColumn" id="sortColumn" value="${filmSearchResult.sortColumn}"/>
    <input type="hidden" name="sortDirection" id="sortDirection" value="${filmSearchResult.sortDirection}"/>
    <input type="hidden" name="page" id="page" value="${filmSearchResult.page}"/>
    <input type="hidden" name="resetPage" id="resetPage"/>

    <table style="margin: 0 auto" class="list">
        <tr>
            <td colspan="4">
                <h2 style="text-align: center;margin: 0;">Search for Films</h2>
            </td>
        </tr>
        <tr>
            <td class="alignright"><label for="title">Title:</label></td>
            <td colspan="3"><input id="title" name="title" type="text" size="20" maxlength="32" value="${filmsForm.titleParam}"></td>
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
                            <input type="text" id="fromReleaseDateDatepicker" name="fromReleaseDateDatepicker" size="6" maxlength="10" value="${filmsForm.fromReleaseDate}">
                        </td>
                    </tr>
                    <tr>
                        <td style="border: none;text-align: right">
                            <label for="toReleaseDateDatepicker">To:</label>
                        </td>
                        <td style="border: none;">
                            <input type="text" id="toReleaseDateDatepicker" name="toReleaseDateDatepicker" size="6" maxlength="10" value="${filmsForm.toReleaseDate}">
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
        <tr>
            <td class="alignright"><label for="minimumVotes">Minimum Votes:</label></td>
            <td colspan="3"><input id="minimumVotes" name="minimumVotes" type="number" size="7" maxlength="7" value="${filmsForm.minimumVotesParam}"></td>
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
                    <option value="" ${filmsForm.language == '' ? 'selected' : ''}>Any</option>
                    <c:forEach var="uniqueLanguage" items="${uniqueLanguages}">
                        <option value="${uniqueLanguage}" ${filmsForm.language == uniqueLanguage ? 'selected' : ''}>${uniqueLanguage}</option>
                    </c:forEach>
                </select>
            </td>
        </tr>
        <tr>
            <td class="alignright"><label for="fldGenre">Genre:</label></td>
            <td colspan="3">
                <select id="fldGenre" name="fldGenre">
                    <option value="" ${filmsForm.genre == '' ? 'selected' : ''}>Any</option>
                    <c:forEach var="uniqueGenre" items="${uniqueGenres}">
                        <option value="${uniqueGenre}" ${filmsForm.genre == uniqueGenre ? 'selected' : ''}>${uniqueGenre}</option>
                    </c:forEach>
                </select>
            </td>
        </tr>
        <tr><td colspan="4" style="text-align: center"><input type="submit" value="Search" onclick="resetPagination();"/></td></tr>
        <tr><td colspan="4" style="text-align: center"><span>${filmSearchResult.searchResultsSize} Results</span></td></tr>
    </table>
</form>
<br>
<table id="filmTable" style="margin: 0 auto" class="list">
    <thead>
    <tr>
        <td colspan="100" style="text-align: center;">
            <input class="firstButton" type="button" value="First" onclick="goToPage('first')" <c:if test="${!filmSearchResult.hasPrevious}">disabled</c:if> />
            <input class="previousButton" type="button" value="Previous" onclick="goToPage('previous')" <c:if test="${!filmSearchResult.hasPrevious}">disabled</c:if> />

            <fmt:formatNumber value="${filmSearchResult.page}" var="formattedPage" pattern="#,###"/>
            <fmt:formatNumber value="${filmSearchResult.pages}" var="formattedPages" pattern="#,###"/>
            <span class="currentPageSpan">${formattedPage}</span> of ${formattedPages}

            <input class="nextButton" type="button" value="Next" onclick="goToPage('next')" <c:if test="${!filmSearchResult.hasNext}">disabled</c:if> />
            <input class="lastButton" type="button" value="Last" onclick="goToPage('last')" <c:if test="${!filmSearchResult.hasNext}">disabled</c:if> />
        </td>
    </tr>
    <tr class="listheading">
        <td></td>
        <td class="sortableHeader" onclick="sortFilms(this, 'title')">Title
            <span>
                <c:if test="${filmSearchResult.sortColumn eq 'title' and filmSearchResult.sortDirection eq 'asc'}">&#9650;</c:if>
                <c:if test="${filmSearchResult.sortColumn eq 'title' and filmSearchResult.sortDirection eq 'desc'}">&#9660;</c:if>
            </span>
        </td>
        <td class="sortableHeader alignright" onclick="sortFilms(this, 'cinemangRating')">
            <img src="images/spaceCat.png" title="Cinemang Rating: Combines imdb Rating, Tomato Meter, and Tomato User Meter" style="height:24px;vertical-align: middle"/>
            <span>
                <c:if test="${filmSearchResult.sortColumn eq 'cinemangRating' and filmSearchResult.sortDirection eq 'asc'}">&#9650;</c:if>
                <c:if test="${filmSearchResult.sortColumn eq 'cinemangRating' and filmSearchResult.sortDirection eq 'desc'}">&#9660;</c:if>
            </span>
        </td>
        <td class="sortableHeader alignright" onclick="sortFilms(this, 'tomatoMeter')">
            <img src="images/rottenTomatoes_logo.png" title="Tomato Meter" style="height:24px;vertical-align: middle"/>
            <span>
                <c:if test="${filmSearchResult.sortColumn eq 'tomatoMeter' and filmSearchResult.sortDirection eq 'asc'}">&#9650;</c:if>
                <c:if test="${filmSearchResult.sortColumn eq 'tomatoMeter' and filmSearchResult.sortDirection eq 'desc'}">&#9660;</c:if>
            </span>
        </td>
        <td class="sortableHeader alignright" onclick="sortFilms(this, 'tomatoUserMeter')">
            <img src="images/rottenTomatoes_user_logo.png" title="Tomato User Meter" style="height:24px;vertical-align: middle"/>
            <span>
                <c:if test="${filmSearchResult.sortColumn eq 'tomatoUserMeter' and filmSearchResult.sortDirection eq 'asc'}">&#9650;</c:if>
                <c:if test="${filmSearchResult.sortColumn eq 'tomatoUserMeter' and filmSearchResult.sortDirection eq 'desc'}">&#9660;</c:if>
            </span>
        </td>
        <td class="sortableHeader alignright" onclick="sortFilms(this, 'imdbRating')">
            <img src="images/imdb_logo.png" title="IMDb Rating" style="height:24px;vertical-align: middle"/>
            <span>
                <c:if test="${filmSearchResult.sortColumn eq 'imdbRating' and filmSearchResult.sortDirection eq 'asc'}">&#9650;</c:if>
                <c:if test="${filmSearchResult.sortColumn eq 'imdbRating' and filmSearchResult.sortDirection eq 'desc'}">&#9660;</c:if>
            </span>
        </td>
        <td class="sortableHeader mediumPriority alignright" onclick="sortFilms(this, 'released')">Released
            <span>
                <c:if test="${filmSearchResult.sortColumn eq 'released' and filmSearchResult.sortDirection eq 'asc'}">&#9650;</c:if>
                <c:if test="${filmSearchResult.sortColumn eq 'released' and filmSearchResult.sortDirection eq 'desc'}">&#9660;</c:if>
            </span>
        </td>
        <td class="sortableHeader lowPriority alignright" onclick="sortFilms(this, 'imdbVotes')">IMDb Votes
            <span>
                <c:if test="${filmSearchResult.sortColumn eq 'imdbVotes' and filmSearchResult.sortDirection eq 'asc'}">&#9650;</c:if>
                <c:if test="${filmSearchResult.sortColumn eq 'imdbVotes' and filmSearchResult.sortDirection eq 'desc'}">&#9660;</c:if>
            </span>
        </td>
        <td class="sortableHeader lowPriority" onclick="sortFilms(this, 'language')">Language
            <span>
                <c:if test="${filmSearchResult.sortColumn eq 'language' and filmSearchResult.sortDirection eq 'asc'}">&#9650;</c:if>
                <c:if test="${filmSearchResult.sortColumn eq 'language' and filmSearchResult.sortDirection eq 'desc'}">&#9660;</c:if>
            </span>
        </td>
        <td class="lowPriority">Genres</td>
    </tr>
    </thead>

    <tbody id="myTBody">
    <c:set var="count" value="${1 + ((filmSearchResult.page - 1) * 100)}"/>
    <c:set var="rowStyle" value="listrowodd"/>
    <c:set var="rowToggle" value="${true}"/>
    <c:forEach var="film" items="${filmSearchResult.pageOfResults}">

        <tr class="${rowStyle}">
            <td class="alignright"><fmt:formatNumber value="${count}" pattern="#,###"/></td>
            <td>
                <span onclick='showPlotDialog("${fn:escapeXml(film.title)}", "${film.imdbID}",
                        "<c:out value="${fn:escapeXml(film.shortFullPlot)}"/>",
                        "<c:out value="${fn:escapeXml(film.director)}"/>",
                        "<c:out value="${fn:escapeXml(film.actors)}"/>",
                        "<c:out value="${fn:escapeXml(film.runtime)}"/>",
                        "<c:out value="${fn:escapeXml(film.tomatoConsensus)}"/>")'
                         style="color: blue; text-decoration: underline; cursor: pointer">
                    <c:set var="filmTitle" value="${film.title}"/>
                    <c:if test="${fn:length(film.title) > 50}"><c:set var="filmTitle" value="${fn:substring(film.title, 0, 50)}..."/></c:if>
                    ${filmTitle}
                    <c:if test="${film.tomatoImage=='fresh'}"><img src="images/certified_logo.png" style="vertical-align: middle" height="16px"/></c:if>
                </span>
            </td>
            <td class="alignright">${film.cinemangRating}</td>
            <td class="alignright">${film.tomatoMeter}</td>
            <td class="alignright">${film.tomatoUserMeter}</td>
            <td class="alignright" onclick="window.open('http://www.imdb.com/title/${film.imdbID}', '_blank');" style="cursor: pointer; color: blue; text-decoration: underline;">
                ${film.imdbRating}
            </td>
            <td class="mediumPriority alignright"><fmt:formatDate value="${film.released}" pattern="yyyy"/></td>
            <td class="alignright lowPriority"><fmt:formatNumber value="${film.imdbVotes}" pattern="#,###"/></td>
            <td class="lowPriority">${film.language}</td>
            <td class="lowPriority">${film.genre}</td>
        </tr>

        <c:if test="${rowToggle}"><c:set var="rowStyle" value="listroweven"/></c:if>
        <c:if test="${!rowToggle}"><c:set var="rowStyle" value="listrowodd"/></c:if>
        <c:set var="rowToggle" value="${!rowToggle}"/>
        <c:set var="count" value="${count + 1}"/>
    </c:forEach>
    </tbody>

    <c:if test="${empty filmSearchResult.searchResults}">
        <tr><td colspan="100">-</td></tr>
    </c:if>
    <tr>
        <td colspan="100" style="text-align: center;">
            <input class="firstButton" type="button" value="First" onclick="goToPage('first')" <c:if test="${!filmSearchResult.hasPrevious}">disabled</c:if> />
            <input class="previousButton" type="button" value="Previous" onclick="goToPage('previous')" <c:if test="${!filmSearchResult.hasPrevious}">disabled</c:if> />

            <fmt:formatNumber value="${filmSearchResult.page}" var="formattedPage" pattern="#,###"/>
            <fmt:formatNumber value="${filmSearchResult.pages}" var="formattedPages" pattern="#,###"/>
            <span class="currentPageSpan">${formattedPage}</span> of ${formattedPages}

            <input class="nextButton" type="button" value="Next" onclick="goToPage('next')" <c:if test="${!filmSearchResult.hasNext}">disabled</c:if> />
            <input class="lastButton" type="button" value="Last" onclick="goToPage('last')" <c:if test="${!filmSearchResult.hasNext}">disabled</c:if> />
        </td>
    </tr>
</table>

<%-- Plot Dialog --%>
<div style="display:none;">
    <div id="dialog-plot" title="Plot" style="text-align: justify;">
        <div style="float: left; padding-right: 10pt">
            <img id="posterUrl" src="" style="max-width: 400px;"/>
        </div>
        <span id="dialogDirector"></span>
        <span id="dialogActors"></span>
        <span id="dialogRuntime"></span>
        <span id="dialogTomatoConsensus"></span>
        <span id="dialogPlot"></span>
    </div>
</div>
</body>
</html>